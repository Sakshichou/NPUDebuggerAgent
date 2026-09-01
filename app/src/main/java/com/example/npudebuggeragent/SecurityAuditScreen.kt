package com.example.npudebuggeragent

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.npudebuggeragent.ui.theme.NPUDebuggerAgentTheme

@Composable
fun SecurityAuditScreen(viewModel: DashboardViewModel = viewModel()) {
    val securityLogs by viewModel.securityLogs.collectAsState()

    // Map the string logs from ViewModel to AuditLog objects for the UI
    val auditLogs = securityLogs.map { log ->
        val isWarning = log.contains("[WARN]")
        val cleanTitle = if (isWarning) "Integrity Warning" else "Security Pass"
        val description = log.replace("[WARN] ", "").replace("[PASS] ", "")
        AuditLog(cleanTitle, description, !isWarning)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGrey)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Shield Icon
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.Green.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Security Status",
                modifier = Modifier.size(60.dp),
                tint = NeonGreen
            )
        }
        
        Text(
            "System Integrity: SECURE",
            style = MaterialTheme.typography.headlineSmall,
            color = NeonGreen,
            fontWeight = FontWeight.Bold
        )

        HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))

        Text(
            "Recent Security Intercepts",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelLarge,
            color = Color.Gray
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(auditLogs) { log ->
                AuditLogItem(log)
            }
        }
    }
}

data class AuditLog(val title: String, val description: String, val isBlocked: Boolean)

@Composable
fun AuditLogItem(log: AuditLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = if (log.isBlocked) Icons.Default.Lock else Icons.Default.Warning,
                contentDescription = null,
                tint = if (log.isBlocked) NeonGreen else Color.Red
            )
            Column {
                Text(
                    text = log.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = log.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SecurityAuditPreview() {
    NPUDebuggerAgentTheme(darkTheme = true) {
        SecurityAuditScreen()
    }
}
