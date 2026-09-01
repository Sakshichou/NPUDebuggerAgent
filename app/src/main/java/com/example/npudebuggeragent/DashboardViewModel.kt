package com.example.npudebuggeragent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.random.Random

/**
 * DashboardViewModel manages the state of the NPUDebugger Agent in a standalone 'Demo Mode'.
 * It simulates real-time NPU telemetry, power metrics, and security intercepts.
 */
class DashboardViewModel : ViewModel() {

    private val _isEnabled = MutableStateFlow(false)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _npuUtilization = MutableStateFlow("0%")
    val npuUtilization: StateFlow<String> = _npuUtilization.asStateFlow()

    private val _temperature = MutableStateFlow("0°")
    val temperature: StateFlow<String> = _temperature.asStateFlow()

    private val _ramUsage = MutableStateFlow("0")
    val ramUsage: StateFlow<String> = _ramUsage.asStateFlow()

    private val _batteryDrain = MutableStateFlow("0mW")
    val batteryDrain: StateFlow<String> = _batteryDrain.asStateFlow()

    private val _inferenceLatency = MutableStateFlow("0ms")
    val inferenceLatency: StateFlow<String> = _inferenceLatency.asStateFlow()

    private val _showAiInsight = MutableStateFlow(false)
    val showAiInsight: StateFlow<Boolean> = _showAiInsight.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private val _securityLogs = MutableStateFlow(listOf(
        "[PASS] Clipboard access denied",
        "[WARN] Unencrypted payload detected on port 443",
        "[PASS] Camera mic disabled",
        "[PASS] Unauthorized socket bind attempt blocked",
        "[PASS] Location API sandbox integrity verified"
    ))
    val securityLogs: StateFlow<List<String>> = _securityLogs.asStateFlow()

    private var demoJob: Job? = null
    private var iterations = 0

    /**
     * Toggles the agent's active state. In demo mode, this starts/stops a simulation loop.
     */
    fun toggleAgent(enabled: Boolean) {
        _isEnabled.value = enabled
        if (enabled) {
            // Requirement: immediately set isConnected = true and log
            _isConnected.value = true
            addLog("[SYSTEM] Connected to local bridge")
            startDemoMode()
        } else {
            // Requirement: cancel coroutine, set isConnected = false, and log disconnection
            stopDemoMode()
        }
    }

    private fun startDemoMode() {
        demoJob?.cancel()
        iterations = 0
        demoJob = viewModelScope.launch {
            while (true) {
                // Requirement: Loop every 500ms
                delay(500)
                iterations++
                
                // Requirement: Mock random values
                val npu = Random.nextInt(0, 101)
                val thermal = Random.nextInt(30, 91)
                
                // Requirement: Every 10 seconds (20 iterations), force a spike (RAM > 2400MB)
                val isSpikeIteration = iterations % 20 == 0
                val ram = if (isSpikeIteration) {
                    Random.nextInt(2500, 4001)
                } else {
                    Random.nextInt(500, 2401)
                }
                
                val battery = Random.nextInt(100, 801)
                val latency = Random.nextInt(5, 51)

                // Update UI states
                _npuUtilization.value = "$npu%"
                _temperature.value = "$thermal°C"
                _ramUsage.value = ram.toString()
                _batteryDrain.value = "${battery}mW"
                _inferenceLatency.value = "${latency}ms"

                // Requirement: Push simulated JSON payload string to logs
                val payload = JSONObject().apply {
                    put("npu_utilization", npu)
                    put("temperature_c", thermal.toDouble())
                    put("memory_mb", ram)
                    put("battery_drain_mw", battery)
                    put("inference_latency_ms", latency)
                }.toString()
                
                addLog("TX: $payload")

                // Requirement: Spike triggers AI Insight bottom sheet
                if (ram > 2400) {
                    _showAiInsight.value = true
                }
            }
        }
    }

    private fun stopDemoMode() {
        demoJob?.cancel()
        demoJob = null
        _isConnected.value = false
        addLog("[SYSTEM] Disconnected")
        
        // Reset telemetry values
        _npuUtilization.value = "0%"
        _temperature.value = "0°"
        _ramUsage.value = "0"
        _batteryDrain.value = "0mW"
        _inferenceLatency.value = "0ms"
    }

    fun dismissAiInsight() {
        _showAiInsight.value = false
    }

    private fun addLog(message: String) {
        _logs.update { currentLogs ->
            val newList = currentLogs.toMutableList()
            if (newList.size > 50) newList.removeAt(0)
            newList.add(message)
            newList
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopDemoMode()
    }
}
