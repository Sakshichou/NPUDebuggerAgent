package com.example.npudebuggeragent

import kotlinx.coroutines.*
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * WebSocketManager handles the connection to the NPU bridge and streams
 * simulated telemetry data using OkHttp and Kotlin Coroutines.
 */
class WebSocketManager(
    private val onConnected: () -> Unit,
    private val onDisconnected: () -> Unit,
    private val onError: (String) -> Unit,
    private val onLog: (String) -> Unit
) {
    private var client: OkHttpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()
    
    private var webSocket: WebSocket? = null
    private var telemetryJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Connects to ws://<ipAddress>:<port>/ws
     */
    fun connect(ipAddress: String, port: Int = 8000) {
        val request = Request.Builder()
            .url("ws://$ipAddress:$port/ws")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                onLog("Connection established with bridge.")
                onConnected()
                startTelemetryStream()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                onLog("Bridge response: $text")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                val errorMsg = t.message ?: "WebSocket connection failure"
                onLog("Error: $errorMsg")
                onError(errorMsg)
                stopTelemetryStream()
                onDisconnected()
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                onLog("Closing connection: $reason")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                onLog("Connection closed.")
                onDisconnected()
            }
        })
    }

    /**
     * Starts a coroutine that sends simulated NPU telemetry every 500ms.
     * Introduces artificial spikes every 10 seconds.
     */
    private fun startTelemetryStream() {
        telemetryJob?.cancel()
        telemetryJob = scope.launch {
            var tick = 0
            while (isActive) {
                tick++
                // Hardware spike logic: every 10 seconds (20 ticks at 500ms)
                val isSpike = tick % 20 == 0
                
                val npuUtil = if (isSpike) 95 else (15..45).random()
                val temp = if (isSpike) 82.5 else (52..58).random().toDouble() + Math.random()
                val ram = if (isSpike) 2400 else (900..1100).random()
                
                // New metrics for hackathon features
                val batteryDrain = (350..550).random()
                val latency = (8..15).random()

                val telemetry = JSONObject().apply {
                    put("npu_utilization", npuUtil)
                    put("temperature_c", temp)
                    put("memory_mb", ram)
                    put("battery_drain_mw", batteryDrain)
                    put("inference_latency_ms", latency)
                    put("timestamp", System.currentTimeMillis())
                }.toString()

                val sent = webSocket?.send(telemetry) ?: false
                if (sent) {
                    onLog("TX: $telemetry")
                } else {
                    onLog("Failed to send telemetry - socket might be closed.")
                }

                delay(500)
            }
        }
    }

    private fun stopTelemetryStream() {
        telemetryJob?.cancel()
        telemetryJob = null
    }

    /**
     * Safely closes the WebSocket connection and cancels telemetry simulation.
     */
    fun disconnect() {
        stopTelemetryStream()
        webSocket?.close(1000, "Termination requested by agent")
        webSocket = null
        onLog("Agent disconnected.")
    }
}
