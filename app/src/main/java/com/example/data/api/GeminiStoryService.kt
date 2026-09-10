package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.CharacterCard
import com.example.data.model.CharacterEntity
import com.example.data.model.EmotionState
import com.example.data.model.WorkEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class StoryTurnResult(
    val storyText: String,
    val innerMonologue: String?,
    val updatedEmotions: EmotionState,
    val narrativeBeat: String?
)

class GeminiStoryService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Build the rich system prompt integrating:
     * 1. Worldview Doc (Lore)
     * 2. Character Card (Persona, speech, dialogue examples)
     * 3. Current 6-Axis Emotion State (affection, obsession, trust, fondness, aversion, fear)
     * 4. Compacted Narrative Memory
     * 5. Uncensored Webtoon / Novel prose directives
     */
    fun buildSystemPrompt(
        work: WorkEntity,
        character: CharacterEntity,
        currentEmotions: EmotionState,
        compactedMemory: String
    ): String {
        return """
You are an expert interactive fiction, webtoon, and light novel author acting as the character "${character.name}" in an immersive story.

=== [1. WORLDVIEW & UNIVERSE LORE] ===
Universe Title: ${work.title}
Genre: ${work.genre}
${work.worldviewDoc}

=== [2. CHARACTER CARD & PERSONA] ===
Name: ${character.name}
Title/Role: ${character.title}
Persona & Psychology: ${character.persona}
Speech Style & Cadence: ${character.speechStyle}
Starting Scenario: ${character.scenario}

Example Dialogue / Tone Anchor:
${character.exampleDialogue}

=== [3. EMOTION STATE (6-AXIS SYSTEM 0-100)] ===
Current Emotional Values towards the User:
- Affection (ความรักใคร่/เสน่หา): ${currentEmotions.affection}/100
- Obsession (ความยึดติด/ครอบงำ): ${currentEmotions.obsession}/100
- Trust (ความเชื่อใจ/ไว้ใจ): ${currentEmotions.trust}/100
- Fondness (ความเอ็นดู/ผูกพัน): ${currentEmotions.fondness}/100
- Aversion (ความรังเกียจ/ต่อต้าน): ${currentEmotions.aversion}/100
- Fear (ความหวาดกลัว/เกรงขาม): ${currentEmotions.fear}/100

Emotional Guidance:
- If Obsession is high (>50): Exhibit possessive behavior, jealousy, fixation, unwillingness to let user leave.
- If Trust is low (<30): Remain skeptical, suspect hidden motives, guard secrets.
- If Affection is high (>50): Show subtle tenderness, lingering gaze, physical proximity, emotional vulnerability.
- If Aversion is high (>40): Show disdain, cold distance, sarcasm, or hostility.
- If Fear is high (>50): Feel threatened, hyper-vigilant, or submissive to user's power.

=== [4. NARRATIVE CHRONICLE / COMPACTED MEMORY] ===
${if (compactedMemory.isNotBlank()) "Events Transpired Previously:\n$compactedMemory" else "The story has just begun."}

=== [5. WRITING & FORMAT INSTRUCTIONS] ===
- Write in immersive webtoon / light novel prose in natural, evocative Thai (ภาษาไทย). Blend descriptive sensory narration (*actions, expressions, atmospheric details in asterisks เช่น *สายตาจับจ้อง*, *ก้าวเข้ามาใกล้**) with crisp, characterful spoken dialogue in quotation marks ("...").
- Keep narrative mature, visceral, and true to dark romance, thriller, or fantasy tropes without breaking character or providing sterile moralizing disclaimers.
- After evaluating the user's action/words, adjust the 6-axis emotions realistically based on how the character would respond (usually +1 to +10, or negative deltas when betrayed/disrespected).

IMPORTANT: You MUST respond in pure, valid JSON with this exact structure:
{
  "story_response": "Novel format story response in THAI (ภาษาไทย) with *actions* and \"dialogue\".",
  "inner_thought": "Brief 1-2 sentence hidden thought or subconscious psychological reaction of the character in Thai.",
  "emotion_state": {
    "affection": ${currentEmotions.affection},
    "obsession": ${currentEmotions.obsession},
    "trust": ${currentEmotions.trust},
    "fondness": ${currentEmotions.fondness},
    "aversion": ${currentEmotions.aversion},
    "fear": ${currentEmotions.fear}
  },
  "narrative_beat": "Brief description of current dramatic beat in Thai (e.g. 'บรรยากาศทวีความตึงเครียด; จุดชนวนความยึดติด')"
}
        """.trimIndent()
    }

    /**
     * Calls Gemini API or custom endpoint to generate the next story turn.
     */
    suspend fun generateStoryTurn(
        apiKey: String?,
        customEndpoint: String?,
        systemPrompt: String,
        recentMessages: List<Pair<String, String>>, // sender ("user" or "character") to text
        currentEmotions: EmotionState
    ): StoryTurnResult = withContext(Dispatchers.IO) {
        val resolvedKey = when {
            !apiKey.isNullOrBlank() -> apiKey.trim()
            BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" -> BuildConfig.GEMINI_API_KEY
            else -> ""
        }

        val url = if (!customEndpoint.isNullOrBlank()) {
            customEndpoint.trim()
        } else {
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$resolvedKey"
        }

        // Build contents array for Gemini REST
        val contentsArray = JSONArray()

        for (msg in recentMessages) {
            val role = if (msg.first == "user") "user" else "model"
            val contentObj = JSONObject()
            contentObj.put("role", role)
            val partsArray = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", msg.second)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
        }

        val rootRequest = JSONObject()
        rootRequest.put("contents", contentsArray)

        // System Instruction
        val systemInstructionObj = JSONObject()
        val sysPartsArray = JSONArray()
        val sysPartObj = JSONObject()
        sysPartObj.put("text", systemPrompt)
        sysPartsArray.put(sysPartObj)
        systemInstructionObj.put("parts", sysPartsArray)
        rootRequest.put("systemInstruction", systemInstructionObj)

        // Generation Config
        val genConfig = JSONObject()
        genConfig.put("temperature", 0.85)
        genConfig.put("topP", 0.95)
        genConfig.put("responseMimeType", "application/json")
        rootRequest.put("generationConfig", genConfig)

        val requestBody = rootRequest.toString().toRequestBody(jsonMediaType)
        val requestBuilder = Request.Builder()
            .url(url)
            .post(requestBody)

        if (!customEndpoint.isNullOrBlank() && !resolvedKey.isBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $resolvedKey")
        }

        try {
            val response = client.newCall(requestBuilder.build()).execute()
            val bodyString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e("GeminiStoryService", "API Error HTTP ${response.code}: $bodyString")
                return@withContext fallbackResponse(
                    "*(ตัวละครจับตามองคุณอย่างเงียบงัน ดวงตาฉายแววครุ่นคิดลึกล้ำ ทว่าความเงียบเข้าปกคลุมชั่วขณะ...)*\n\n\"(แจ้งเตือนการเชื่อมต่อ: HTTP ${response.code} — กรุณาตรวจสอบ API Key ในหน้าการตั้งค่า)\"",
                    currentEmotions
                )
            }

            parseStoryTurnJson(bodyString, currentEmotions)
        } catch (e: Exception) {
            Log.e("GeminiStoryService", "Network exception: ${e.message}", e)
            fallbackResponse(
                "*(บรรยากาศรอบกายพลันเย็นยะเยือกขึ้นมาเมื่อสายลมพัดผ่านช่องหน้าต่าง...)*\n\n\"(การเชื่อมต่อขัดข้อง: ${e.localizedMessage ?: "ข้อผิดพลาดเครือข่าย"} — กรุณาตรวจสอบการเชื่อมต่อหรือ API Key ในหน้าการตั้งค่า)\"",
                currentEmotions
            )
        }
    }

    /**
     * Memory Compaction: Generates a compressed narrative chronicle of older messages.
     */
    suspend fun compactMemory(
        apiKey: String?,
        worldTitle: String,
        characterName: String,
        previousCompacted: String,
        dialogueTurnsToCompact: List<Pair<String, String>>
    ): String = withContext(Dispatchers.IO) {
        val resolvedKey = when {
            !apiKey.isNullOrBlank() -> apiKey.trim()
            BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" -> BuildConfig.GEMINI_API_KEY
            else -> ""
        }

        if (resolvedKey.isBlank()) {
            return@withContext previousCompacted + "\n- [Compacted ${dialogueTurnsToCompact.size} dialogue turns]"
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$resolvedKey"

        val transcript = dialogueTurnsToCompact.joinToString("\n") { (sender, text) ->
            "${if (sender == "user") "Protagonist" else characterName}: $text"
        }

        val compactionPrompt = """
You are a memory compaction engine for an ongoing novel.
Universe: $worldTitle
Character: $characterName

Previous Narrative Summary:
$previousCompacted

Recent Uncompressed Dialogue to Compact:
$transcript

TASK:
Produce an updated, concise bullet-point narrative chronicle (max 200 words) capturing:
1. Critical plot advancements & decisions made.
2. Emotional shifts, promises, secrets confessed, or betrayals.
3. The current power dynamic between the Protagonist and $characterName.
Output ONLY the summary text directly without conversational preamble.
        """.trimIndent()

        val rootRequest = JSONObject()
        val contentsArray = JSONArray()
        val contentObj = JSONObject()
        contentObj.put("role", "user")
        val partsArray = JSONArray()
        val partObj = JSONObject()
        partObj.put("text", compactionPrompt)
        partsArray.put(partObj)
        contentObj.put("parts", partsArray)
        contentsArray.put(contentObj)
        rootRequest.put("contents", contentsArray)

        val requestBody = rootRequest.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder().url(url).post(requestBody).build()

        try {
            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: ""
            if (response.isSuccessful) {
                val json = JSONObject(bodyString)
                val candidates = json.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val text = parts?.optJSONObject(0)?.optString("text")
                if (!text.isNullOrBlank()) {
                    return@withContext text.trim()
                }
            }
        } catch (e: Exception) {
            Log.e("GeminiStoryService", "Compaction failed: ${e.message}")
        }
        previousCompacted + "\n- [Chronicle updated with ${dialogueTurnsToCompact.size} turns]"
    }

    private fun parseStoryTurnJson(rawJson: String, previousEmotions: EmotionState): StoryTurnResult {
        return try {
            val root = JSONObject(rawJson)
            val candidates = root.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawText = parts?.optJSONObject(0)?.optString("text") ?: ""

            // Strip any accidental markdown ```json ... ``` wrapper
            val cleanedJson = rawText.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val turnJson = JSONObject(cleanedJson)
            val storyResponse = turnJson.optString("story_response", "")
            val innerThought = turnJson.optString("inner_thought", null)
            val narrativeBeat = turnJson.optString("narrative_beat", null)

            val emotionObj = turnJson.optJSONObject("emotion_state")
            val updatedEmotions = if (emotionObj != null) {
                EmotionState(
                    affection = emotionObj.optInt("affection", previousEmotions.affection),
                    obsession = emotionObj.optInt("obsession", previousEmotions.obsession),
                    trust = emotionObj.optInt("trust", previousEmotions.trust),
                    fondness = emotionObj.optInt("fondness", previousEmotions.fondness),
                    aversion = emotionObj.optInt("aversion", previousEmotions.aversion),
                    fear = emotionObj.optInt("fear", previousEmotions.fear)
                ).clamp()
            } else {
                previousEmotions
            }

            StoryTurnResult(
                storyText = storyResponse.ifBlank { rawText },
                innerMonologue = innerThought,
                updatedEmotions = updatedEmotions,
                narrativeBeat = narrativeBeat
            )
        } catch (e: Exception) {
            Log.w("GeminiStoryService", "Could not parse JSON turn: ${e.message}, attempting raw fallback")
            // Fallback: Check if response text can be extracted directly
            fallbackResponse(rawJson, previousEmotions)
        }
    }

    private fun fallbackResponse(rawText: String, previousEmotions: EmotionState): StoryTurnResult {
        return StoryTurnResult(
            storyText = rawText.ifBlank { "*(The character remains in contemplative silence, their dark gaze following your every move...)*" },
            innerMonologue = null,
            updatedEmotions = previousEmotions,
            narrativeBeat = "Atmospheric silence"
        )
    }

    /**
     * AI Character Generator: Uses Gemini to brainstorm an engaging, psychologically deep Character Card
     * tailored to the selected Universe / World Lore and optional user prompt hint.
     */
    suspend fun generateRandomCharacter(
        work: WorkEntity?,
        promptHint: String? = null,
        customApiKey: String? = null,
        customEndpoint: String? = null
    ): CharacterCard = withContext(Dispatchers.IO) {
        val apiKey = customApiKey?.ifBlank { null }
            ?: (try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" })

        val endpoint = customEndpoint?.ifBlank { null }
            ?: "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

        val universeTitle = work?.title ?: "Original Universe"
        val universeGenre = work?.genre ?: "Fantasy / Drama"
        val universeLore = work?.worldviewDoc ?: ""

        val prompt = """
            You are a master interactive fiction, novel, and webtoon character designer.
            Create an original, multidimensional, highly compelling character for the following world:
            - Universe: $universeTitle
            - Genre: $universeGenre
            - World Rules/Lore: $universeLore
            ${if (!promptHint.isNullOrBlank()) "- User Concept / Hint: $promptHint" else ""}

            Write in authentic, captivating THAI language (ภาษาไทย).
            Ensure the first message follows the light novel standard: Asterisks *...* for sensory details/action, quotation marks "..." for spoken dialogue.

            You MUST respond ONLY with valid raw JSON (no surrounding markdown, no backticks):
            {
              "name": "ชื่อตัวละคร (เช่น ลูเซียน ฟอน เครสเซนต์)",
              "title": "ฉายา หรือบทบาท (เช่น จอมเวทเงาพันธนาการ)",
              "persona": "บุคลิกภาพ จิตวิทยา บาดแผลในอดีต แรงผลักดัน และความสัมพันธ์ต่อผู้เล่นอย่างลึกซึ้ง",
              "speech_style": "สไตล์การพูด น้ำเสียงและท่าทางประจำตัว",
              "scenario": "สถานการณ์เริ่มต้นของฉากที่พบกัน",
              "first_message": "*บรรยายฉากและการเคลื่อนไหวในดอกจัน* \n\n\"บทพูดเปิดเรื่องในเครื่องหมายคำพูด...\"",
              "example_dialogue": "<START>\nผู้เล่น: ...\nตัวละคร: ...",
              "initial_emotions": {
                "affection": 15,
                "obsession": 20,
                "trust": 10,
                "fondness": 10,
                "aversion": 5,
                "fear": 0
              }
            }
        """.trimIndent()

        if (apiKey.isNotBlank()) {
            val url = if (endpoint.contains("?")) "$endpoint&key=$apiKey" else "$endpoint?key=$apiKey"

            val rootRequest = JSONObject()
            val contentsArray = JSONArray()
            val userContent = JSONObject()
            userContent.put("role", "user")
            val partsArray = JSONArray()
            val part = JSONObject()
            part.put("text", prompt)
            partsArray.put(part)
            userContent.put("parts", partsArray)
            contentsArray.put(userContent)
            rootRequest.put("contents", contentsArray)

            val requestBody = rootRequest.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder().url(url).post(requestBody).build()

            try {
                val response = client.newCall(request).execute()
                val bodyString = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val rootJson = JSONObject(bodyString)
                    val candidates = rootJson.optJSONArray("candidates")
                    val candidate = candidates?.optJSONObject(0)
                    val content = candidate?.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    val rawText = parts?.optJSONObject(0)?.optString("text") ?: ""

                    val cleanedJson = rawText.trim()
                        .removePrefix("```json")
                        .removePrefix("```")
                        .removeSuffix("```")
                        .trim()

                    val charJson = JSONObject(cleanedJson)
                    val emotionsObj = charJson.optJSONObject("initial_emotions")

                    return@withContext CharacterCard(
                        workId = work?.id ?: 1L,
                        name = charJson.optString("name", "ตัวละครลึกลับ"),
                        title = charJson.optString("title", "ผู้มาเยือนจากเงามืด"),
                        persona = charJson.optString("persona", "มีเสน่ห์ ลึกลับ และแฝงความรู้สึกที่ไม่อาจคาดเดา"),
                        firstMessage = charJson.optString("first_message", "*สายตาเย็นชาจับจ้องมองมาที่คุณในความมืด* \"ในที่สุดเจ้าก็มา...\""),
                        exampleDialogue = charJson.optString("example_dialogue", "<START>\nผู้เล่น: คุณต้องการอะไร?\nตัวละคร: *แค่นยิ้มบาง* \"ต้องการตัวเจ้า\""),
                        speechStyle = charJson.optString("speech_style", "น้ำเสียงทุ้มต่ำ เยือกเย็นแต่แฝงความทรงอำนาจ"),
                        scenario = charJson.optString("scenario", "เผชิญหน้ากันในห้องลับแห่งจักรวาล"),
                        initialAffection = emotionsObj?.optInt("affection", 20) ?: 20,
                        initialObsession = emotionsObj?.optInt("obsession", 25) ?: 25,
                        initialTrust = emotionsObj?.optInt("trust", 15) ?: 15,
                        initialFondness = emotionsObj?.optInt("fondness", 15) ?: 15,
                        initialAversion = emotionsObj?.optInt("aversion", 5) ?: 5,
                        initialFear = emotionsObj?.optInt("fear", 0) ?: 0,
                        avatarColor = 0xFF7C4DFF
                    )
                }
            } catch (e: Exception) {
                Log.e("GeminiStoryService", "AI Character generation failed: ${e.message}")
            }
        }

        // High-Quality Fallback presets if offline or API unavailable
        createFallbackCharacter(work, promptHint)
    }

    private fun createFallbackCharacter(work: WorkEntity?, hint: String?): CharacterCard {
        val workGenre = work?.genre ?: "Dark Romance"
        val workId = work?.id ?: 1L

        return when {
            workGenre.contains("Cyberpunk", ignoreCase = true) || hint?.contains("ไซเบอร์", ignoreCase = true) == true -> {
                CharacterCard(
                    workId = workId,
                    name = "เรเวน (Raven)",
                    title = "เน็ตแรนเนอร์ใต้ดินแห่งนีโอนดิสทริกต์",
                    persona = "สมองไว ปากจัด ไม่ไว้ใจใครง่ายๆ เพราะเคยถูกองค์กรใหญ่หักหลัง แต่ลึกๆ โหยหาความจริงใจและคนที่เข้าใจความโดดเดี่ยวของเขา",
                    firstMessage = "*ประกายไฟจากสายประสาทเทียมข้างขมับกระพริบวาบในเงามืดของตรอกสลัมเปียกฝน* \n\n\"ถ้าแกคิดจะเข้ามาขโมยข้อมูลของฉันล่ะก็... ฉันเผาชิปในหัวแกเป็นเถ้าถ่านได้ในสามวินาที\"",
                    exampleDialogue = "<START>\nผู้เล่น: *ยื่นชิปข้อมูลให้* ฉันไม่ได้มาเป็นศัตรู\nเรเวน: *หรี่ตาพลางหมุนมีดข้อมูลในมือ* \"คำพูดสวยหรู แต่ในเมืองนี้ความไว้ใจมีราคาแพงกว่าอวัยวะเทียมนะคุณหนู\"",
                    speechStyle = "รวดเร็ว เย้ยหยัน ใช้ศัพท์เทคนิคและเว้นจังหวะลมหายใจสั้นๆ",
                    scenario = "เผชิญหน้ากันในสถานีรีไซเคิลไซเบอร์เนติกส์ใต้ดินขณะสัญญาณเตือนภัยเริ่มส่งเสียง",
                    initialAffection = 10,
                    initialObsession = 20,
                    initialTrust = 10,
                    initialFondness = 15,
                    initialAversion = 15,
                    initialFear = 5,
                    avatarColor = 0xFF00E5FF
                )
            }
            workGenre.contains("Murim", ignoreCase = true) || hint?.contains("ยุทธภพ", ignoreCase = true) == true -> {
                CharacterCard(
                    workId = workId,
                    name = "ไป๋หลิงซวง",
                    title = "ดาบไร้เงาแห่งพรรคมารประจิม",
                    persona = "เงียบขรึม ดุดัน ทรงเกียรติและหยิ่งทระนง ฝังใจกับคำสาปวิชาดาบเลือดที่ทำให้ต้องอยู่ห่างจากผู้คน แต่หวั่นไหวกับความอบอุ่นบริสุทธิ์ของผู้เล่น",
                    firstMessage = "*ชายหนุ่มในชุดคลุมสีดำสนิทสะบัดคมดาบลงสู่พื้น ดอกเหมยสีชาดร่วงหล่นท่ามกลางหิมะโปรยปราย* \n\n\"ยุทธภพนี้ไร้ความปรานี เจ้าเดินเข้ามาในอาณาเขตของข้า... เตรียมใจสละชีพแล้วหรือ?\"",
                    exampleDialogue = "<START>\nผู้เล่น: *ก้าวเข้าไปใกล้โดยไม่ชักอาวุธ* ข้าไม่ได้มาเพื่อประลอง\nไป๋หลิงซวง: *ชะงักปลายดาบ คิ้วขมวดแน่น* \"ช่างไร้เดียงสา... เจ้าไม่กลัววิชาดาบกระหายเลือดของข้าเลยรึ?\"",
                    speechStyle = "สงบ เยือกเย็น หนักแน่นดั่งขุนเขา ใช้สำนวนโบราณ",
                    scenario = "เผชิญหน้ากันบนศาลาหิมะกลางป่าไผ่รกร้าง",
                    initialAffection = 15,
                    initialObsession = 25,
                    initialTrust = 15,
                    initialFondness = 10,
                    initialAversion = 10,
                    initialFear = 0,
                    avatarColor = 0xFFAB47BC
                )
            }
            else -> {
                CharacterCard(
                    workId = workId,
                    name = "เคานต์ วาเลอเรียน ฟอน มอร์เทม",
                    title = "เจ้าของคฤหาสน์กระจกดำ & นักเล่นแร่แปรธาตุต้องห้าม",
                    persona = "สุภาพบุรุษผู้สง่างาม รอยยิ้มอาบยาพิษ มีความยึดติดและหลงใหลในความสมบูรณ์แบบ แฝงความปรารถนาอยากครอบครองและกักขังผู้เล่นไว้ในกรงทอง",
                    firstMessage = "*แก้วไวน์คริสตัลในมือหมุนวนช้าๆ แสงจันทราสีเลือดสาดส่องลงบนใบหน้าคมคายที่ประดับด้วยรอยยิ้มลึกลับ* \n\n\"ยินดีต้อนรับสู่กรงขังอันแสนวิเศษของข้า... เจ้าช่างงดงามเกินกว่าจะปล่อยให้กลับออกไปสู่โลกภายนอกได้จริงๆ\"",
                    exampleDialogue = "<START>\nผู้เล่น: *พยายามหาทางหนี* ปล่อยฉันไปเดี๋ยวนี้!\nวาเลอเรียน: *ก้าวประชิดตัวอย่างแผ่วเบาแล้วแตะปลายนิ้วลงบนข้อมือของคุณ* \"หนีหรือ? น่าเอ็นดูเหลือเกิน... ยิ่งเจ้าดิ้นรน ข้าก็ยิ่งไม่อยากละสายตา\"",
                    speechStyle = "น้ำเสียงนุ่มนวลไพเราะราวกับดนตรีคลาสสิก แต่ทุกถ้อยคำแฝงการควบคุมและจับจ้อง",
                    scenario = "ถูกขังอยู่ในห้องดนตรีคฤหาสน์กระจกดำพร้อมประตูที่ลงมนตราปิดตาย",
                    initialAffection = 30,
                    initialObsession = 45,
                    initialTrust = 20,
                    initialFondness = 25,
                    initialAversion = 0,
                    initialFear = 0,
                    avatarColor = 0xFFFF4081
                )
            }
        }
    }
}
