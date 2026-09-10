package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.EmotionDeltas
import com.example.data.model.EmotionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Sky Dream", appName)
    }

    @Test
    fun `test emotion state clamping and delta calculation`() {
        val initial = EmotionState(
            affection = 20,
            obsession = 30,
            trust = 15,
            fondness = 25,
            aversion = 5,
            fear = 0
        )

        val updated = EmotionState(
            affection = 35,
            obsession = 45,
            trust = 10,
            fondness = 25,
            aversion = 0,
            fear = 10
        )

        val delta: EmotionDeltas = updated.calculateDelta(initial)
        assertEquals(15, delta.deltaAffection)
        assertEquals(15, delta.deltaObsession)
        assertEquals(-5, delta.deltaTrust)
        assertEquals(0, delta.deltaFondness)
        assertEquals(-5, delta.deltaAversion)
        assertEquals(10, delta.deltaFear)
        assertTrue(delta.hasAnyChange())
    }

    @Test
    fun `test dominant mood detection`() {
        val obsessiveState = EmotionState(obsession = 85, affection = 60)
        assertEquals("คลั่งรัก & ยึดติดสุดโต่ง (Yandere)", obsessiveState.getDominantMood())

        val fearfulState = EmotionState(fear = 75, aversion = 60)
        assertEquals("หวาดผวา & ต่อต้านป้องกันตัว", fearfulState.getDominantMood())
    }

    @Test
    fun `test CharacterCard and WorldLore entity mappings`() {
        val lore = com.example.data.model.WorldLore(
            id = 1L,
            title = "หอคอยเวทมนตร์นิรันดร์",
            genre = "Fantasy",
            summary = "เรื่องราวแห่งเวทมนตร์",
            worldviewDoc = "กฎแห่งมานา",
            tags = "Magic, Academy"
        )
        assertEquals("หอคอยเวทมนตร์นิรันดร์", lore.title)

        val card = com.example.data.model.CharacterCard(
            workId = lore.id,
            name = "ลูเซียน",
            title = "จอมเวทศิลา",
            persona = "เย็นชาแต่คอยปกป้อง",
            firstMessage = "*มองด้วยสายตาราบเรียบ*",
            exampleDialogue = "<START>",
            speechStyle = "ทุ้มต่ำ",
            scenario = "ในห้องสมุดต้องห้าม",
            initialAffection = 40,
            initialObsession = 25,
            initialTrust = 30,
            initialFondness = 20,
            initialAversion = 5,
            initialFear = 0
        )
        val initialEmotions = card.toInitialEmotionState()
        assertEquals(40, initialEmotions.affection)
        assertEquals(25, initialEmotions.obsession)
        assertEquals(30, initialEmotions.trust)
        assertEquals(20, initialEmotions.fondness)
        assertEquals(5, initialEmotions.aversion)
        assertEquals(0, initialEmotions.fear)
    }
}
