package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ChatSessionEntity
import com.example.data.model.CharacterEntity
import com.example.data.model.EmotionDeltas
import com.example.data.model.EmotionState
import com.example.data.model.WorkEntity
import com.example.ui.components.CompactedMemoryCard
import com.example.ui.components.EmotionSummaryHeader
import com.example.ui.components.ExportStoryDialog
import com.example.ui.components.FullEmotionTrackerCard
import com.example.ui.components.WebtoonMessageItem
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.MysticViolet
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VelvetPurple
import kotlinx.coroutines.launch

/**
 * Visual atmosphere styling model derived from character 6-axis emotion state.
 */
data class EmotionAtmosphere(
    val bgTopColor: Color,
    val bgBottomColor: Color,
    val surfaceColor: Color,
    val cardBorderColor: Color,
    val accentColor: Color,
    val moodLabel: String,
    val moodSubtitle: String,
    val moodIcon: ImageVector
)

fun getAtmosphereForEmotion(emotions: EmotionState): EmotionAtmosphere {
    return when {
        // High Fear (fear >= 35): Dark, eerie, shadowy desaturated charcoal & cold ghost mist
        emotions.fear >= 35 && emotions.fear >= emotions.affection && emotions.fear >= emotions.fondness -> {
            EmotionAtmosphere(
                bgTopColor = Color(0xFF090D12),
                bgBottomColor = Color(0xFF040608),
                surfaceColor = Color(0xFF111822),
                cardBorderColor = Color(0xFF2B3A4A),
                accentColor = Color(0xFF90A4AE),
                moodLabel = "บรรยากาศ: วิตกกังวล & หวาดกลัว (Shadow Eeriness)",
                moodSubtitle = "ความเย็นเยียบและเงามืดสลัวปกคลุมทั่วบทสนทนา",
                moodIcon = Icons.Default.VisibilityOff
            )
        }
        // High Obsession (obsession >= 35): Seductive dark violet / crimson orchid abyss
        emotions.obsession >= 35 && emotions.obsession >= emotions.fear -> {
            EmotionAtmosphere(
                bgTopColor = Color(0xFF17081E),
                bgBottomColor = Color(0xFF0D0312),
                surfaceColor = Color(0xFF240E30),
                cardBorderColor = Color(0xFF581C74),
                accentColor = Color(0xFFE040FB),
                moodLabel = "บรรยากาศ: คลั่งรัก & ยึดติด (Obsessive Abyss)",
                moodSubtitle = "สายตาแห่งความปรารถนาอันรุนแรงตรึงไว้ไม่คลาดสายตา",
                moodIcon = Icons.Default.Visibility
            )
        }
        // High Affection (affection >= 40): Warm romantic dark crimson rose / velvet wine
        emotions.affection >= 40 -> {
            EmotionAtmosphere(
                bgTopColor = Color(0xFF1B0714),
                bgBottomColor = Color(0xFF0E020A),
                surfaceColor = Color(0xFF2B0C20),
                cardBorderColor = Color(0xFF6B184B),
                accentColor = Color(0xFFFF4081),
                moodLabel = "บรรยากาศ: เสน่หาลึกซึ้ง (Crimson Rose)",
                moodSubtitle = "ความอบอุ่นและประกายหวานซ่อนลึกแผ่ซ่านทั่วทุกถ้อยคำ",
                moodIcon = Icons.Default.Favorite
            )
        }
        // High Trust (trust >= 40): Celestial midnight azure / calm clarity
        emotions.trust >= 40 -> {
            EmotionAtmosphere(
                bgTopColor = Color(0xFF071424),
                bgBottomColor = Color(0xFF030A12),
                surfaceColor = Color(0xFF0D243E),
                cardBorderColor = Color(0xFF1B4E80),
                accentColor = Color(0xFF00E5FF),
                moodLabel = "บรรยากาศ: ไว้เนื้อเชื่อใจ (Midnight Azure)",
                moodSubtitle = "กำแพงในใจลดลง เผยความจริงใจและความสงบนิ่ง",
                moodIcon = Icons.Default.Shield
            )
        }
        // High Fondness (fondness >= 40): Gentle emerald harmony / deep woodland
        emotions.fondness >= 40 -> {
            EmotionAtmosphere(
                bgTopColor = Color(0xFF071A0E),
                bgBottomColor = Color(0xFF030D07),
                surfaceColor = Color(0xFF0E2B18),
                cardBorderColor = Color(0xFF1C5731),
                accentColor = Color(0xFF00E676),
                moodLabel = "บรรยากาศ: อบอุ่น & ผูกพัน (Emerald Harmony)",
                moodSubtitle = "ความรู้สึกคุ้นเคยและสบายใจดั่งสายลมอ่อนยามเช้า",
                moodIcon = Icons.Default.Security
            )
        }
        // High Aversion (aversion >= 35): Volcanic rust ember / hostile tension
        emotions.aversion >= 35 -> {
            EmotionAtmosphere(
                bgTopColor = Color(0xFF1C0A04),
                bgBottomColor = Color(0xFF0E0401),
                surfaceColor = Color(0xFF2E1208),
                cardBorderColor = Color(0xFF6D2810),
                accentColor = Color(0xFFFF6E40),
                moodLabel = "บรรยากาศ: เป็นปรปักษ์ & คุกรุ่น (Hostile Friction)",
                moodSubtitle = "ความตึงเครียดคมกริบ พร้อมจะปะทะกันได้ทุกวินาที",
                moodIcon = Icons.Default.LocalFireDepartment
            )
        }
        // Default Balanced
        else -> {
            EmotionAtmosphere(
                bgTopColor = Color(0xFF0E0A1E),
                bgBottomColor = DeepObsidian,
                surfaceColor = SurfaceDark,
                cardBorderColor = CardBorder,
                accentColor = MysticViolet,
                moodLabel = "บรรยากาศ: ดำเนินเรื่อง & สำรวจจิตใจ",
                moodSubtitle = "โชคชะตากำลังเปิดบทสนทนาใหม่ในความเงียบสงัด",
                moodIcon = Icons.Default.Psychology
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryChatScreen(
    character: CharacterEntity,
    work: WorkEntity?,
    session: ChatSessionEntity?,
    messages: List<ChatMessageEntity>,
    emotions: EmotionState,
    latestDeltas: EmotionDeltas?,
    isLoadingTurn: Boolean,
    isCompacting: Boolean,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit,
    onTriggerCompaction: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var showEmotionSheet by remember { mutableStateOf(false) }
    var showMemorySheet by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    // Dynamic Atmosphere calculation & smooth animated transitions
    val atmosphere = remember(emotions) { getAtmosphereForEmotion(emotions) }
    val animatedBgTop by animateColorAsState(targetValue = atmosphere.bgTopColor, animationSpec = tween(700))
    val animatedBgBottom by animateColorAsState(targetValue = atmosphere.bgBottomColor, animationSpec = tween(700))
    val animatedSurface by animateColorAsState(targetValue = atmosphere.surfaceColor, animationSpec = tween(700))
    val animatedBorder by animateColorAsState(targetValue = atmosphere.cardBorderColor, animationSpec = tween(700))
    val animatedAccent by animateColorAsState(targetValue = atmosphere.accentColor, animationSpec = tween(700))

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size, isLoadingTurn) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickActionSuggestions = listOf(
        "จ้องตากลับอย่างไม่ยอมแพ้",
        "ก้าวเข้าไปใกล้แล้วท้าทายอำนาจ",
        "เสนอข้อตกลงสงบศึกชั่วคราว",
        "ถามถึงความลับต้องห้ามที่ปิดบังไว้",
        "แกล้งทำเป็นยอมจำนนเพื่อลดการระแวง"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(character.avatarColor))
                                .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = character.name.take(1).uppercase(),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = character.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                ),
                                maxLines = 1
                            )
                            Text(
                                text = "${character.title} • ${work?.title ?: "จักรวาล"}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = animatedAccent,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ย้อนกลับ",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    // Export Story Dialog button
                    IconButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.testTag("chat_export_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "ส่งออกเนื้อเรื่อง (.md / .txt)",
                            tint = animatedAccent
                        )
                    }

                    // Toggle Memory Compaction Sheet
                    IconButton(
                        onClick = { showMemorySheet = true },
                        modifier = Modifier.testTag("memory_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = "บันทึกความจำสรุป",
                            tint = CyanNeon
                        )
                    }

                    // Toggle 6-Axis Emotion Sheet
                    IconButton(
                        onClick = { showEmotionSheet = true },
                        modifier = Modifier.testTag("emotion_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "ระบบ 6 มิติอารมณ์",
                            tint = animatedAccent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = animatedSurface)
            )
        },
        bottomBar = {
            Surface(
                color = animatedSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, animatedBorder)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    // Quick Action Prompts
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 6.dp)
                    ) {
                        items(quickActionSuggestions) { suggestion ->
                            SuggestionChip(
                                onClick = {
                                    inputText = "*$suggestion* "
                                },
                                label = {
                                    Text(suggestion, fontSize = 11.sp, color = TextSecondary)
                                },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = CardSurface
                                ),
                                border = SuggestionChipDefaults.suggestionChipBorder(
                                    borderColor = animatedBorder,
                                    enabled = true
                                )
                            )
                        }
                    }

                    // Formatting Helper shortcuts
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = { inputText = "$inputText*การกระทำ* " },
                            color = animatedAccent.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "+ *การกระทำ*",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = animatedAccent,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            onClick = { inputText = "$inputText\"บทพูด\" " },
                            color = CyanNeon.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "+ \"บทพูด\"",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = CyanNeon,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Main Text Input & Send Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = {
                                Text(
                                    "บรรยายฉาก *การกระทำ...* หรือพูด \"บทสนทนา...\"",
                                    fontSize = 13.sp,
                                    color = TextSecondary.copy(alpha = 0.6f)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_field"),
                            maxLines = 4,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = CardSurface,
                                unfocusedContainerColor = CardSurface,
                                focusedBorderColor = animatedAccent,
                                unfocusedBorderColor = animatedBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank() && !isLoadingTurn) {
                                    val textToSend = inputText
                                    inputText = ""
                                    onSendMessage(textToSend)
                                }
                            },
                            enabled = inputText.isNotBlank() && !isLoadingTurn,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (inputText.isNotBlank() && !isLoadingTurn) animatedAccent else CardSurface)
                                .testTag("chat_send_button")
                        ) {
                            if (isLoadingTurn) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "ส่งข้อความ",
                                    tint = if (inputText.isNotBlank()) Color.White else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = animatedBgTop
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(animatedBgTop, animatedBgBottom)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Live Sticky 6-Axis Emotion Bar Header
                EmotionSummaryHeader(
                    emotions = emotions,
                    latestDeltas = latestDeltas,
                    onClickExpand = { showEmotionSheet = true },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )

                // Dynamic Atmosphere Tone Indicator Bar
                Surface(
                    color = animatedSurface.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, animatedBorder.copy(alpha = 0.8f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .clickable { showEmotionSheet = true }
                        .testTag("chat_atmosphere_indicator")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = atmosphere.moodIcon,
                            contentDescription = null,
                            tint = animatedAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = atmosphere.moodLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = animatedAccent,
                                fontSize = 11.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• ${atmosphere.moodSubtitle}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondary,
                                fontSize = 10.sp
                            ),
                            maxLines = 1
                        )
                    }
                }

                // Message list
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        WebtoonMessageItem(
                            message = msg,
                            characterName = character.name,
                            characterAvatarColor = character.avatarColor
                        )
                    }

                    if (isLoadingTurn) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = animatedAccent,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${character.name} กำลังคิดและตอบสนองตามอารมณ์...",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet: Full 6-Axis Emotion Tracker
    if (showEmotionSheet) {
        ModalBottomSheet(
            onDismissRequest = { showEmotionSheet = false },
            containerColor = animatedSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                FullEmotionTrackerCard(emotions = emotions)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal Bottom Sheet: Memory Compaction & Chronicle
    if (showMemorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showMemorySheet = false },
            containerColor = animatedSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                CompactedMemoryCard(
                    compactedMemory = session?.compactedMemory ?: "",
                    totalMessages = messages.size,
                    isCompacting = isCompacting,
                    onTriggerCompaction = onTriggerCompaction
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Export Story Dialog (.md / .txt)
    if (showExportDialog) {
        val activeSession = session ?: ChatSessionEntity(
            id = 0,
            workId = work?.id ?: 0,
            characterId = character.id,
            title = character.name,
            currentAffection = emotions.affection,
            currentObsession = emotions.obsession,
            currentTrust = emotions.trust,
            currentFondness = emotions.fondness,
            currentAversion = emotions.aversion,
            currentFear = emotions.fear
        )
        ExportStoryDialog(
            session = activeSession,
            character = character,
            work = work,
            messages = messages,
            onDismiss = { showExportDialog = false }
        )
    }
}
