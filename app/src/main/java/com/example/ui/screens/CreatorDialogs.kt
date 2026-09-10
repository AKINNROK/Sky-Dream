package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.EmotionAffectionColor
import com.example.ui.theme.EmotionAversionColor
import com.example.ui.theme.EmotionFearColor
import com.example.ui.theme.EmotionFondnessColor
import com.example.ui.theme.EmotionObsessionColor
import com.example.ui.theme.EmotionTrustColor
import com.example.ui.theme.MysticViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VelvetPurple

@Composable
fun CreateWorkDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, genre: String, summary: String, worldviewDoc: String, tags: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("Dark Romance / Fantasy") }
    var summary by remember { mutableStateOf("") }
    var worldviewDoc by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("Webtoon,Story,Fantasy") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "สร้างจักรวาล / ผลงานใหม่",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "กำหนดเอกสารโลกทัศน์ (Lore) กลาง ซึ่งจะถูกส่งเป็นฉากหลังให้ทุกตัวละครในจักรวาลนี้",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("ชื่อจักรวาล / เรื่อง") },
                    placeholder = { Text("เช่น พันธนาการรัตติกาล, ไซเบอร์กรุงเทพ 2099") },
                    modifier = Modifier.fillMaxWidth().testTag("work_title_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = genre,
                    onValueChange = { genre = it },
                    label = { Text("แนวเรื่อง (Genre)") },
                    placeholder = { Text("เช่น ดาร์กโรแมนซ์ / ยุทธภพ / ไซเบอร์พังก์") },
                    modifier = Modifier.fillMaxWidth().testTag("work_genre_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("เรื่องย่อ (Summary)") },
                    placeholder = { Text("เกริ่นนำหรือจุดดึงดูดของโลกนี้สั้นๆ...") },
                    modifier = Modifier.fillMaxWidth().testTag("work_summary_input"),
                    maxLines = 3
                )

                OutlinedTextField(
                    value = worldviewDoc,
                    onValueChange = { worldviewDoc = it },
                    label = { Text("เอกสารโลกทัศน์ (Lore กลาง)") },
                    placeholder = { Text("กฎของโลก, พลังเวทต้องห้าม, ขั้วอำนาจ, ชนชั้นทางสังคม...") },
                    modifier = Modifier.fillMaxWidth().height(140.dp).testTag("work_worldview_input")
                )

                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("แท็ก (คั่นด้วยจุลภาค)") },
                    placeholder = { Text("เช่น โรแมนซ์, สืบสวน, ดาร์ก, แฟนตาซี") },
                    modifier = Modifier.fillMaxWidth().testTag("work_tags_input"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && worldviewDoc.isNotBlank()) {
                        onConfirm(title, genre, summary, worldviewDoc, tags)
                    }
                },
                enabled = title.isNotBlank() && worldviewDoc.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = VelvetPurple),
                modifier = Modifier.testTag("work_confirm_button")
            ) {
                Text("สร้างโลก", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ยกเลิก", color = TextSecondary)
            }
        },
        containerColor = CardSurface
    )
}

@Composable
fun CreateCharacterDialog(
    workTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        title: String,
        persona: String,
        firstMessage: String,
        exampleDialogue: String,
        speechStyle: String,
        scenario: String,
        affection: Int,
        obsession: Int,
        trust: Int,
        fondness: Int,
        aversion: Int,
        fear: Int
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var persona by remember { mutableStateOf("") }
    var firstMessage by remember { mutableStateOf("") }
    var exampleDialogue by remember { mutableStateOf("") }
    var speechStyle by remember { mutableStateOf("") }
    var scenario by remember { mutableStateOf("") }

    var affection by remember { mutableFloatStateOf(10f) }
    var obsession by remember { mutableFloatStateOf(15f) }
    var trust by remember { mutableFloatStateOf(10f) }
    var fondness by remember { mutableFloatStateOf(10f) }
    var aversion by remember { mutableFloatStateOf(0f) }
    var fear by remember { mutableFloatStateOf(0f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "เพิ่มตัวละครใน \"$workTitle\"",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "การ์ดตัวละคร: บุคลิกภาพ, ฉากเปิดเรื่อง, ตัวอย่างบทพูด และค่าอารมณ์เริ่มต้น 6 แกน",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("ชื่อตัวละคร") },
                    placeholder = { Text("เช่น ลอร์ดจูเลียน, ซิลเวีย") },
                    modifier = Modifier.fillMaxWidth().testTag("char_name_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("ฉายา / บทบาท") },
                    placeholder = { Text("เช่น ดยุคผู้ไร้หัวใจ, แฮกเกอร์เงา") },
                    modifier = Modifier.fillMaxWidth().testTag("char_title_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = persona,
                    onValueChange = { persona = it },
                    label = { Text("บุคลิกภาพ & จิตวิทยา (Persona)") },
                    placeholder = { Text("ลักษณะนิสัย ปมในใจ จุดกระตุ้นความยึดติด ความลับ...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp).testTag("char_persona_input")
                )

                OutlinedTextField(
                    value = firstMessage,
                    onValueChange = { firstMessage = it },
                    label = { Text("ข้อความเปิดเรื่อง (First Message)") },
                    placeholder = { Text("*สายฝนโปรยปรายลงมาระเบียง...* \"เจ้าคิดว่าจะหนีพ้นข้าอย่างนั้นหรือ?\"") },
                    modifier = Modifier.fillMaxWidth().height(100.dp).testTag("char_first_msg_input")
                )

                OutlinedTextField(
                    value = exampleDialogue,
                    onValueChange = { exampleDialogue = it },
                    label = { Text("ตัวอย่างบทสนทนาอ้างอิง (Example Dialogue)") },
                    placeholder = { Text("<START>\nผู้เล่น: ปล่อยข้าไปเถอะ\nตัวละคร: *ยกยิ้มเย็นชา* \"ไม่มีวัน\"") },
                    modifier = Modifier.fillMaxWidth().height(80.dp).testTag("char_example_dialogue_input")
                )

                OutlinedTextField(
                    value = speechStyle,
                    onValueChange = { speechStyle = it },
                    label = { Text("ลักษณะและสไตล์การพูด") },
                    placeholder = { Text("เช่น เสียงนุ่มลึกแต่เย็นชา สรรพนาม 'ข้า-เจ้า'") },
                    modifier = Modifier.fillMaxWidth().testTag("char_speech_style_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = scenario,
                    onValueChange = { scenario = it },
                    label = { Text("ฉากสถานการณ์เริ่มต้น") },
                    placeholder = { Text("เช่น เผชิญหน้ากันในคุกใต้ดินหลวงยามดึก") },
                    modifier = Modifier.fillMaxWidth().testTag("char_scenario_input"),
                    singleLine = true
                )

                Text(
                    text = "ค่า 6 มิติอารมณ์เริ่มต้น (0-100):",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                )

                EmotionSlider(label = "ความเสน่หา (Affection)", value = affection, color = EmotionAffectionColor) { affection = it }
                EmotionSlider(label = "ความยึดติด (Obsession)", value = obsession, color = EmotionObsessionColor) { obsession = it }
                EmotionSlider(label = "ความเชื่อใจ (Trust)", value = trust, color = EmotionTrustColor) { trust = it }
                EmotionSlider(label = "ความผูกพัน (Fondness)", value = fondness, color = EmotionFondnessColor) { fondness = it }
                EmotionSlider(label = "ความรังเกียจ (Aversion)", value = aversion, color = EmotionAversionColor) { aversion = it }
                EmotionSlider(label = "ความหวาดกลัว (Fear)", value = fear, color = EmotionFearColor) { fear = it }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && firstMessage.isNotBlank()) {
                        onConfirm(
                            name,
                            title,
                            persona,
                            firstMessage,
                            exampleDialogue,
                            speechStyle,
                            scenario,
                            affection.toInt(),
                            obsession.toInt(),
                            trust.toInt(),
                            fondness.toInt(),
                            aversion.toInt(),
                            fear.toInt()
                        )
                    }
                },
                enabled = name.isNotBlank() && firstMessage.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = VelvetPurple),
                modifier = Modifier.testTag("char_confirm_button")
            ) {
                Text("เพิ่มตัวละคร", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ยกเลิก", color = TextSecondary)
            }
        },
        containerColor = CardSurface
    )
}

@Composable
private fun EmotionSlider(
    label: String,
    value: Float,
    color: Color,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = color))
            Text(text = "${value.toInt()}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = color))
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color
            )
        )
    }
}
