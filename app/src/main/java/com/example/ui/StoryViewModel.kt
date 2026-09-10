package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiStoryService
import com.example.data.db.AppDatabase
import com.example.data.model.CharacterCard
import com.example.data.model.CharacterEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ChatSessionEntity
import com.example.data.model.EmotionDeltas
import com.example.data.model.EmotionState
import com.example.data.model.WorkEntity
import com.example.data.model.WorldLore
import com.example.data.repository.StoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    WORK_EXPLORER,
    WORK_DETAIL,
    CHARACTER_DETAIL,
    STORY_CHAT,
    CREATE_CHARACTER_CARD,
    CHAT_HISTORY
}

class StoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StoryRepository

    init {
        val database = AppDatabase.getInstance(application)
        val service = GeminiStoryService()
        repository = StoryRepository(database.storyDao(), service, application)
    }

    private val _currentScreen = MutableStateFlow(AppScreen.WORK_EXPLORER)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // All works in the database
    val allWorks: StateFlow<List<WorkEntity>> = repository.getAllWorks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All saved chat sessions in Room database
    val allSessions: StateFlow<List<ChatSessionEntity>> = repository.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Currently viewed work
    private val _selectedWork = MutableStateFlow<WorkEntity?>(null)
    val selectedWork: StateFlow<WorkEntity?> = _selectedWork.asStateFlow()

    // Characters in the currently selected work
    @OptIn(ExperimentalCoroutinesApi::class)
    val charactersForSelectedWork: StateFlow<List<CharacterEntity>> = _selectedWork
        .flatMapLatest { work ->
            if (work != null) repository.getCharactersForWork(work.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All characters across all universes
    val allCharacters: StateFlow<List<CharacterEntity>> = repository.getAllCharacters()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Currently inspected character
    private val _selectedCharacter = MutableStateFlow<CharacterEntity?>(null)
    val selectedCharacter: StateFlow<CharacterEntity?> = _selectedCharacter.asStateFlow()

    // Active storytelling session
    private val _activeSession = MutableStateFlow<ChatSessionEntity?>(null)
    val activeSession: StateFlow<ChatSessionEntity?> = _activeSession.asStateFlow()

    // Messages in active session
    @OptIn(ExperimentalCoroutinesApi::class)
    val activeMessages: StateFlow<List<ChatMessageEntity>> = _activeSession
        .flatMapLatest { session ->
            if (session != null) repository.getMessagesForSession(session.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current 6-axis emotions
    private val _currentEmotions = MutableStateFlow(EmotionState())
    val currentEmotions: StateFlow<EmotionState> = _currentEmotions.asStateFlow()

    // Turn-by-turn emotion deltas
    private val _latestDeltas = MutableStateFlow<EmotionDeltas?>(null)
    val latestDeltas: StateFlow<EmotionDeltas?> = _latestDeltas.asStateFlow()

    // Loading & Compaction states
    private val _isLoadingTurn = MutableStateFlow(false)
    val isLoadingTurn: StateFlow<Boolean> = _isLoadingTurn.asStateFlow()

    private val _isCompacting = MutableStateFlow(false)
    val isCompacting: StateFlow<Boolean> = _isCompacting.asStateFlow()

    private val _isGeneratingCharacter = MutableStateFlow(false)
    val isGeneratingCharacter: StateFlow<Boolean> = _isGeneratingCharacter.asStateFlow()

    // Settings
    val customApiKey: String? get() = repository.getApiKey()
    val customEndpoint: String? get() = repository.getCustomEndpoint()

    suspend fun generateRandomCharacter(work: WorkEntity?, promptHint: String? = null): CharacterCard {
        _isGeneratingCharacter.value = true
        return try {
            repository.generateRandomCharacter(work, promptHint)
        } finally {
            _isGeneratingCharacter.value = false
        }
    }

    suspend fun getMessagesForSession(sessionId: Long): List<ChatMessageEntity> {
        return repository.getMessagesListForSession(sessionId)
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun selectWork(work: WorkEntity) {
        _selectedWork.value = work
        _currentScreen.value = AppScreen.WORK_DETAIL
    }

    fun selectCharacter(character: CharacterEntity) {
        _selectedCharacter.value = character
        _currentScreen.value = AppScreen.CHARACTER_DETAIL
    }

    fun startStoryWithCharacter(character: CharacterEntity) {
        viewModelScope.launch {
            _selectedCharacter.value = character
            val work = repository.getWorkById(character.workId)
            _selectedWork.value = work

            val session = repository.getOrCreateSession(character)
            _activeSession.value = session
            _currentEmotions.value = session.toEmotionState()
            _latestDeltas.value = null
            _currentScreen.value = AppScreen.STORY_CHAT
        }
    }

    fun sendPlayerTurn(text: String) {
        val session = _activeSession.value ?: return
        if (text.isBlank() || _isLoadingTurn.value) return

        viewModelScope.launch {
            _isLoadingTurn.value = true
            val prevEmotions = _currentEmotions.value

            val result = repository.sendMessage(session.id, text.trim())
            result.onSuccess { charMessage ->
                val newEmotions = EmotionState(
                    affection = charMessage.emotionAffection ?: prevEmotions.affection,
                    obsession = charMessage.emotionObsession ?: prevEmotions.obsession,
                    trust = charMessage.emotionTrust ?: prevEmotions.trust,
                    fondness = charMessage.emotionFondness ?: prevEmotions.fondness,
                    aversion = charMessage.emotionAversion ?: prevEmotions.aversion,
                    fear = charMessage.emotionFear ?: prevEmotions.fear
                )
                _currentEmotions.value = newEmotions
                _latestDeltas.value = newEmotions.calculateDelta(prevEmotions)

                // Refresh session entity
                _activeSession.value = repository.getSessionById(session.id)
            }
            _isLoadingTurn.value = false
        }
    }

    /**
     * Memory Compaction: Summarizes older dialogue history using Gemini into a concise chronicle,
     * saving LLM context tokens and persisting into Room database.
     */
    fun compactChatMemory(sessionId: Long? = _activeSession.value?.id) {
        val targetSessionId = sessionId ?: return
        viewModelScope.launch {
            _isCompacting.value = true
            repository.forceMemoryCompaction(targetSessionId)
            val updated = repository.getSessionById(targetSessionId)
            if (_activeSession.value?.id == targetSessionId) {
                _activeSession.value = updated
            }
            _isCompacting.value = false
        }
    }

    fun triggerMemoryCompaction() {
        compactChatMemory(_activeSession.value?.id)
    }

    /**
     * Resume reading / interacting with a saved chat session from Room database.
     */
    fun resumeSession(session: ChatSessionEntity) {
        viewModelScope.launch {
            val char = repository.getCharacterById(session.characterId)
            val work = repository.getWorkById(session.workId)
            _selectedCharacter.value = char
            _selectedWork.value = work
            _activeSession.value = session
            _currentEmotions.value = session.toEmotionState()
            _latestDeltas.value = null
            _currentScreen.value = AppScreen.STORY_CHAT
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_activeSession.value?.id == sessionId) {
                _activeSession.value = null
            }
        }
    }

    /**
     * Save CharacterCard directly into Room database.
     */
    fun saveCharacterCard(card: CharacterCard) {
        viewModelScope.launch {
            val charId = repository.saveCharacterCard(card)
            val saved = repository.getCharacterById(charId)
            if (saved != null) {
                _selectedCharacter.value = saved
                val work = repository.getWorkById(saved.workId)
                _selectedWork.value = work
                _currentScreen.value = AppScreen.CHARACTER_DETAIL
            }
        }
    }

    fun saveWorldLore(lore: WorldLore) {
        viewModelScope.launch {
            val id = repository.saveWorldLore(lore)
            val saved = repository.getWorkById(id)
            if (saved != null) {
                _selectedWork.value = saved
                _currentScreen.value = AppScreen.WORK_DETAIL
            }
        }
    }

    fun createWork(
        title: String,
        genre: String,
        summary: String,
        worldviewDoc: String,
        tags: String
    ) {
        viewModelScope.launch {
            val newWork = WorkEntity(
                title = title.trim(),
                genre = genre.trim(),
                summary = summary.trim(),
                worldviewDoc = worldviewDoc.trim(),
                tags = tags.trim(),
                bannerColor = 0xFF2A163B
            )
            val id = repository.insertWork(newWork)
            val created = repository.getWorkById(id)
            if (created != null) {
                _selectedWork.value = created
            }
        }
    }

    fun createCharacter(
        workId: Long,
        name: String,
        title: String,
        persona: String,
        firstMessage: String,
        exampleDialogue: String,
        speechStyle: String,
        scenario: String,
        initialAffection: Int,
        initialObsession: Int,
        initialTrust: Int,
        initialFondness: Int,
        initialAversion: Int,
        initialFear: Int
    ) {
        viewModelScope.launch {
            val newChar = CharacterEntity(
                workId = workId,
                name = name.trim(),
                title = title.trim(),
                persona = persona.trim(),
                firstMessage = firstMessage.trim(),
                exampleDialogue = exampleDialogue.trim(),
                speechStyle = speechStyle.trim(),
                scenario = scenario.trim(),
                initialAffection = initialAffection.coerceIn(0, 100),
                initialObsession = initialObsession.coerceIn(0, 100),
                initialTrust = initialTrust.coerceIn(0, 100),
                initialFondness = initialFondness.coerceIn(0, 100),
                initialAversion = initialAversion.coerceIn(0, 100),
                initialFear = initialFear.coerceIn(0, 100),
                avatarColor = 0xFF7C4DFF
            )
            repository.insertCharacter(newChar)
        }
    }

    fun deleteWork(workId: Long) {
        viewModelScope.launch {
            repository.deleteWork(workId)
            _selectedWork.value = null
            _currentScreen.value = AppScreen.WORK_EXPLORER
        }
    }

    fun deleteCharacter(characterId: Long) {
        viewModelScope.launch {
            repository.deleteCharacter(characterId)
            _selectedCharacter.value = null
            _currentScreen.value = AppScreen.WORK_DETAIL
        }
    }

    fun saveApiSettings(apiKey: String?, endpoint: String?) {
        repository.setApiKey(apiKey?.ifBlank { null })
        repository.setCustomEndpoint(endpoint?.ifBlank { null })
    }
}
