package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppScreen
import com.example.ui.StoryViewModel
import com.example.ui.screens.CharacterDetailScreen
import com.example.ui.screens.ChatHistoryScreen
import com.example.ui.screens.CreateCharacterCardScreen
import com.example.ui.screens.CreateCharacterDialog
import com.example.ui.screens.CreateWorkDialog
import com.example.ui.screens.SettingsDialog
import com.example.ui.screens.StoryChatScreen
import com.example.ui.screens.WorkDetailScreen
import com.example.ui.screens.WorkExplorerScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                WhifStoryApp()
            }
        }
    }
}

@Composable
fun WhifStoryApp(
    viewModel: StoryViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val allWorks by viewModel.allWorks.collectAsState()
    val allCharacters by viewModel.allCharacters.collectAsState()
    val selectedWork by viewModel.selectedWork.collectAsState()
    val charactersForWork by viewModel.charactersForSelectedWork.collectAsState()
    val selectedCharacter by viewModel.selectedCharacter.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()
    val activeMessages by viewModel.activeMessages.collectAsState()
    val currentEmotions by viewModel.currentEmotions.collectAsState()
    val latestDeltas by viewModel.latestDeltas.collectAsState()
    val isLoadingTurn by viewModel.isLoadingTurn.collectAsState()
    val isCompacting by viewModel.isCompacting.collectAsState()

    var showCreateWorkDialog by remember { mutableStateOf(false) }
    var showCreateCharacterDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // System back button handling
    BackHandler(enabled = currentScreen != AppScreen.WORK_EXPLORER) {
        when (currentScreen) {
            AppScreen.STORY_CHAT -> {
                if (selectedCharacter != null) {
                    viewModel.navigateTo(AppScreen.CHARACTER_DETAIL)
                } else if (selectedWork != null) {
                    viewModel.navigateTo(AppScreen.WORK_DETAIL)
                } else {
                    viewModel.navigateTo(AppScreen.WORK_EXPLORER)
                }
            }
            AppScreen.CHARACTER_DETAIL -> {
                if (selectedWork != null) {
                    viewModel.navigateTo(AppScreen.WORK_DETAIL)
                } else {
                    viewModel.navigateTo(AppScreen.WORK_EXPLORER)
                }
            }
            AppScreen.WORK_DETAIL -> viewModel.navigateTo(AppScreen.WORK_EXPLORER)
            AppScreen.CREATE_CHARACTER_CARD -> {
                if (selectedWork != null) {
                    viewModel.navigateTo(AppScreen.WORK_DETAIL)
                } else {
                    viewModel.navigateTo(AppScreen.WORK_EXPLORER)
                }
            }
            AppScreen.CHAT_HISTORY -> viewModel.navigateTo(AppScreen.WORK_EXPLORER)
            AppScreen.WORK_EXPLORER -> {}
        }
    }

    when (currentScreen) {
        AppScreen.WORK_EXPLORER -> {
            WorkExplorerScreen(
                works = allWorks,
                allCharacters = allCharacters,
                onSelectWork = { work -> viewModel.selectWork(work) },
                onSelectCharacter = { char -> viewModel.selectCharacter(char) },
                onOpenCreateWork = { showCreateWorkDialog = true },
                onOpenSettings = { showSettingsDialog = true },
                onOpenCreateCharacterCard = { viewModel.navigateTo(AppScreen.CREATE_CHARACTER_CARD) },
                onOpenChatHistory = { viewModel.navigateTo(AppScreen.CHAT_HISTORY) },
                savedSessionCount = allSessions.size
            )
        }

        AppScreen.WORK_DETAIL -> {
            val work = selectedWork
            if (work != null) {
                WorkDetailScreen(
                    work = work,
                    characters = charactersForWork,
                    onBack = { viewModel.navigateTo(AppScreen.WORK_EXPLORER) },
                    onSelectCharacter = { char -> viewModel.selectCharacter(char) },
                    onOpenCreateCharacter = { viewModel.navigateTo(AppScreen.CREATE_CHARACTER_CARD) },
                    onDeleteWork = { workId -> viewModel.deleteWork(workId) }
                )
            } else {
                viewModel.navigateTo(AppScreen.WORK_EXPLORER)
            }
        }

        AppScreen.CHARACTER_DETAIL -> {
            val char = selectedCharacter
            if (char != null) {
                CharacterDetailScreen(
                    character = char,
                    work = selectedWork,
                    onBack = {
                        if (selectedWork != null) viewModel.navigateTo(AppScreen.WORK_DETAIL)
                        else viewModel.navigateTo(AppScreen.WORK_EXPLORER)
                    },
                    onStartStory = { targetChar -> viewModel.startStoryWithCharacter(targetChar) },
                    onDeleteCharacter = { charId -> viewModel.deleteCharacter(charId) }
                )
            } else {
                viewModel.navigateTo(AppScreen.WORK_EXPLORER)
            }
        }

        AppScreen.STORY_CHAT -> {
            val char = selectedCharacter
            if (char != null) {
                StoryChatScreen(
                    character = char,
                    work = selectedWork,
                    session = activeSession,
                    messages = activeMessages,
                    emotions = currentEmotions,
                    latestDeltas = latestDeltas,
                    isLoadingTurn = isLoadingTurn,
                    isCompacting = isCompacting,
                    onBack = { viewModel.navigateTo(AppScreen.CHARACTER_DETAIL) },
                    onSendMessage = { text -> viewModel.sendPlayerTurn(text) },
                    onTriggerCompaction = { viewModel.triggerMemoryCompaction() }
                )
            } else {
                viewModel.navigateTo(AppScreen.WORK_EXPLORER)
            }
        }

        AppScreen.CREATE_CHARACTER_CARD -> {
            CreateCharacterCardScreen(
                works = allWorks,
                selectedWorkId = selectedWork?.id,
                onBack = {
                    if (selectedWork != null) viewModel.navigateTo(AppScreen.WORK_DETAIL)
                    else viewModel.navigateTo(AppScreen.WORK_EXPLORER)
                },
                onSaveCharacterCard = { card ->
                    viewModel.saveCharacterCard(card)
                }
            )
        }

        AppScreen.CHAT_HISTORY -> {
            ChatHistoryScreen(
                sessions = allSessions,
                characters = allCharacters,
                works = allWorks,
                onBack = { viewModel.navigateTo(AppScreen.WORK_EXPLORER) },
                onResumeSession = { session -> viewModel.resumeSession(session) },
                onCompactSession = { sessionId -> viewModel.compactChatMemory(sessionId) },
                onDeleteSession = { sessionId -> viewModel.deleteSession(sessionId) }
            )
        }
    }

    // Dialogs
    if (showCreateWorkDialog) {
        CreateWorkDialog(
            onDismiss = { showCreateWorkDialog = false },
            onConfirm = { title, genre, summary, worldviewDoc, tags ->
                viewModel.createWork(title, genre, summary, worldviewDoc, tags)
                showCreateWorkDialog = false
                viewModel.navigateTo(AppScreen.WORK_DETAIL)
            }
        )
    }

    if (showCreateCharacterDialog && selectedWork != null) {
        CreateCharacterDialog(
            workTitle = selectedWork!!.title,
            onDismiss = { showCreateCharacterDialog = false },
            onConfirm = { name, title, persona, firstMessage, exampleDialogue, speechStyle, scenario,
                          affection, obsession, trust, fondness, aversion, fear ->
                viewModel.createCharacter(
                    workId = selectedWork!!.id,
                    name = name,
                    title = title,
                    persona = persona,
                    firstMessage = firstMessage,
                    exampleDialogue = exampleDialogue,
                    speechStyle = speechStyle,
                    scenario = scenario,
                    initialAffection = affection,
                    initialObsession = obsession,
                    initialTrust = trust,
                    initialFondness = fondness,
                    initialAversion = aversion,
                    initialFear = fear
                )
                showCreateCharacterDialog = false
            }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            currentApiKey = viewModel.customApiKey,
            currentEndpoint = viewModel.customEndpoint,
            onDismiss = { showSettingsDialog = false },
            onSave = { apiKey, endpoint ->
                viewModel.saveApiSettings(apiKey, endpoint)
                showSettingsDialog = false
            }
        )
    }
}
