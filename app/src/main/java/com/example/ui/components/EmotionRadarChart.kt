package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EmotionState
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
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VelvetPurple
import kotlin.math.cos
import kotlin.math.sin

/**
 * 6-Axis Radar Chart showing real-time emotions:
 * 1. Affection (เสน่หา)
 * 2. Obsession (ยึดติด)
 * 3. Trust (เชื่อใจ)
 * 4. Fondness (ผูกพัน)
 * 5. Aversion (รังเกียจ)
 * 6. Fear (หวาดกลัว)
 */
@Composable
fun EmotionRadarChart(
    emotions: EmotionState,
    modifier: Modifier = Modifier,
    fillAlpha: Float = 0.42f
) {
    // Smoothly animate each axis value (0f..1f)
    val animAffection by animateFloatAsState(
        targetValue = (emotions.affection.coerceIn(5, 100) / 100f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "anim_affection"
    )
    val animObsession by animateFloatAsState(
        targetValue = (emotions.obsession.coerceIn(5, 100) / 100f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "anim_obsession"
    )
    val animTrust by animateFloatAsState(
        targetValue = (emotions.trust.coerceIn(5, 100) / 100f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "anim_trust"
    )
    val animFondness by animateFloatAsState(
        targetValue = (emotions.fondness.coerceIn(5, 100) / 100f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "anim_fondness"
    )
    val animAversion by animateFloatAsState(
        targetValue = (emotions.aversion.coerceIn(5, 100) / 100f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "anim_aversion"
    )
    val animFear by animateFloatAsState(
        targetValue = (emotions.fear.coerceIn(5, 100) / 100f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "anim_fear"
    )

    val axisValues = remember(animAffection, animObsession, animTrust, animFondness, animAversion, animFear) {
        listOf(
            animAffection,
            animObsession,
            animTrust,
            animFondness,
            animAversion,
            animFear
        )
    }

    val axisColors = listOf(
        EmotionAffectionColor,
        EmotionObsessionColor,
        EmotionTrustColor,
        EmotionFondnessColor,
        EmotionAversionColor,
        EmotionFearColor
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.15f)
            .testTag("emotion_radar_chart"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = (size.minDimension / 2f) * 0.72f

            // 1. Draw Concentric Hexagonal Web Rings (25%, 50%, 75%, 100%)
            val ringFractions = listOf(0.25f, 0.5f, 0.75f, 1.0f)
            ringFractions.forEachIndexed { index, fraction ->
                val ringRadius = maxRadius * fraction
                val ringPath = Path()
                for (i in 0 until 6) {
                    val angle = Math.toRadians((i * 60.0) - 90.0)
                    val x = center.x + (ringRadius * cos(angle)).toFloat()
                    val y = center.y + (ringRadius * sin(angle)).toFloat()
                    if (i == 0) ringPath.moveTo(x, y) else ringPath.lineTo(x, y)
                }
                ringPath.close()

                // Subtle fill for outer ring
                if (fraction == 1.0f) {
                    drawPath(
                        path = ringPath,
                        color = CardSurface.copy(alpha = 0.5f),
                        style = Fill
                    )
                }

                drawPath(
                    path = ringPath,
                    color = Color.White.copy(alpha = if (fraction == 1.0f) 0.22f else 0.10f),
                    style = Stroke(
                        width = if (fraction == 1.0f) 1.5f else 1f,
                        pathEffect = if (fraction < 1.0f) PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f) else null
                    )
                )
            }

            // 2. Draw 6 Radial Spokes from Center
            for (i in 0 until 6) {
                val angle = Math.toRadians((i * 60.0) - 90.0)
                val target = Offset(
                    x = center.x + (maxRadius * cos(angle)).toFloat(),
                    y = center.y + (maxRadius * sin(angle)).toFloat()
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.15f),
                    start = center,
                    end = target,
                    strokeWidth = 1f
                )
            }

            // 3. Compute Data Polygon Path
            val dataPath = Path()
            val points = mutableListOf<Offset>()
            for (i in 0 until 6) {
                val angle = Math.toRadians((i * 60.0) - 90.0)
                val r = maxRadius * axisValues[i]
                val pt = Offset(
                    x = center.x + (r * cos(angle)).toFloat(),
                    y = center.y + (r * sin(angle)).toFloat()
                )
                points.add(pt)
                if (i == 0) dataPath.moveTo(pt.x, pt.y) else dataPath.lineTo(pt.x, pt.y)
            }
            dataPath.close()

            // 4. Fill Data Polygon with Glowing Gradient
            val gradientBrush = Brush.radialGradient(
                colors = listOf(
                    MysticViolet.copy(alpha = fillAlpha * 0.9f),
                    VelvetPurple.copy(alpha = fillAlpha * 0.65f),
                    CyanNeon.copy(alpha = fillAlpha * 0.25f)
                ),
                center = center,
                radius = maxRadius
            )
            drawPath(path = dataPath, brush = gradientBrush, style = Fill)

            // 5. Draw Glowing Border on Data Polygon
            drawPath(
                path = dataPath,
                color = MysticViolet.copy(alpha = 0.95f),
                style = Stroke(width = 2.5f, cap = StrokeCap.Round)
            )

            // 6. Draw Data Vertex Nodes with individual axis colors
            points.forEachIndexed { i, pt ->
                // Outer glow
                drawCircle(
                    color = axisColors[i].copy(alpha = 0.4f),
                    radius = 7.dp.toPx(),
                    center = pt
                )
                // Inner core
                drawCircle(
                    color = axisColors[i],
                    radius = 4.dp.toPx(),
                    center = pt
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = pt
                )
            }
        }

        // Overlay 6 Labels at Chart Extremities
        RadarChartLabels(
            emotions = emotions,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun RadarChartLabels(
    emotions: EmotionState,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // 0: Affection (Top)
        RadarLabelBadge(
            label = "เสน่หา",
            en = "Affection",
            value = emotions.affection,
            color = EmotionAffectionColor,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 2.dp)
        )

        // 1: Obsession (Top-Right)
        RadarLabelBadge(
            label = "ยึดติด",
            en = "Obsession",
            value = emotions.obsession,
            color = EmotionObsessionColor,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 26.dp, end = 4.dp)
        )

        // 2: Trust (Bottom-Right)
        RadarLabelBadge(
            label = "เชื่อใจ",
            en = "Trust",
            value = emotions.trust,
            color = EmotionTrustColor,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 26.dp, end = 4.dp)
        )

        // 3: Fondness (Bottom)
        RadarLabelBadge(
            label = "ผูกพัน",
            en = "Fondness",
            value = emotions.fondness,
            color = EmotionFondnessColor,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 2.dp)
        )

        // 4: Aversion (Bottom-Left)
        RadarLabelBadge(
            label = "รังเกียจ",
            en = "Aversion",
            value = emotions.aversion,
            color = EmotionAversionColor,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 26.dp, start = 4.dp)
        )

        // 5: Fear (Top-Left)
        RadarLabelBadge(
            label = "หวาดกลัว",
            en = "Fear",
            value = emotions.fear,
            color = EmotionFearColor,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 26.dp, start = 4.dp)
        )
    }
}

@Composable
private fun RadarLabelBadge(
    label: String,
    en: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = CardSurface.copy(alpha = 0.92f),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, color.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "$value",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = color
                        )
                    )
                }
            }
        }
    }
}

/**
 * Interactive Real-Time 6-Axis Emotion Tracker Card with Toggle (Radar Chart vs. Bars)
 */
@Composable
fun RealtimeEmotionDashboardCard(
    emotions: EmotionState,
    modifier: Modifier = Modifier,
    initialMode: Int = 0 // 0 = Radar, 1 = Bars, 2 = Both
) {
    var selectedViewMode by remember { mutableIntStateOf(initialMode) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("realtime_emotion_dashboard_card"),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "เมทริกซ์ 6 มิติอารมณ์แบบเรียลไทม์",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "อารมณ์เด่นขณะนี้: ${emotions.getDominantMood()}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MysticViolet,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                // Mode switch pills
                Surface(
                    color = Color.Black.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        ModeChip(
                            icon = Icons.Default.Radar,
                            text = "เรดาร์",
                            selected = selectedViewMode == 0,
                            onClick = { selectedViewMode = 0 }
                        )
                        ModeChip(
                            icon = Icons.Default.BarChart,
                            text = "แท่งแถบ",
                            selected = selectedViewMode == 1,
                            onClick = { selectedViewMode = 1 }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (selectedViewMode) {
                0 -> {
                    // Radar View
                    EmotionRadarChart(
                        emotions = emotions,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }
                1 -> {
                    // Bars View
                    EmotionBarsList(emotions = emotions)
                }
                else -> {
                    EmotionRadarChart(emotions = emotions)
                    Spacer(modifier = Modifier.height(12.dp))
                    EmotionBarsList(emotions = emotions)
                }
            }
        }
    }
}

@Composable
private fun ModeChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (selected) VelvetPurple else Color.Transparent,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) Color.White else TextSecondary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) Color.White else TextSecondary
                )
            )
        }
    }
}

@Composable
fun EmotionBarsList(
    emotions: EmotionState,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        EmotionBarItem(
            name = "ความเสน่หา (Affection)",
            value = emotions.affection,
            color = EmotionAffectionColor,
            icon = Icons.Default.Favorite,
            desc = "ความปรารถนาโรแมนติก การเปิดใจ และความอ่อนโยน"
        )
        EmotionBarItem(
            name = "ความยึดติด (Obsession)",
            value = emotions.obsession,
            color = EmotionObsessionColor,
            icon = Icons.Default.Lock,
            desc = "ความคลั่งไคล้ หึงหวง และความต้องการครอบครอง"
        )
        EmotionBarItem(
            name = "ความเชื่อใจ (Trust)",
            value = emotions.trust,
            color = EmotionTrustColor,
            icon = Icons.Default.Shield,
            desc = "การยอมลดการระแวดระวังและเผยความลับในใจ"
        )
        EmotionBarItem(
            name = "ความผูกพัน (Fondness)",
            value = emotions.fondness,
            color = EmotionFondnessColor,
            icon = Icons.Default.Psychology,
            desc = "ความห่วงใย มิตรภาพลึกซึ้ง และความเอ็นดู"
        )
        EmotionBarItem(
            name = "ความรังเกียจ (Aversion)",
            value = emotions.aversion,
            color = EmotionAversionColor,
            icon = Icons.Default.SentimentDissatisfied,
            desc = "ความเย็นชา ความรังเกียจ และการปฏิเสธสัมผัส"
        )
        EmotionBarItem(
            name = "ความหวาดกลัว (Fear)",
            value = emotions.fear,
            color = EmotionFearColor,
            icon = Icons.Default.Visibility,
            desc = "ความตึงเครียด วิตกกังวล และความรู้สึกถูกคุกคาม"
        )
    }
}

@Composable
private fun EmotionBarItem(
    name: String,
    value: Int,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    desc: String
) {
    val progress by animateFloatAsState(
        targetValue = (value / 100f).coerceIn(0.02f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "bar_progress_$name"
    )

    Column {
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
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                )
            }
            Text(
                text = "$value / 100",
                style = MaterialTheme.typography.labelSmall.copy(
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
                    .fillMaxSize()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(color.copy(alpha = 0.6f), color)
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = desc,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = TextSecondary.copy(alpha = 0.75f)
            )
        )
    }
}
