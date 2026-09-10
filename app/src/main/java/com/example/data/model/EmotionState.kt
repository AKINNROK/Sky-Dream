package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EmotionState(
    val affection: Int = 10,  // ความรักใคร่ / เสน่หา (0-100)
    val obsession: Int = 5,   // ความยึดติด / ครอบงำ (0-100)
    val trust: Int = 10,      // ความเชื่อใจ / ไว้ใจ (0-100)
    val fondness: Int = 10,   // ความเอ็นดู / ผูกพัน (0-100)
    val aversion: Int = 0,    // ความรังเกียจ / ต่อต้าน (0-100)
    val fear: Int = 0         // ความหวาดกลัว / เกรงขาม (0-100)
) {
    fun clamp(): EmotionState = EmotionState(
        affection = affection.coerceIn(0, 100),
        obsession = obsession.coerceIn(0, 100),
        trust = trust.coerceIn(0, 100),
        fondness = fondness.coerceIn(0, 100),
        aversion = aversion.coerceIn(0, 100),
        fear = fear.coerceIn(0, 100)
    )

    fun calculateDelta(previous: EmotionState): EmotionDeltas {
        return EmotionDeltas(
            deltaAffection = this.affection - previous.affection,
            deltaObsession = this.obsession - previous.obsession,
            deltaTrust = this.trust - previous.trust,
            deltaFondness = this.fondness - previous.fondness,
            deltaAversion = this.aversion - previous.aversion,
            deltaFear = this.fear - previous.fear
        )
    }

    fun getDominantMood(): String {
        return when {
            obsession > 60 && affection > 50 -> "คลั่งรัก & ยึดติดสุดโต่ง (Yandere)"
            fear > 60 && aversion > 40 -> "หวาดผวา & ต่อต้านป้องกันตัว"
            trust > 70 && affection > 60 -> "ผูกพันลึกซึ้ง & ยอมจำนนด้วยใจ"
            aversion > 70 -> "รังเกียจเย็นชา & เหยียดหยาม"
            fear > 65 -> "เกรงขาม & ไม่กล้าขัดขืน"
            obsession > 50 -> "จับจ้องยึดติด & หวงแหน"
            affection > 60 -> "เสน่หา & ใจอ่อนไหว"
            trust > 60 -> "เริ่มเชื่อใจ & ลดกำแพง"
            else -> "สงวนท่าที & เฝ้าสังเกต"
        }
    }
}

data class EmotionDeltas(
    val deltaAffection: Int = 0,
    val deltaObsession: Int = 0,
    val deltaTrust: Int = 0,
    val deltaFondness: Int = 0,
    val deltaAversion: Int = 0,
    val deltaFear: Int = 0
) {
    fun hasAnyChange(): Boolean =
        deltaAffection != 0 || deltaObsession != 0 || deltaTrust != 0 ||
                deltaFondness != 0 || deltaAversion != 0 || deltaFear != 0
}

enum class EmotionAxis(
    val key: String,
    val labelEn: String,
    val labelTh: String,
    val hexColor: Long,
    val description: String
) {
    AFFECTION("affection", "Affection", "เสน่หา/รักใคร่", 0xFFE91E63, "Romantic passion, tenderness, attraction"),
    OBSESSION("obsession", "Obsession", "ยึดติด/ครอบงำ", 0xFF9C27B0, "Possessive fixation, jealousy, unwillingness to part"),
    TRUST("trust", "Trust", "เชื่อใจ/ไว้ใจ", 0xFF2196F3, "Vulnerability, confiding secrets, letting guards down"),
    FONDNESS("fondness", "Fondness", "เอ็นดู/ผูกพัน", 0xFF4CAF50, "Comradely warmth, fond attachment, protectiveness"),
    AVERSION("aversion", "Aversion", "รังเกียจ/ต่อต้าน", 0xFFFF5722, "Disgust, cold disdain, emotional hostility"),
    FEAR("fear", "Fear", "หวาดกลัว/เกรงขาม", 0xFF607D8B, "Anxiety, feeling intimidated or overpowered");

    val color: Color get() = Color(hexColor)
}
