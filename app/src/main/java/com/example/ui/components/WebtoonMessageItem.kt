package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessageEntity
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmotionAffectionColor
import com.example.ui.theme.EmotionAversionColor
import com.example.ui.theme.EmotionFearColor
import com.example.ui.theme.EmotionFondnessColor
import com.example.ui.theme.EmotionObsessionColor
import com.example.ui.theme.EmotionTrustColor
import com.example.ui.theme.MysticViolet
import com.example.ui.theme.RoseCrimson
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.VelvetPurple

@Composable
fun WebtoonMessageItem(
    message: ChatMessageEntity,
    characterName: String,
    characterAvatarColor: Long,
    modifier: Modifier = Modifier
) {
    val isUser = message.sender == "user"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag(if (isUser) "user_message_item" else "character_message_item"),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        if (isUser) {
            // Player dialogue / action card
            PlayerMessageBubble(message = message)
        } else {
            // Novel / Webtoon narrative presentation
            NovelCharacterBlock(
                message = message,
                characterName = characterName,
                avatarColor = Color(characterAvatarColor)
            )
        }
    }
}

@Composable
private fun PlayerMessageBubble(message: ChatMessageEntity) {
    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier.widthIn(max = 320.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp, end = 4.dp)
        ) {
            Text(
                text = "คุณ (ผู้เล่น)",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon
                )
            )
        }

        Surface(
            color = VelvetPurple.copy(alpha = 0.35f),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MysticViolet.copy(alpha = 0.5f))
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextPrimary,
                    lineHeight = 22.sp
                ),
                modifier = Modifier.padding(14.dp)
            )
        }
    }
}

@Composable
private fun NovelCharacterBlock(
    message: ChatMessageEntity,
    characterName: String,
    avatarColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 12.dp)
    ) {
        // Character header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(avatarColor, avatarColor.copy(alpha = 0.6f))
                        )
                    )
                    .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = characterName.take(1).uppercase(),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = characterName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                if (!message.narrativeBeat.isNullOrBlank()) {
                    Text(
                        text = message.narrativeBeat,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MysticViolet,
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic
                        )
                    )
                }
            }
        }

        // Novel narrative card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Main narrative text (Webtoon novel prose)
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextPrimary,
                        lineHeight = 23.sp
                    )
                )

                // Optional Inner Monologue
                if (!message.innerMonologue.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceDark.copy(alpha = 0.8f))
                            .border(0.5.dp, RoseCrimson.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = RoseCrimson,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ความคิดในใจ: \"${message.innerMonologue}\"",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontStyle = FontStyle.Italic,
                                    color = TextSecondary,
                                    lineHeight = 18.sp
                                )
                            )
                        }
                    }
                }

                // Turn emotion deltas badges
                val hasDeltas = (message.deltaAffection != null && message.deltaAffection != 0) ||
                        (message.deltaObsession != null && message.deltaObsession != 0) ||
                        (message.deltaTrust != null && message.deltaTrust != 0) ||
                        (message.deltaFondness != null && message.deltaFondness != 0) ||
                        (message.deltaAversion != null && message.deltaAversion != 0) ||
                        (message.deltaFear != null && message.deltaFear != 0)

                if (hasDeltas) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (message.deltaAffection != null && message.deltaAffection != 0) {
                            DeltaPill(label = "เสน่หา", delta = message.deltaAffection, color = EmotionAffectionColor)
                        }
                        if (message.deltaObsession != null && message.deltaObsession != 0) {
                            DeltaPill(label = "ยึดติด", delta = message.deltaObsession, color = EmotionObsessionColor)
                        }
                        if (message.deltaTrust != null && message.deltaTrust != 0) {
                            DeltaPill(label = "เชื่อใจ", delta = message.deltaTrust, color = EmotionTrustColor)
                        }
                        if (message.deltaFondness != null && message.deltaFondness != 0) {
                            DeltaPill(label = "ผูกพัน", delta = message.deltaFondness, color = EmotionFondnessColor)
                        }
                        if (message.deltaAversion != null && message.deltaAversion != 0) {
                            DeltaPill(label = "รังเกียจ", delta = message.deltaAversion, color = EmotionAversionColor)
                        }
                        if (message.deltaFear != null && message.deltaFear != 0) {
                            DeltaPill(label = "หวาดกลัว", delta = message.deltaFear, color = EmotionFearColor)
                        }
                    }
                }
            }
        }
    }
}
