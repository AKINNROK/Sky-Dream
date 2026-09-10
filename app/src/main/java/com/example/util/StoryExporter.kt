package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.CharacterEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ChatSessionEntity
import com.example.data.model.WorkEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ExportFormat {
    MARKDOWN,
    PLAIN_TEXT
}

object StoryExporter {

    fun generateExportContent(
        session: ChatSessionEntity,
        character: CharacterEntity?,
        work: WorkEntity?,
        messages: List<ChatMessageEntity>,
        format: ExportFormat = ExportFormat.MARKDOWN
    ): String {
        val dateFormat = SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("th", "TH"))
        val dateStr = dateFormat.format(Date(session.updatedAt))
        val emotions = session.toEmotionState()

        val charName = character?.name ?: "ตัวละคร"
        val charTitle = character?.title ?: ""
        val workTitle = work?.title ?: "จักรวาลไม่ระบุ"
        val workGenre = work?.genre ?: "เรื่องเล่า"

        return if (format == ExportFormat.MARKDOWN) {
            buildString {
                appendLine("# 🌌 Sky Dream: บันทึกเรื่องเล่า $charName")
                appendLine()
                appendLine("**จักรวาลต้นสังกัด:** $workTitle (แนว: $workGenre)")
                appendLine("**ตัวละคร:** $charName ${if (charTitle.isNotBlank()) "($charTitle)" else ""}")
                appendLine("**วันที่บันทึกล่าสุด:** $dateStr")
                appendLine()
                appendLine("---")
                appendLine("### 📊 สภาวะอารมณ์ 6 มิติ (6-Axis Emotion State)")
                appendLine("- **อารมณ์เด่น:** ${emotions.getDominantMood()}")
                appendLine("- **ความเสน่หา (Affection):** ${emotions.affection}/100")
                appendLine("- **ความยึดติด (Obsession):** ${emotions.obsession}/100")
                appendLine("- **ความเชื่อใจ (Trust):** ${emotions.trust}/100")
                appendLine("- **ความผูกพัน (Fondness):** ${emotions.fondness}/100")
                appendLine("- **ความรังเกียจ (Aversion):** ${emotions.aversion}/100")
                appendLine("- **ความหวาดกลัว (Fear):** ${emotions.fear}/100")
                appendLine()

                if (session.compactedMemory.isNotBlank()) {
                    appendLine("---")
                    appendLine("### 🧠 ความทรงจำเรื่องเล่าที่บีบอัดแล้ว (Compacted Memory Chronicle)")
                    appendLine(session.compactedMemory)
                    appendLine()
                }

                appendLine("---")
                appendLine("### 📜 บทสนทนาและฉากบรรยายเรื่องราว (Dialogue & Narrative)")
                appendLine()

                messages.forEachIndexed { index, msg ->
                    val senderName = if (msg.sender == "user") "ผู้เล่น" else charName
                    val beat = if (!msg.narrativeBeat.isNullOrBlank()) " *[ฉาก: ${msg.narrativeBeat}]*" else ""
                    appendLine("#### **#${index + 1} $senderName**$beat")
                    appendLine()
                    appendLine(msg.content)
                    appendLine()
                }

                appendLine("---")
                appendLine("*ส่งออกโดยแอปพลิเคชัน Sky Dream • Interactive Storytelling & Emotion Simulation*")
            }
        } else {
            // Plain Text
            buildString {
                appendLine("=== SKY DREAM: บันทึกเรื่องเล่า $charName ===")
                appendLine("จักรวาล: $workTitle ($workGenre)")
                appendLine("ตัวละคร: $charName ${if (charTitle.isNotBlank()) "($charTitle)" else ""}")
                appendLine("วันที่: $dateStr")
                appendLine("อารมณ์เด่น: ${emotions.getDominantMood()}")
                appendLine("สถิติอารมณ์: เสน่หา ${emotions.affection} | ยึดติด ${emotions.obsession} | เชื่อใจ ${emotions.trust} | ผูกพัน ${emotions.fondness} | รังเกียจ ${emotions.aversion} | กลัว ${emotions.fear}")
                appendLine("==============================================")
                appendLine()

                if (session.compactedMemory.isNotBlank()) {
                    appendLine("[ความทรงจำเนื้อเรื่องย่อ]")
                    appendLine(session.compactedMemory)
                    appendLine("----------------------------------------------")
                    appendLine()
                }

                messages.forEachIndexed { index, msg ->
                    val senderName = if (msg.sender == "user") "ผู้เล่น" else charName
                    appendLine("[$senderName]")
                    appendLine(msg.content)
                    appendLine()
                }

                appendLine("==============================================")
                appendLine("ส่งออกจากแอปพลิเคชัน Sky Dream")
            }
        }
    }

    fun copyToClipboard(context: Context, text: String, label: String = "Sky Dream Story") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "คัดลอกบทสนทนาลงคลิปบอร์ดแล้ว", Toast.LENGTH_SHORT).show()
    }

    fun shareStory(
        context: Context,
        content: String,
        title: String,
        format: ExportFormat = ExportFormat.MARKDOWN
    ) {
        try {
            val extension = if (format == ExportFormat.MARKDOWN) "md" else "txt"
            val mimeType = if (format == ExportFormat.MARKDOWN) "text/markdown" else "text/plain"
            val sanitizedTitle = title.replace("[^a-zA-Z0-9ก-๙_\\-]".toRegex(), "_").take(30)
            val fileName = "SkyDream_${sanitizedTitle}_${System.currentTimeMillis()}.$extension"

            val cachePath = File(context.cacheDir, "exports")
            cachePath.mkdirs()
            val file = File(cachePath, fileName)
            FileOutputStream(file).use { output ->
                output.write(content.toByteArray(Charsets.UTF_8))
            }

            val fileUri = try {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } catch (e: Exception) {
                null
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_SUBJECT, "Sky Dream Story: $title")
                putExtra(Intent.EXTRA_TEXT, content)
                if (fileUri != null) {
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }

            val chooser = Intent.createChooser(intent, "ส่งออกบทสนทนา Sky Dream ($extension)")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            // Fallback to simple text share
            val simpleIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Sky Dream Story: $title")
                putExtra(Intent.EXTRA_TEXT, content)
            }
            context.startActivity(Intent.createChooser(simpleIntent, "แชร์บทสนทนา"))
        }
    }
}
