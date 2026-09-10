package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CharacterEntity
import com.example.data.model.WorkEntity
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.EmotionAffectionColor
import com.example.ui.theme.MysticViolet
import com.example.ui.theme.RoseCrimson
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.VelvetPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkExplorerScreen(
    works: List<WorkEntity>,
    allCharacters: List<CharacterEntity>,
    onSelectWork: (WorkEntity) -> Unit,
    onSelectCharacter: (CharacterEntity) -> Unit,
    onOpenCreateWork: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCreateCharacterCard: () -> Unit = {},
    onOpenChatHistory: () -> Unit = {},
    savedSessionCount: Int = 0
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Universes (Works), 1: Characters
    var selectedGenre by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    val genres = listOf("All", "Dark Romance", "Cyberpunk", "Murim", "Fantasy")

    val filteredWorks = if (selectedGenre == "All") works else works.filter {
        it.genre.contains(selectedGenre, ignoreCase = true) || it.tags.contains(selectedGenre, ignoreCase = true)
    }

    val searchFilteredWorks = remember(filteredWorks, searchQuery) {
        if (searchQuery.isBlank()) filteredWorks
        else {
            filteredWorks.filter { work ->
                work.title.contains(searchQuery, ignoreCase = true) ||
                work.summary.contains(searchQuery, ignoreCase = true) ||
                work.worldviewDoc.contains(searchQuery, ignoreCase = true) ||
                work.tags.contains(searchQuery, ignoreCase = true) ||
                work.genre.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(VelvetPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoStories,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Sky Dream",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "จักรวาล → ตัวละคร • นิยาย & แชทจำลอง 6 มิติอารมณ์",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MysticViolet,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                },
                actions = {
                    // Chat History action
                    IconButton(
                        onClick = onOpenChatHistory,
                        modifier = Modifier.testTag("chat_history_icon_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (savedSessionCount > 0) {
                                    Badge(
                                        containerColor = VelvetPurple,
                                        contentColor = Color.White
                                    ) {
                                        Text("$savedSessionCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "ประวัติการสนทนา",
                                tint = if (savedSessionCount > 0) CyanNeon else TextSecondary
                            )
                        }
                    }

                    // Create Character Card action
                    IconButton(
                        onClick = onOpenCreateCharacterCard,
                        modifier = Modifier.testTag("create_character_card_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "สร้างการ์ดตัวละคร",
                            tint = EmotionAffectionColor
                        )
                    }

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("settings_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "การตั้งค่า",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark
                )
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Secondary FAB: Create Character Card
                SmallFloatingActionButton(
                    onClick = onOpenCreateCharacterCard,
                    containerColor = MysticViolet,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("create_character_card_fab")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("การ์ดตัวละครใหม่", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }

                // Primary FAB: Create World
                FloatingActionButton(
                    onClick = onOpenCreateWork,
                    containerColor = VelvetPurple,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("create_world_fab")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("สร้างโลกใหม่", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        containerColor = DeepObsidian
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Mode Tabs: Universes vs All Characters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(SurfaceDark, RoundedCornerShape(12.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                TabButton(
                    title = "จักรวาล / ผลงาน (${works.size})",
                    icon = Icons.Default.Public,
                    isSelected = selectedTab == 0,
                    modifier = Modifier.weight(1f)
                ) { selectedTab = 0 }

                TabButton(
                    title = "ตัวละครทั้งหมด (${allCharacters.size})",
                    icon = Icons.Default.Groups,
                    isSelected = selectedTab == 1,
                    modifier = Modifier.weight(1f)
                ) { selectedTab = 1 }
            }

            // Search Bar for WorldLore and Character details
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        if (selectedTab == 0) "ค้นหาเนื้อเรื่อง, กฎจักรวาล, WorldLore, คำสำคัญ..."
                        else "ค้นหาชื่อตัวละคร, บุคลิกภาพ Persona, สไตล์การพูด...",
                        fontSize = 12.sp,
                        color = TextSecondary.copy(alpha = 0.7f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "ค้นหา",
                        tint = MysticViolet,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "ล้างการค้นหา",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("worldlore_search_bar"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CardSurface,
                    unfocusedContainerColor = CardSurface,
                    focusedBorderColor = MysticViolet,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            // Search matches feedback indicator
            if (searchQuery.isNotBlank()) {
                val matchCount = if (selectedTab == 0) searchFilteredWorks.size else {
                    val baseChars = if (selectedGenre == "All") allCharacters else allCharacters.filter { char ->
                        val w = works.find { it.id == char.workId }
                        w?.genre?.contains(selectedGenre, ignoreCase = true) == true || w?.tags?.contains(selectedGenre, ignoreCase = true) == true
                    }
                    baseChars.count {
                        it.name.contains(searchQuery, ignoreCase = true) ||
                        it.title.contains(searchQuery, ignoreCase = true) ||
                        it.persona.contains(searchQuery, ignoreCase = true) ||
                        it.scenario.contains(searchQuery, ignoreCase = true) ||
                        it.speechStyle.contains(searchQuery, ignoreCase = true)
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ผลการค้นหา: พบ $matchCount รายการสำหรับ \"$searchQuery\"",
                        style = MaterialTheme.typography.labelSmall.copy(color = CyanNeon)
                    )
                    Text(
                        text = "ล้าง",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MysticViolet,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.clickable { searchQuery = "" }
                    )
                }
            }

            // Genre Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val genreDisplayNames = mapOf(
                    "All" to "ทั้งหมด",
                    "Dark Romance" to "โรแมนซ์ดาร์ก",
                    "Cyberpunk" to "ไซเบอร์พังก์",
                    "Murim" to "ยุทธภพ",
                    "Fantasy" to "แฟนตาซี"
                )
                items(genres) { genreKey ->
                    val labelText = genreDisplayNames[genreKey] ?: genreKey
                    FilterChip(
                        selected = selectedGenre == genreKey,
                        onClick = { selectedGenre = genreKey },
                        label = { Text(labelText, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VelvetPurple,
                            selectedLabelColor = Color.White,
                            containerColor = CardSurface,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = CardBorder,
                            selectedBorderColor = MysticViolet,
                            enabled = true,
                            selected = selectedGenre == genreKey
                        )
                    )
                }
            }

            if (selectedTab == 0) {
                // List of Works / Universes (Filtered by search & genre)
                if (searchFilteredWorks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = TextSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = if (searchQuery.isNotBlank()) "ไม่พบจักรวาลที่ตรงกับ \"$searchQuery\"" else "ไม่มีจักรวาลในหมวดหมู่นี้",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                            )
                            if (searchQuery.isNotBlank()) {
                                Button(
                                    onClick = { searchQuery = "" },
                                    colors = ButtonDefaults.buttonColors(containerColor = VelvetPurple),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("ล้างการค้นหา", color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(searchFilteredWorks, key = { it.id }) { work ->
                            val charCount = allCharacters.count { it.workId == work.id }
                            WorkCard(
                                work = work,
                                characterCount = charCount,
                                onClick = { onSelectWork(work) }
                            )
                        }
                    }
                }
            } else {
                // List of Characters (Filtered by search & genre)
                val baseChars = if (selectedGenre == "All") allCharacters else allCharacters.filter { char ->
                    val w = works.find { it.id == char.workId }
                    w?.genre?.contains(selectedGenre, ignoreCase = true) == true || w?.tags?.contains(selectedGenre, ignoreCase = true) == true
                }
                val searchFilteredChars = remember(baseChars, searchQuery) {
                    if (searchQuery.isBlank()) baseChars
                    else {
                        baseChars.filter { char ->
                            char.name.contains(searchQuery, ignoreCase = true) ||
                            char.title.contains(searchQuery, ignoreCase = true) ||
                            char.persona.contains(searchQuery, ignoreCase = true) ||
                            char.scenario.contains(searchQuery, ignoreCase = true) ||
                            char.speechStyle.contains(searchQuery, ignoreCase = true)
                        }
                    }
                }

                if (searchFilteredChars.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = TextSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = if (searchQuery.isNotBlank()) "ไม่พบตัวละครที่ตรงกับ \"$searchQuery\"" else "ไม่มีตัวละครในหมวดนี้",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                            )
                            if (searchQuery.isNotBlank()) {
                                Button(
                                    onClick = { searchQuery = "" },
                                    colors = ButtonDefaults.buttonColors(containerColor = VelvetPurple),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("ล้างการค้นหา", color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(searchFilteredChars, key = { it.id }) { char ->
                            val parentWork = works.find { it.id == char.workId }
                            CharacterCardItem(
                                character = char,
                                workTitle = parentWork?.title ?: "Universe",
                                onClick = { onSelectCharacter(char) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) VelvetPurple else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else TextSecondary,
                    fontSize = 12.sp
                )
            )
        }
    }
}

@Composable
fun WorkCard(
    work: WorkEntity,
    characterCount: Int,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val drawableId = remember(work.coverDrawable) {
        if (!work.coverDrawable.isNullOrBlank()) {
            context.resources.getIdentifier(work.coverDrawable, "drawable", context.packageName)
        } else 0
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("work_card_${work.id}"),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column {
            // Header visual or gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(work.bannerColor), SurfaceDark)
                        )
                    )
            ) {
                if (drawableId != 0) {
                    Image(
                        painter = painterResource(id = drawableId),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Dark gradient scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, CardSurface.copy(alpha = 0.95f))
                                )
                            )
                    )
                }

                // Genre & character badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .border(0.5.dp, MysticViolet.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = work.genre,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MysticViolet
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                tint = CyanNeon,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$characterCount ตัวละคร",
                                style = MaterialTheme.typography.labelSmall.copy(color = CyanNeon)
                            )
                        }
                    }
                }
            }

            // Work details
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = work.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = work.summary,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        lineHeight = 18.sp
                    ),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "เอกสารโลกทัศน์: ${work.worldviewDoc.take(50).trim()}...",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextTertiary,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "สำรวจจักรวาล →",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun CharacterCardItem(
    character: CharacterEntity,
    workTitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("character_item_${character.id}"),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(character.avatarColor))
                    .border(1.5.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = character.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = character.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = "${character.title} • $workTitle",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MysticViolet,
                        fontSize = 11.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = character.persona,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 12.sp
                    ),
                    maxLines = 1
                )
            }

            Text(
                text = "เริ่มคุย 💬",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon
                )
            )
        }
    }
}
