package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CharacterCard
import com.example.data.model.EmotionState
import com.example.data.model.WorkEntity
import com.example.ui.components.EmotionRadarChart
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.EmotionAffectionColor
import com.example.ui.theme.EmotionAversionColor
import com.example.ui.theme.EmotionFearColor
import com.example.ui.theme.EmotionFondnessColor
import com.example.ui.theme.EmotionObsessionColor
import com.example.ui.theme.EmotionTrustColor
import com.example.ui.theme.MysticViolet
import com.example.ui.theme.RoseCrimson
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VelvetPurple
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCharacterCardScreen(
    works: List<WorkEntity>,
    selectedWorkId: Long? = null,
    onBack: () -> Unit,
    onSaveCharacterCard: (CharacterCard) -> Unit,
    onGenerateAiCharacter: (suspend (WorkEntity?, String?) -> CharacterCard)? = null
) {
    val coroutineScope = rememberCoroutineScope()

    // Universe selection
    var currentWorkId by remember {
        mutableStateOf(selectedWorkId ?: works.firstOrNull()?.id ?: 0L)
    }
    var expandedWorkDropdown by remember { mutableStateOf(false) }

    // Character Card Fields
    var name by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var persona by remember { mutableStateOf("") }
    var firstMessage by remember { mutableStateOf("") }
    var exampleDialogue by remember { mutableStateOf("") }
    var speechStyle by remember { mutableStateOf("") }
    var scenario by remember { mutableStateOf("") }

    // Initial 6-Axis Emotions
    var affection by remember { mutableFloatStateOf(20f) }
    var obsession by remember { mutableFloatStateOf(15f) }
    var trust by remember { mutableFloatStateOf(15f) }
    var fondness by remember { mutableFloatStateOf(10f) }
    var aversion by remember { mutableFloatStateOf(0f) }
    var fear by remember { mutableFloatStateOf(0f) }

    // AI Generator state
    var isGeneratingAi by remember { mutableStateOf(false) }
    var aiPromptHint by remember { mutableStateOf("") }
    var aiGeneratedSuccessMessage by remember { mutableStateOf<String?>(null) }

    val currentPreviewEmotions = remember(affection, obsession, trust, fondness, aversion, fear) {
        EmotionState(
            affection = affection.toInt(),
            obsession = obsession.toInt(),
            trust = trust.toInt(),
            fondness = fondness.toInt(),
            aversion = aversion.toInt(),
            fear = fear.toInt()
        )
    }

    val selectedWork = works.find { it.id == currentWorkId } ?: works.firstOrNull()
    val isFormValid = name.isNotBlank() && persona.isNotBlank() && currentWorkId > 0L

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "สร้างการ์ดตัวละคร (Character Card)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "บันทึกข้อมูล Persona และสภาวะจิตวิทยาลง Room Database",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("create_char_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ย้อนกลับ",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        },
        containerColor = BackgroundDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Universe / WorldLore Selection
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Public, contentDescription = null, tint = MysticViolet)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "1. จักรวาลต้นสังกัด (WorldLore Universe)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    if (works.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = expandedWorkDropdown,
                            onExpandedChange = { expandedWorkDropdown = !expandedWorkDropdown }
                        ) {
                            OutlinedTextField(
                                value = selectedWork?.title ?: "เลือกจักรวาล...",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedWorkDropdown) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .testTag("work_selector_field"),
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedWorkDropdown,
                                onDismissRequest = { expandedWorkDropdown = false }
                            ) {
                                works.forEach { work ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    text = work.title,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                                )
                                                Text(
                                                    text = work.genre,
                                                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                                                )
                                            }
                                        },
                                        onClick = {
                                            currentWorkId = work.id
                                            expandedWorkDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "ยังไม่มีจักรวาลในฐานข้อมูล กรุณาสร้างโลกก่อน",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Red)
                        )
                    }
                }
            }

            // AI Character Generator Feature (Gemini powered)
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, VelvetPurple.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanNeon)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "สุ่มสร้างตัวละครด้วย Gemini AI",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                        }

                        Surface(
                            color = VelvetPurple.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "AI Generator",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MysticViolet,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "คิดไม่ออกว่าจะสร้างตัวละครอย่างไร? ให้ Gemini ช่วยคิด Persona, สไตล์การพูด, ฉากเปิดเรื่อง และค่าอารมณ์ 6 แกนให้สอดคล้องกับจักรวาลนี้โดยอัตโนมัติ",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                    )

                    OutlinedTextField(
                        value = aiPromptHint,
                        onValueChange = { aiPromptHint = it },
                        placeholder = {
                            Text(
                                "ระบุแนวคิดเพิ่มเติม (ไม่บังคับ) เช่น: 'อัศวินผู้เย็นชาแต่คลั่งรัก', 'นักฆ่าสองบุคลิก'",
                                fontSize = 12.sp,
                                color = TextSecondary.copy(alpha = 0.6f)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_character_hint_field"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DeepObsidian,
                            unfocusedContainerColor = DeepObsidian,
                            focusedBorderColor = MysticViolet,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Button(
                        onClick = {
                            if (!isGeneratingAi && onGenerateAiCharacter != null) {
                                coroutineScope.launch {
                                    isGeneratingAi = true
                                    try {
                                        val generated = onGenerateAiCharacter(selectedWork, aiPromptHint)
                                        name = generated.name
                                        title = generated.title
                                        persona = generated.persona
                                        speechStyle = generated.speechStyle
                                        scenario = generated.scenario
                                        firstMessage = generated.firstMessage
                                        exampleDialogue = generated.exampleDialogue
                                        affection = generated.initialAffection.toFloat()
                                        obsession = generated.initialObsession.toFloat()
                                        trust = generated.initialTrust.toFloat()
                                        fondness = generated.initialFondness.toFloat()
                                        aversion = generated.initialAversion.toFloat()
                                        fear = generated.initialFear.toFloat()
                                        aiGeneratedSuccessMessage = "สร้าง '${generated.name}' สำเร็จ! ปรับแต่งฟิลด์ด้านล่างตามต้องการได้เลย"
                                    } catch (e: Exception) {
                                        aiGeneratedSuccessMessage = "เกิดข้อผิดพลาดในการสร้าง: ${e.message}"
                                    } finally {
                                        isGeneratingAi = false
                                    }
                                }
                            }
                        },
                        enabled = !isGeneratingAi && works.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_generate_character_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = VelvetPurple),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isGeneratingAi) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gemini กำลังออกแบบตัวละคร...", color = Color.White, fontSize = 13.sp)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = CyanNeon)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "สุ่มสร้างตัวละครทันที (AI Generate)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    if (!aiGeneratedSuccessMessage.isNullOrBlank()) {
                        Surface(
                            color = CyanNeon.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = aiGeneratedSuccessMessage!!,
                                    style = MaterialTheme.typography.bodySmall.copy(color = CyanNeon)
                                )
                            }
                        }
                    }
                }
            }

            // Section 2: Character Identity & Persona
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Badge, contentDescription = null, tint = CyanNeon)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "2. อัตลักษณ์และบุคลิกภาพ (Character Identity)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("ชื่อตัวละคร (Character Name) *") },
                        placeholder = { Text("เช่น แกรนด์ดยุค เคเลียม, นักฆ่าพันหน้า") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("character_name_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("ฉายา / ตำแหน่ง / บทบาท") },
                        placeholder = { Text("เช่น แม่ทัพกองหน้าออบซิเดียน, เจ้าพ่อโลกมืด") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("character_title_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = persona,
                        onValueChange = { persona = it },
                        label = { Text("Persona & จิตวิทยาตัวละคร *") },
                        placeholder = { Text("บรรยายอุปนิสัย แรงผลักดัน บาดแผลในใจ ความยึดติด ความสัมพันธ์ต่อผู้เล่น...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("character_persona_input")
                    )

                    OutlinedTextField(
                        value = speechStyle,
                        onValueChange = { speechStyle = it },
                        label = { Text("สไตล์การพูด (Speech Quirks & Tone)") },
                        placeholder = { Text("เช่น เสียงทุ้มต่ำ เยือกเย็น เสียดสี เว้นจังหวะสายตา...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("character_speech_style_input"),
                        singleLine = true
                    )
                }
            }

            // Section 3: Opening Scene & Dialogue Examples
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FormatQuote, contentDescription = null, tint = EmotionAffectionColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "3. ฉากเปิดเรื่องและตัวอย่างบทสนทนา (Dialogue & Scene)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }

                    OutlinedTextField(
                        value = scenario,
                        onValueChange = { scenario = it },
                        label = { Text("สถานการณ์เริ่มต้น (Scenario)") },
                        placeholder = { Text("เช่น เผชิญหน้ากันในห้องทรงอักษรหลังแผนการหลบหนีล้มเหลว") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("character_scenario_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = firstMessage,
                        onValueChange = { firstMessage = it },
                        label = { Text("ฉากเปิดเรื่อง (First Message / Webtoon Opener)") },
                        placeholder = { Text("*บรรยายฉากและการเคลื่อนไหวในดอกจัน* \n\n\"บทพูดของตัวละครในเครื่องหมายคำพูด...\"") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .testTag("character_first_message_input")
                    )

                    OutlinedTextField(
                        value = exampleDialogue,
                        onValueChange = { exampleDialogue = it },
                        label = { Text("ตัวอย่างบทสนทนา (Example Dialogue)") },
                        placeholder = { Text("<START>\nผู้เล่น: *จ้องตากลับ* ฉันจะไม่ยอมแพ้\nตัวละคร: *แค่นหัวเราะในลำคอ* \"ความดื้อรั้นของเจ้านี่แหละที่ข้าโปรดปราน\"") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("character_example_dialogue_input")
                    )
                }
            }

            // Section 4: Initial 6-Axis Emotions & Live Radar Preview
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = VelvetPurple)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "4. สภาวะอารมณ์เริ่มต้น 6 มิติ (Initial 6-Axis Radar)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Live Radar Chart Preview
                    EmotionRadarChart(
                        emotions = currentPreviewEmotions,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 6 Sliders
                    EmotionSliderInput(
                        label = "ความเสน่หา (Affection)",
                        value = affection,
                        onValueChange = { affection = it },
                        color = EmotionAffectionColor
                    )
                    EmotionSliderInput(
                        label = "ความยึดติด (Obsession)",
                        value = obsession,
                        onValueChange = { obsession = it },
                        color = EmotionObsessionColor
                    )
                    EmotionSliderInput(
                        label = "ความเชื่อใจ (Trust)",
                        value = trust,
                        onValueChange = { trust = it },
                        color = EmotionTrustColor
                    )
                    EmotionSliderInput(
                        label = "ความผูกพัน (Fondness)",
                        value = fondness,
                        onValueChange = { fondness = it },
                        color = EmotionFondnessColor
                    )
                    EmotionSliderInput(
                        label = "ความรังเกียจ (Aversion)",
                        value = aversion,
                        onValueChange = { aversion = it },
                        color = EmotionAversionColor
                    )
                    EmotionSliderInput(
                        label = "ความหวาดกลัว (Fear)",
                        value = fear,
                        onValueChange = { fear = it },
                        color = EmotionFearColor
                    )
                }
            }

            // Section 5: Prominent Save Button
            Button(
                onClick = {
                    if (isFormValid) {
                        val card = CharacterCard(
                            workId = currentWorkId,
                            name = name.trim(),
                            title = title.trim(),
                            persona = persona.trim(),
                            firstMessage = firstMessage.trim().ifBlank {
                                "*ดวงตาคมกริบของ ${name.trim()} จับจ้องมาที่คุณอย่างเงียบงันในเงามืด...*\n\n\"เจ้าคิดว่าเจ้ากำลังทำอะไรอยู่?\""
                            },
                            exampleDialogue = exampleDialogue.trim(),
                            speechStyle = speechStyle.trim(),
                            scenario = scenario.trim(),
                            initialAffection = affection.toInt(),
                            initialObsession = obsession.toInt(),
                            initialTrust = trust.toInt(),
                            initialFondness = fondness.toInt(),
                            initialAversion = aversion.toInt(),
                            initialFear = fear.toInt(),
                            avatarColor = 0xFF7C4DFF
                        )
                        onSaveCharacterCard(card)
                    }
                },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_character_card_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VelvetPurple,
                    disabledContainerColor = CardBorder
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "บันทึกการ์ดตัวละครลงในฐานข้อมูล",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EmotionSliderInput(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    color: Color
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            )
            Text(
                text = "${value.toInt()} / 100",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = Color.White.copy(alpha = 0.1f)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
