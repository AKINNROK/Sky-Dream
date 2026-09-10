package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.api.GeminiStoryService
import com.example.data.api.StoryTurnResult
import com.example.data.dao.StoryDao
import com.example.data.model.CharacterCard
import com.example.data.model.CharacterEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ChatSessionEntity
import com.example.data.model.EmotionState
import com.example.data.model.WorkEntity
import kotlinx.coroutines.flow.Flow

class StoryRepository(
    private val dao: StoryDao,
    private val service: GeminiStoryService,
    context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("whif_story_prefs", Context.MODE_PRIVATE)

    fun getApiKey(): String? = prefs.getString("custom_api_key", null)
    fun setApiKey(key: String?) {
        prefs.edit().putString("custom_api_key", key).apply()
    }

    fun getCustomEndpoint(): String? = prefs.getString("custom_endpoint", null)
    fun setCustomEndpoint(endpoint: String?) {
        prefs.edit().putString("custom_endpoint", endpoint).apply()
    }

    // --- Works ---
    fun getAllWorks(): Flow<List<WorkEntity>> = dao.getAllWorks()
    suspend fun getWorkById(workId: Long): WorkEntity? = dao.getWorkById(workId)
    suspend fun insertWork(work: WorkEntity): Long = dao.insertWork(work)
    suspend fun deleteWork(workId: Long) = dao.deleteWork(workId)

    // --- Characters ---
    fun getCharactersForWork(workId: Long): Flow<List<CharacterEntity>> =
        dao.getCharactersForWork(workId)

    fun getAllCharacters(): Flow<List<CharacterEntity>> = dao.getAllCharacters()
    suspend fun getCharacterById(characterId: Long): CharacterEntity? =
        dao.getCharacterById(characterId)

    suspend fun insertCharacter(character: CharacterEntity): Long =
        dao.insertCharacter(character)

    suspend fun deleteCharacter(characterId: Long) = dao.deleteCharacter(characterId)

    // --- Sessions ---
    fun getAllSessions(): Flow<List<ChatSessionEntity>> = dao.getAllSessions()

    fun getSessionsForCharacter(characterId: Long): Flow<List<ChatSessionEntity>> =
        dao.getSessionsForCharacter(characterId)

    suspend fun getSessionById(sessionId: Long): ChatSessionEntity? =
        dao.getSessionById(sessionId)

    suspend fun deleteSession(sessionId: Long) {
        dao.deleteMessagesForSession(sessionId)
        dao.deleteSession(sessionId)
    }

    // Alias methods for CharacterCard and WorldLore
    suspend fun saveCharacterCard(card: CharacterEntity): Long = dao.insertCharacter(card)
    suspend fun saveWorldLore(lore: WorkEntity): Long = dao.insertWork(lore)

    suspend fun getOrCreateSession(character: CharacterEntity): ChatSessionEntity {
        // Check if there is an existing session
        val list = dao.getMessagesListForSession(0) // dummy check
        val session = ChatSessionEntity(
            characterId = character.id,
            workId = character.workId,
            title = "เรื่องเล่ากับ ${character.name}",
            currentAffection = character.initialAffection,
            currentObsession = character.initialObsession,
            currentTrust = character.initialTrust,
            currentFondness = character.initialFondness,
            currentAversion = character.initialAversion,
            currentFear = character.initialFear,
            compactedMemory = "",
            lastCompactedMessageIndex = 0
        )
        val newId = dao.insertSession(session)
        val createdSession = session.copy(id = newId)

        // Seed character's firstMessage as the opening turn
        if (character.firstMessage.isNotBlank()) {
            dao.insertMessage(
                ChatMessageEntity(
                    sessionId = newId,
                    sender = "character",
                    content = character.firstMessage,
                    emotionAffection = character.initialAffection,
                    emotionObsession = character.initialObsession,
                    emotionTrust = character.initialTrust,
                    emotionFondness = character.initialFondness,
                    emotionAversion = character.initialAversion,
                    emotionFear = character.initialFear,
                    narrativeBeat = "ฉากเปิดเรื่อง"
                )
            )
        }
        return createdSession
    }

    // --- Messages ---
    fun getMessagesForSession(sessionId: Long): Flow<List<ChatMessageEntity>> =
        dao.getMessagesForSession(sessionId)

    suspend fun getMessagesListForSession(sessionId: Long): List<ChatMessageEntity> =
        dao.getMessagesListForSession(sessionId)

    /**
     * Send player dialogue/action, update memory compaction if needed, call Gemini,
     * calculate 6-axis emotion deltas, and record character response.
     */
    suspend fun sendMessage(
        sessionId: Long,
        playerText: String
    ): Result<ChatMessageEntity> {
        val session = dao.getSessionById(sessionId)
            ?: return Result.failure(Exception("Session not found"))

        val character = dao.getCharacterById(session.characterId)
            ?: return Result.failure(Exception("Character not found"))

        val work = dao.getWorkById(session.workId)
            ?: return Result.failure(Exception("Work not found"))

        // 1. Insert user message
        val userMsg = ChatMessageEntity(
            sessionId = sessionId,
            sender = "user",
            content = playerText
        )
        dao.insertMessage(userMsg)

        // 2. Fetch all messages in this session
        val allMessages = dao.getMessagesListForSession(sessionId)

        // 3. Memory Compaction Check (e.g. if more than 10 turns have accumulated since last compaction)
        var currentCompactedMemory = session.compactedMemory
        val uncompactedCount = allMessages.size - session.lastCompactedMessageIndex
        var newLastCompactedIndex = session.lastCompactedMessageIndex

        if (uncompactedCount >= 8) {
            // Compact older slice (keep last 4 turns fresh)
            val turnsToCompact = allMessages.subList(
                session.lastCompactedMessageIndex,
                allMessages.size - 4
            ).map { it.sender to it.content }

            if (turnsToCompact.isNotEmpty()) {
                currentCompactedMemory = service.compactMemory(
                    apiKey = getApiKey(),
                    worldTitle = work.title,
                    characterName = character.name,
                    previousCompacted = currentCompactedMemory,
                    dialogueTurnsToCompact = turnsToCompact
                )
                newLastCompactedIndex = allMessages.size - 4
            }
        }

        // 4. Build prompt & recent context (last 6-8 messages for immediate back-and-forth)
        val currentEmotions = session.toEmotionState()
        val systemPrompt = service.buildSystemPrompt(
            work = work,
            character = character,
            currentEmotions = currentEmotions,
            compactedMemory = currentCompactedMemory
        )

        val recentTurns = allMessages.takeLast(6).map { it.sender to it.content }

        // 5. Call LLM
        val turnResult: StoryTurnResult = service.generateStoryTurn(
            apiKey = getApiKey(),
            customEndpoint = getCustomEndpoint(),
            systemPrompt = systemPrompt,
            recentMessages = recentTurns,
            currentEmotions = currentEmotions
        )

        // 6. Compute 6-axis emotion deltas
        val deltas = turnResult.updatedEmotions.calculateDelta(currentEmotions)

        // 7. Update Session with new emotions and updated memory compaction state
        val updatedSession = session.copy(
            currentAffection = turnResult.updatedEmotions.affection,
            currentObsession = turnResult.updatedEmotions.obsession,
            currentTrust = turnResult.updatedEmotions.trust,
            currentFondness = turnResult.updatedEmotions.fondness,
            currentAversion = turnResult.updatedEmotions.aversion,
            currentFear = turnResult.updatedEmotions.fear,
            compactedMemory = currentCompactedMemory,
            lastCompactedMessageIndex = newLastCompactedIndex,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateSession(updatedSession)

        // 8. Insert character reply
        val charMsg = ChatMessageEntity(
            sessionId = sessionId,
            sender = "character",
            content = turnResult.storyText,
            emotionAffection = turnResult.updatedEmotions.affection,
            emotionObsession = turnResult.updatedEmotions.obsession,
            emotionTrust = turnResult.updatedEmotions.trust,
            emotionFondness = turnResult.updatedEmotions.fondness,
            emotionAversion = turnResult.updatedEmotions.aversion,
            emotionFear = turnResult.updatedEmotions.fear,
            deltaAffection = deltas.deltaAffection,
            deltaObsession = deltas.deltaObsession,
            deltaTrust = deltas.deltaTrust,
            deltaFondness = deltas.deltaFondness,
            deltaAversion = deltas.deltaAversion,
            deltaFear = deltas.deltaFear,
            innerMonologue = turnResult.innerMonologue,
            narrativeBeat = turnResult.narrativeBeat
        )
        val charMsgId = dao.insertMessage(charMsg)
        return Result.success(charMsg.copy(id = charMsgId))
    }

    /**
     * Force immediate memory compaction on demand.
     */
    suspend fun forceMemoryCompaction(sessionId: Long): String {
        val session = dao.getSessionById(sessionId) ?: return ""
        val character = dao.getCharacterById(session.characterId) ?: return ""
        val work = dao.getWorkById(session.workId) ?: return ""
        val allMessages = dao.getMessagesListForSession(sessionId)

        if (allMessages.size <= 2) return session.compactedMemory

        val turnsToCompact = allMessages.take(allMessages.size - 2).map { it.sender to it.content }
        val updatedCompacted = service.compactMemory(
            apiKey = getApiKey(),
            worldTitle = work.title,
            characterName = character.name,
            previousCompacted = session.compactedMemory,
            dialogueTurnsToCompact = turnsToCompact
        )

        dao.updateSession(
            session.copy(
                compactedMemory = updatedCompacted,
                lastCompactedMessageIndex = allMessages.size - 2,
                updatedAt = System.currentTimeMillis()
            )
        )
        return updatedCompacted
    }

    suspend fun generateRandomCharacter(work: WorkEntity?, promptHint: String? = null): CharacterCard {
        return service.generateRandomCharacter(
            work = work,
            promptHint = promptHint,
            customApiKey = getApiKey(),
            customEndpoint = getCustomEndpoint()
        )
    }
}
