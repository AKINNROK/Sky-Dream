package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmotionFondnessColor
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VelvetPurple

@Composable
fun SettingsDialog(
    currentApiKey: String?,
    currentEndpoint: String?,
    onDismiss: () -> Unit,
    onSave: (apiKey: String?, endpoint: String?) -> Unit
) {
    var apiKey by remember { mutableStateOf(currentApiKey ?: "") }
    var endpoint by remember { mutableStateOf(currentEndpoint ?: "") }

    val hasDefaultKey = BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = CyanNeon,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "การตั้งค่าโมเดล AI & เนื้อเรื่อง",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cloud Status Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark, RoundedCornerShape(10.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmotionFondnessColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "โมเดลหลัก: Google AI Studio (Gemini 3.5 Flash)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (hasDefaultKey) "ระบบเชื่อมต่อ API อัตโนมัติพร้อมใช้งาน" else "ใช้คีย์ระบบ AI Studio หรือกำหนดคีย์ส่วนตัวด้านล่าง",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                }

                Text(
                    text = "กำหนด API Key เอง (Gemini / Groq / OpenRouter):",
                    style = MaterialTheme.typography.labelMedium.copy(color = TextPrimary, fontWeight = FontWeight.SemiBold)
                )

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key ส่วนตัว (ไม่บังคับ)") },
                    placeholder = { Text("AIzaSy... หรือ sk-or-v1-...") },
                    leadingIcon = {
                        Icon(Icons.Default.Key, contentDescription = null, tint = CyanNeon)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("settings_api_key_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = endpoint,
                    onValueChange = { endpoint = it },
                    label = { Text("Custom Endpoint URL (ไม่บังคับ)") },
                    placeholder = { Text("เว้นว่างไว้สำหรับ Gemini มาตรฐาน") },
                    modifier = Modifier.fillMaxWidth().testTag("settings_endpoint_input"),
                    singleLine = true
                )

                // Dramatic / Uncensored note
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "ทิศทางการเล่าเรื่อง: รองรับมิติอารมณ์แนวดาร์กโรแมนซ์ โอโตเมะ ยึดติดครอบงำ และแฟนตาซีเข้มข้น ดำเนินเรื่องบนเครื่องของคุณพร้อมเชื่อมต่อ AI เพื่อขับเคลื่อนเรื่องราวแบบเรียลไทม์",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, lineHeight = 16.sp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(apiKey, endpoint) },
                colors = ButtonDefaults.buttonColors(containerColor = VelvetPurple),
                modifier = Modifier.testTag("settings_save_button")
            ) {
                Text("บันทึก", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ปิด", color = TextSecondary)
            }
        },
        containerColor = CardSurface
    )
}
