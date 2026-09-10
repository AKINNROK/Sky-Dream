package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * WorldLore represents the local universe and lore document.
 * (Stored in Room database table "works")
 */
typealias WorldLore = WorkEntity

/**
 * CharacterCard represents an AI Character profile card with persona, dialog examples, and initial 6-axis emotions.
 * (Stored in Room database table "characters")
 */
typealias CharacterCard = CharacterEntity

/**
 * Represents a "Work" / "WorldLore" universe that contains lore and rules shared by characters.
 */
@Entity(tableName = "works")
data class WorkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val genre: String,
    val summary: String,
    val worldviewDoc: String, // Shared world lore prepended to all characters in this universe
    val coverDrawable: String? = null,
    val bannerColor: Long = 0xFF2A163B,
    val tags: String = "Webtoon,Story,Fantasy",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Represents an AI CharacterCard living inside a specific Work/WorldLore universe.
 */
@Entity(
    tableName = "characters",
    foreignKeys = [
        ForeignKey(
            entity = WorkEntity::class,
            parentColumns = ["id"],
            childColumns = ["workId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workId")]
)
data class CharacterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workId: Long,
    val name: String,
    val title: String,
    val avatarDrawable: String? = null,
    val avatarColor: Long = 0xFF7C4DFF,
    val persona: String,           // Personality, psychological traits, hidden motives
    val firstMessage: String,      // Initial novel scene opener with actions *...* and quotes "..."
    val exampleDialogue: String,   // Sample dialog turns demonstrating cadence and tone
    val speechStyle: String,       // Speech quirks, honorifics, pacing
    val scenario: String,          // Current scene setup
    val initialAffection: Int = 10,
    val initialObsession: Int = 5,
    val initialTrust: Int = 10,
    val initialFondness: Int = 10,
    val initialAversion: Int = 0,
    val initialFear: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toInitialEmotionState(): EmotionState = EmotionState(
        affection = initialAffection,
        obsession = initialObsession,
        trust = initialTrust,
        fondness = initialFondness,
        aversion = initialAversion,
        fear = initialFear
    )
}

/**
 * An active interactive storytelling session with a character.
 */
@Entity(
    tableName = "chat_sessions",
    foreignKeys = [
        ForeignKey(
            entity = CharacterEntity::class,
            parentColumns = ["id"],
            childColumns = ["characterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("characterId")]
)
data class ChatSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val characterId: Long,
    val workId: Long,
    val title: String,
    val currentAffection: Int = 10,
    val currentObsession: Int = 5,
    val currentTrust: Int = 10,
    val currentFondness: Int = 10,
    val currentAversion: Int = 0,
    val currentFear: Int = 0,
    val compactedMemory: String = "", // Compressed narrative chronicle summarizing older dialogue
    val lastCompactedMessageIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toEmotionState(): EmotionState = EmotionState(
        affection = currentAffection,
        obsession = currentObsession,
        trust = currentTrust,
        fondness = currentFondness,
        aversion = currentAversion,
        fear = currentFear
    )
}

/**
 * A turn in the novel / webtoon dialogue and narration.
 */
@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val sender: String, // "user" or "character"
    val content: String,
    val emotionAffection: Int? = null,
    val emotionObsession: Int? = null,
    val emotionTrust: Int? = null,
    val emotionFondness: Int? = null,
    val emotionAversion: Int? = null,
    val emotionFear: Int? = null,
    val deltaAffection: Int? = null,
    val deltaObsession: Int? = null,
    val deltaTrust: Int? = null,
    val deltaFondness: Int? = null,
    val deltaAversion: Int? = null,
    val deltaFear: Int? = null,
    val innerMonologue: String? = null, // Hidden thoughts / subtext of character
    val narrativeBeat: String? = null,  // Current dramatic shift
    val timestamp: Long = System.currentTimeMillis()
)
