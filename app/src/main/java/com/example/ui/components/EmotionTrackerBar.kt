package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EmotionDeltas
import com.example.data.model.EmotionState
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.EmotionAffectionColor
import com.example.ui.theme.EmotionAversionColor
import com.example.ui.theme.EmotionFearColor
import com.example.ui.theme.EmotionFondnessColor
import com.example.ui.theme.EmotionObsessionColor
import com.example.ui.theme.EmotionTrustColor
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun EmotionSummaryHeader(
    emotions: EmotionState,
    latestDeltas: EmotionDeltas? = null,
    modifier: Modifier = Modifier,
    onClickExpand: () -> Unit = {}
) {
    Surface(
        onClick = onClickExpand,
        modifier = modifier
            .fillMaxWidth()
            .testTag("emotion_summary_header"),
        color = CardSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(EmotionObsessionColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "สภาวะอารมณ์: ${emotions.getDominantMood()}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    )
                }

                Text(
                    text = "ดู 6 มิติอารมณ์ ▾",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Mini multi-color 6-axis progress track
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.Black.copy(alpha = 0.4f)),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                MiniEmotionBar(value = emotions.affection, color = EmotionAffectionColor, modifier = Modifier.weight(1f))
                MiniEmotionBar(value = emotions.obsession, color = EmotionObsessionColor, modifier = Modifier.weight(1f))
                MiniEmotionBar(value = emotions.trust, color = EmotionTrustColor, modifier = Modifier.weight(1f))
                MiniEmotionBar(value = emotions.fondness, color = EmotionFondnessColor, modifier = Modifier.weight(1f))
                MiniEmotionBar(value = emotions.aversion, color = EmotionAversionColor, modifier = Modifier.weight(1f))
                MiniEmotionBar(value = emotions.fear, color = EmotionFearColor, modifier = Modifier.weight(1f))
            }

            if (latestDeltas != null && latestDeltas.hasAnyChange()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (latestDeltas.deltaAffection != 0) {
                        DeltaPill(label = "เสน่หา", delta = latestDeltas.deltaAffection, color = EmotionAffectionColor)
                    }
                    if (latestDeltas.deltaObsession != 0) {
                        DeltaPill(label = "ยึดติด", delta = latestDeltas.deltaObsession, color = EmotionObsessionColor)
                    }
                    if (latestDeltas.deltaTrust != 0) {
                        DeltaPill(label = "เชื่อใจ", delta = latestDeltas.deltaTrust, color = EmotionTrustColor)
                    }
                    if (latestDeltas.deltaFondness != 0) {
                        DeltaPill(label = "ผูกพัน", delta = latestDeltas.deltaFondness, color = EmotionFondnessColor)
                    }
                    if (latestDeltas.deltaAversion != 0) {
                        DeltaPill(label = "รังเกียจ", delta = latestDeltas.deltaAversion, color = EmotionAversionColor)
                    }
                    if (latestDeltas.deltaFear != 0) {
                        DeltaPill(label = "หวาดกลัว", delta = latestDeltas.deltaFear, color = EmotionFearColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniEmotionBar(value: Int, color: Color, modifier: Modifier = Modifier) {
    val progress = (value.toFloat() / 100f).coerceIn(0.05f, 1f)
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(2.dp))
            .background(Color.White.copy(alpha = 0.08f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .background(color)
        )
    }
}

@Composable
fun FullEmotionTrackerCard(
    emotions: EmotionState,
    modifier: Modifier = Modifier
) {
    RealtimeEmotionDashboardCard(
        emotions = emotions,
        modifier = modifier
    )
}

@Composable
private fun EmotionAxisRow(
    name: String,
    value: Int,
    color: Color,
    icon: ImageVector,
    description: String
) {
    val animatedProgress by animateFloatAsState(
        targetValue = value.toFloat() / 100f,
        label = "progress_$name"
    )

    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                )
            }
            Text(
                text = "$value / 100",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Black.copy(alpha = 0.5f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress.coerceIn(0.01f, 1f))
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(color.copy(alpha = 0.7f), color)
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = TextSecondary.copy(alpha = 0.8f)
            )
        )
    }
}

@Composable
fun DeltaPill(
    label: String,
    delta: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val sign = if (delta > 0) "+$delta" else "$delta"
    val arrow = if (delta > 0) "▲" else "▼"
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.18f))
            .border(0.5.dp, color.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "$arrow $label $sign",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = color
            )
        )
    }
}
