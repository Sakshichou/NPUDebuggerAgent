package com.example.npudebuggeragent

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.npudebuggeragent.ui.theme.NPUDebuggerAgentTheme
import java.text.SimpleDateFormat
import java.util.*

val NeonGreen = Color(0xFF39FF14)
val DarkGrey = Color(0xFF121212)
val TerminalBlack = Color(0xFF0A0A0A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = viewModel()) {
    val showAiInsight by viewModel.showAiInsight.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val sheetState = rememberModalBottomSheetState()

    // Removed Scaffold and NavigationBar here as they are provided by MainScreen in MainActivity.kt
    Box(modifier = Modifier.fillMaxSize().background(DarkGrey)) {
        PerformanceLayout(viewModel, isConnected)

        if (showAiInsight) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.dismissAiInsight() },
                sheetState = sheetState,
                containerColor = DarkGrey,
                contentColor = Color.White,
                dragHandle = { BottomSheetDefaults.DragHandle(color = NeonGreen) }
            ) {
                AiInsightModalContent(
                    onApplyFix = { viewModel.dismissAiInsight() }
                )
            }
        }
    }
}

@Composable
fun PerformanceLayout(viewModel: DashboardViewModel, isConnected: Boolean) {
    val isEnabled by viewModel.isEnabled.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val listState = rememberLazyListState()

    // Collect metrics
    val npuUtil by viewModel.npuUtilization.collectAsState()
    val temp by viewModel.temperature.collectAsState()
    val ram by viewModel.ramUsage.collectAsState()
    val battery by viewModel.batteryDrain.collectAsState()
    val latency by viewModel.inferenceLatency.collectAsState()

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "NPUDebugger Hardware Monitor",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            ConnectionStatusPill(isConnected = isConnected)
        }

        // Control Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Enable NPUDebugger Agent",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (isEnabled && isConnected) "Streaming telemetry..." else "Agent inactive",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isEnabled && isConnected) NeonGreen else Color.Gray
                    )
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { viewModel.toggleAgent(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NeonGreen,
                        checkedTrackColor = NeonGreen.copy(alpha = 0.5f)
                    )
                )
            }
        }

        // Metrics Grid (Responsive rows)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard("NPU Util", npuUtil, Modifier.weight(1f))
                MetricCard("Thermal", temp, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard("RAM (MB)", ram, Modifier.weight(1f))
                MetricCard("Battery Drain", battery, Modifier.weight(1f))
            }
            MetricCard("Inf. Latency", latency, Modifier.fillMaxWidth())
        }

        // Terminal Logs
        Text("System Logs", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(8.dp)),
            color = TerminalBlack
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.padding(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(logs) { log ->
                    val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                    Text(
                        text = "[$timestamp] $log",
                        color = if (log.contains("Error")) Color.Red else NeonGreen,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AiInsightModalContent(onApplyFix: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(bottom = 40.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "⚠️ AI Anomaly: Memory Leak Detected",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.Red,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )

        Text(
            "Un-optimized tensor allocation found in NPU delegate. High memory retention detected in Snapdragon buffer.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray
        )

        Surface(
            color = TerminalBlack,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "val options = Model.Options.Builder()\n    .setUseNNAPI(true)\n    .build()",
                modifier = Modifier.padding(16.dp),
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = NeonGreen
            )
        }

        Button(
            onClick = onApplyFix,
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonGreen,
                contentColor = DarkGrey
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Apply Fix", fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = NeonGreen
            )
        }
    }
}

@Composable
fun ConnectionStatusPill(isConnected: Boolean) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (isConnected) NeonGreen.copy(alpha = 0.2f) else Color.Red.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isConnected) NeonGreen else Color.Red)
        )
        Text(
            text = if (isConnected) "CONNECTED" else "DISCONNECTED",
            style = MaterialTheme.typography.labelSmall,
            color = if (isConnected) NeonGreen else Color.Red,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DashboardPreview() {
    NPUDebuggerAgentTheme(darkTheme = true) {
        DashboardScreen()
    }
}
