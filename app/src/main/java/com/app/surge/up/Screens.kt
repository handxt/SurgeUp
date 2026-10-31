package com.app.surge.up

import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class Launch(val oppId: String, val oppName: String, val map: String)

fun startGame(ctx: Context, l: Launch) {
    UiState.gameOn = true
    val i = Intent(ctx, GameActivity::class.java)
    i.putExtra("opp", l.oppId)
    i.putExtra("oppName", l.oppName)
    i.putExtra("map", l.map)
    ctx.startActivity(i)
}

@Composable
fun SurgeApp() {
    val ctx = LocalContext.current
    MaterialTheme(colorScheme = darkColorScheme()) {
        var stage by remember { mutableStateOf("loading") }
        var launch by remember { mutableStateOf<Launch?>(null) }
        val ban = UiState.ban
        Box(Modifier.fillMaxSize().background(CBg)) {
            if (ban != null) {
                BanScreen(ban)
            } else {
                when (stage) {
                    "loading" -> LoadingScreen("Memuat game...", 2600L) {
                        stage = if (Session.id.isEmpty()) "register" else "lobby"
                    }
                    "register" -> RegisterScreen { stage = "lobby" }
                    "lobby" -> LobbyScreen { l ->
                        launch = l
                        stage = "launching"
                    }
                    "launching" -> LoadingScreen("Menyiapkan pertandingan...", 1300L) {
                        val l = launch
                        if (l != null) startGame(ctx, l)
                        stage = "lobby"
                    }
                    else -> {}
                }
            }
        }
    }
}

private val TIPS = listOf(
    "Undang teman lewat panel TEMAN, lalu tekan UNDANG.",
    "Buka kartu SQUAD untuk bikin room dan salin kodenya.",
    "Chat global hilang setelah 7 menit.",
    "Masuk zona aman lebih awal, jangan jadi sasaran empuk.",
    "Pakai musik? Atur di menu pengaturan."
)

// ---------- Loading screen ----------
@Composable
fun LoadingScreen(text: String, ms: Long, onDone: () -> Unit) {
    var progress by remember { mutableStateOf(0f) }
    val tip = remember { TIPS[(Math.random() * TIPS.size).toInt()] }
    val inf = rememberInfiniteTransition(label = "load")
    val zoom by inf.animateFloat(
        initialValue = 1.04f, targetValue = 1.14f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Reverse),
        label = "zoom"
    )
    val sweep by inf.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Restart),
        label = "sweep"
    )

    LaunchedEffect(Unit) {
        val start = System.currentTimeMillis()
        // kerja nyata saat loading: ambil kata terlarang dan cek ban (maks 4 detik)
        withTimeoutOrNull(4000L) {
            Moderation.refreshWords()
            if (Session.id.isNotEmpty()) UiState.ban = Moderation.checkBan(Session.id)
        }
        while (true) {
            val el = System.currentTimeMillis() - start
            progress = (el.toFloat() / ms.toFloat()).coerceAtMost(1f)
            if (el >= ms) break
            delay(40L)
        }
        onDone()
    }

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.loading_art),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().graphicsLayer {
                scaleX = zoom
                scaleY = zoom
            }
        )
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0x33000000), Color(0xEE000000)))))
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xAA000000), Color.Transparent, Color(0x66000000)))))
        // garis cahaya merah yang bergerak
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            for (k in 0 until 3) {
                val x = ((sweep + k * 0.33f) % 1f) * (w + h) - h
                drawLine(
                    color = Color(0x55E5233A),
                    start = Offset(x, h),
                    end = Offset(x + h * 0.6f, 0f),
                    strokeWidth = 3f + k * 2f
                )
            }
        }
        Column(
            Modifier.align(Alignment.BottomStart).padding(start = 36.dp, bottom = 30.dp).width(420.dp)
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text("SURGE", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
                Text("UP", color = CRed, fontSize = 40.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
            }
            Box(Modifier.width(120.dp).height(3.dp).background(CRed))
            Spacer(Modifier.height(10.dp))
            Text(text, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(tip, color = CSoft, fontSize = 12.sp)
            Spacer(Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFF26262E))) {
                Box(
                    Modifier.fillMaxWidth(progress).fillMaxHeight()
                        .background(Brush.horizontalGradient(listOf(CRedDark, CRed, Color(0xFFFF6B7A))))
                )
            }
            Spacer(Modifier.height(4.dp))
            Text((progress * 100f).toInt().toString() + "%", color = CSoft, fontSize = 11.sp)
        }
    }
}

// ---------- Daftar ----------
@Composable
fun RegisterScreen(onDone: () -> Unit) {
    var nick by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var msg by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.loading_art),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(Modifier.fillMaxSize().background(Color(0xCC000000)))
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row {
                Text("SURGE", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
                Text("UP", color = CRed, fontSize = 36.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
            }
            Text("Buat nama pemain", color = CSoft, fontSize = 14.sp)
            Spacer(Modifier.height(14.dp))
            Field(nick, { if (it.length <= 14) nick = it }, "Nama kamu", Modifier.width(280.dp))
            Spacer(Modifier.height(12.dp))
            RedButton(if (busy) "..." else "MASUK", Modifier.width(280.dp), enabled = !busy) {
                if (nick.trim().length < 3) {
                    msg = "Minimal 3 huruf."
                } else {
                    busy = true
                    msg = ""
                    scope.launch {
                        val ok = Session.register(nick)
                        busy = false
                        if (ok) onDone() else msg = "Gagal terhubung ke server. Cek internet."
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(msg, color = Color(0xFFFF7A8A), fontSize = 13.sp)
        }
    }
}

// ---------- Ban ----------
@Composable
fun BanScreen(b: BanInfo) {
    var tick by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            tick++
        }
    }
    val left = if (tick >= 0) Moderation.fmtLeft(b.until) else ""
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Akun kamu di-ban", color = CRed, fontSize = 28.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
        Spacer(Modifier.height(10.dp))
        Text("Alasan: " + b.reason, color = Color.White, fontSize = 15.sp)
        Spacer(Modifier.height(6.dp))
        Text("Sisa waktu: " + left, color = CSoft, fontSize = 15.sp)
    }
}
