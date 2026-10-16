package com.opus.piano.ui

import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.opus.piano.data.*
import kotlin.math.*

/** Original resolution-independent engraved medals; no emoji, stock artwork, or network assets. */
@Composable internal fun MedalArtwork(badge: BadgeProgress, modifier: Modifier = Modifier) {
    val light = if (!badge.unlocked) Color(0xFF8A857B) else when (badge.definition.rarity) {
        BadgeRarity.COMMON -> Color(0xFFD4AB83)
        BadgeRarity.RARE -> Color(0xFFD9DDE1)
        BadgeRarity.EPIC -> Color(0xFFC9AED2)
        BadgeRarity.LEGENDARY -> Color(0xFFF0D591)
    }
    val metal = if (!badge.unlocked) Color(0xFF514D47) else when (badge.definition.rarity) {
        BadgeRarity.COMMON -> Color(0xFFA5764B)
        BadgeRarity.RARE -> Color(0xFF8B969E)
        BadgeRarity.EPIC -> Color(0xFF84678F)
        BadgeRarity.LEGENDARY -> Color(0xFFBE9246)
    }
    val dark = if (!badge.unlocked) Color(0xFF282723) else when (badge.definition.rarity) {
        BadgeRarity.COMMON -> Color(0xFF423124)
        BadgeRarity.RARE -> Color(0xFF30383C)
        BadgeRarity.EPIC -> Color(0xFF352A3C)
        BadgeRarity.LEGENDARY -> Color(0xFF46371F)
    }
    Canvas(modifier.semantics { contentDescription = "${badge.definition.rarity.label} engraved ${badge.definition.name} medal, ${if (badge.unlocked) "unlocked" else "locked"}" }) {
        withTransform({ scale(size.width / 200f, size.height / 200f, Offset.Zero) }) {
            val center = Offset(100f, 100f)
            drawCircle(Color.Black.copy(alpha = .3f), 91f, Offset(100f, 104f))
            drawCircle(Brush.linearGradient(listOf(light, metal, dark, light), Offset(30f, 20f), Offset(170f, 180f)), 90f, center)
            drawCircle(dark, 85f, center)
            drawCircle(light.copy(alpha = .7f), 82f, center, style = Stroke(1.2f))
            drawCircle(Brush.radialGradient(listOf(dark, metal.copy(alpha = .55f), dark), center, 79f), 79f, center)
            drawCircle(metal, 73f, center, style = Stroke(.7f))
            // Milled coin edge and fine guilloché rather than a plain flat circle.
            repeat(96) { i ->
                val angle = i * PI.toFloat() / 48
                val p = Offset(100 + cos(angle) * 87.5f, 100 + sin(angle) * 87.5f)
                drawLine(if (i % 2 == 0) light.copy(alpha = .65f) else dark, p, Offset(100 + cos(angle) * 84.7f, 100 + sin(angle) * 84.7f), .65f)
            }
            repeat(48) { i ->
                val a = i * PI.toFloat() / 24
                drawCircle(light.copy(alpha = .6f), .8f, Offset(100 + cos(a) * 76.5f, 100 + sin(a) * 76.5f))
            }
            // Paired curved laurel stems with individual leaves, veins and relief shadows.
            listOf(-1f, 1f).forEach { side ->
                val stem = Path().apply { moveTo(100f, 164f); cubicTo(100 + side * 58, 152f, 100 + side * 70, 83f, 100 + side * 26, 45f) }
                drawPath(stem, dark, style = Stroke(3f))
                drawPath(stem, metal, style = Stroke(1.4f))
                repeat(10) { i ->
                    val t = i / 10f
                    val angle = (70 + t * 110) * PI.toFloat() / 180f
                    val x = 100 + side * (sin(angle) * 55)
                    val y = 103 + cos(angle) * 55
                    listOf(-1f, 1f).forEach { direction ->
                        val tip = Offset(x + side * direction * 12, y - 13)
                        val leaf = Path().apply { moveTo(x, y); quadraticTo(x + side * direction * 18, y - 1, tip.x, tip.y); quadraticTo(x - side * direction * 1, y - 15, x, y); close() }
                        drawPath(leaf, Brush.linearGradient(listOf(light, metal, dark), Offset(x - 4, y - 14), Offset(x + 7, y)))
                        drawLine(light.copy(alpha = .55f), Offset(x, y), tip, .5f)
                    }
                }
            }
            // Restrained central engravings distinguish the collections.
            when (badge.definition.artwork) {
                MedalArt.PIANO -> {
                    drawRoundRect(dark, Offset(72f, 75f), Size(56f, 46f), androidx.compose.ui.geometry.CornerRadius(3f))
                    repeat(7) { i -> drawRect(light.copy(alpha = .85f), Offset(74f + i * 7.5f, 78f), Size(6.4f, 40f)) }
                    listOf(0, 1, 3, 4, 5).forEach { i -> drawRect(dark, Offset(79f + i * 7.5f, 77f), Size(4f, 25f)) }
                }
                MedalArt.NOTE -> {
                    drawOval(light, Offset(80f, 106f), Size(21f, 14f))
                    drawLine(light, Offset(99f, 111f), Offset(99f, 68f), 4f)
                    val flag = Path().apply { moveTo(99f, 68f); cubicTo(119f, 75f, 116f, 83f, 104f, 91f) }
                    drawPath(flag, metal, style = Stroke(4f))
                }
                MedalArt.LYRE, MedalArt.BAROQUE -> {
                    val lyre = Path().apply { moveTo(77f, 71f); cubicTo(59f, 79f, 79f, 114f, 100f, 122f); cubicTo(121f, 114f, 141f, 79f, 123f, 71f) }
                    drawPath(lyre, light, style = Stroke(4f))
                    drawLine(light, Offset(76f, 75f), Offset(124f, 75f), 3f)
                    repeat(5) { i -> drawLine(metal, Offset(86f + i * 7f, 77f), Offset(93f + i * 3.5f, 115f), .9f) }
                    drawArc(light, 0f, 180f, false, Offset(85f, 116f), Size(30f, 8f), style = Stroke(2f))
                }
                MedalArt.ARCHIVE -> {
                    val book = Path().apply { moveTo(100f, 81f); quadraticTo(86f, 72f, 72f, 77f); lineTo(72f, 116f); quadraticTo(88f, 111f, 100f, 121f); quadraticTo(112f, 111f, 128f, 116f); lineTo(128f, 77f); quadraticTo(114f, 72f, 100f, 81f); lineTo(100f, 121f) }
                    drawPath(book, light, style = Stroke(2.4f))
                    repeat(4) { i -> drawLine(metal, Offset(78f, 85f + i * 7), Offset(94f, 89f + i * 7), 1f); drawLine(metal, Offset(106f, 89f + i * 7), Offset(122f, 85f + i * 7), 1f) }
                }
                MedalArt.CROWN -> {
                    val crown = Path().apply { moveTo(76f, 110f); lineTo(70f, 80f); lineTo(88f, 92f); lineTo(100f, 71f); lineTo(112f, 92f); lineTo(130f, 80f); lineTo(124f, 110f); close() }
                    drawPath(crown, Brush.verticalGradient(listOf(light, metal, dark), 70f, 118f))
                    drawLine(light, Offset(76f, 115f), Offset(124f, 115f), 3f)
                    listOf(Offset(70f, 80f), Offset(100f, 71f), Offset(130f, 80f)).forEach { drawCircle(light, 2.8f, it) }
                }
                else -> {
                    val monogram = when (badge.definition.artwork) { MedalArt.CHOPIN -> "FC"; MedalArt.BACH -> "JSB"; MedalArt.MOZART -> "WA"; else -> if (badge.definition.target == 7L) "VII" else "XXX" }
                    drawContext.canvas.nativeCanvas.drawText(monogram, 100f, 109f, android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { color = light.toArgb(); textSize = if (monogram.length > 2) 30f else 37f; textAlign = android.graphics.Paint.Align.CENTER; typeface = Typeface.create("serif", Typeface.BOLD) })
                }
            }
            drawLine(metal, Offset(78f, 136f), Offset(122f, 136f), .7f)
            val inscription = if (badge.definition.metric == AchievementMetric.PRACTICE_MILLIS) "C · HOURS" else "PRACTICE · ${badge.definition.target}"
            drawContext.canvas.nativeCanvas.drawText(inscription, 100f, 150f, android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { color = light.toArgb(); textSize = 8f; textAlign = android.graphics.Paint.Align.CENTER; isSubpixelText = true; typeface = Typeface.create("serif", Typeface.NORMAL) })
            drawCircle(light, 2f, Offset(100f, 39f))
            drawLine(light, Offset(94f, 165f), Offset(106f, 159f), 1.5f)
            drawLine(light, Offset(94f, 159f), Offset(106f, 165f), 1.5f)
        }
    }
}
