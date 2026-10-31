package com.app.surge.up

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PUNK_FRAMES = intArrayOf(
        R.drawable.punk_f00,
        R.drawable.punk_f01,
        R.drawable.punk_f02,
        R.drawable.punk_f03,
        R.drawable.punk_f04,
        R.drawable.punk_f05,
        R.drawable.punk_f06,
        R.drawable.punk_f07,
        R.drawable.punk_f08,
        R.drawable.punk_f09,
        R.drawable.punk_f10,
        R.drawable.punk_f11,
        R.drawable.punk_f12,
        R.drawable.punk_f13,
        R.drawable.punk_f14,
        R.drawable.punk_f15,
)

private val BALOK_FRAMES = intArrayOf(
        R.drawable.balok_f00,
        R.drawable.balok_f01,
        R.drawable.balok_f02,
        R.drawable.balok_f03,
        R.drawable.balok_f04,
        R.drawable.balok_f05,
        R.drawable.balok_f06,
        R.drawable.balok_f07,
        R.drawable.balok_f08,
        R.drawable.balok_f09,
        R.drawable.balok_f10,
        R.drawable.balok_f11,
        R.drawable.balok_f12,
        R.drawable.balok_f13,
        R.drawable.balok_f14,
        R.drawable.balok_f15,
)

/** Panggung lobby 100% native: satu lantai penuh (tanpa lingkaran) + karakter 3D beranimasi. */
@Composable
fun NativeStage(room: RoomState?) {
    val mate = room?.members?.firstOrNull { it.id != Session.id }
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier.align(Alignment.BottomCenter).padding(bottom = 58.dp).fillMaxWidth(0.7f).height(30.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF2A2A32), Color(0xFF0C0C10))))
        )
        Row(
            Modifier.align(Alignment.BottomCenter).padding(bottom = 72.dp),
            horizontalArrangement = Arrangement.spacedBy(30.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CharArt(Session.char, 290)
                NameTag(Session.name)
            }
            if (mate != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CharArt(mate.ch, 290, true)
                    NameTag(mate.name)
                }
            }
        }
    }
}

@Composable
fun NameTag(name: String) {
    Text(
        name.uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic, fontSize = 13.sp, maxLines = 1,
        modifier = Modifier.padding(top = 4.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xCC000000)).padding(horizontal = 12.dp, vertical = 3.dp)
    )
}

/** Karakter 3D (render model low-poly, animasi idle). PUNK dari game; BALOK karakter original. */
@Composable
fun CharArt(id: String, heightDp: Int, mirror: Boolean = false) {
    val ctx = LocalContext.current
    val ids = if (id == "BALOK") BALOK_FRAMES else PUNK_FRAMES
    val frames: List<ImageBitmap> = remember(id) { ids.map { ImageBitmap.imageResource(ctx.resources, it) } }
    val inf = rememberInfiniteTransition(label = "char")
    val f by inf.animateFloat(
        initialValue = 0f,
        targetValue = ids.size.toFloat(),
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing)),
        label = "frame"
    )
    val idx = f.toInt().coerceIn(0, ids.size - 1)
    Image(
        bitmap = frames[idx],
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier.height(heightDp.dp).graphicsLayer { scaleX = if (mirror) -1f else 1f }
    )
}
