package com.masjid.clockdisplay

import android.content.Context
import android.graphics.Bitmap
import android.media.RingtoneManager
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

val CardBg = Color(0x7F0B2218)
val CardBorder = Color(0x33A0D6B4)
val ActiveBorder = Color(0xFFEADB80)
val TextWhite = Color(0xFFFFFFFF)
val TextMuted = Color(0xB3FFFFFF)
val GoldAccent = Color(0xFFF3C759)
val MetallicRingOuter = Color(0xFFDCDCDC)
val MetallicRingInner = Color(0xFF888888)

val CdBgTop = Color(0xFF1F1C6B)
val CdBgBottom = Color(0xFF801A9E)
val CdArcTrack = Color(0x33FFFFFF)
val CdArcProgress = Color(0xFF00E5FF)

data class PrayerTime(
    val icon: String,
    val name: String,
    val athan: String,
    val jamaat: String,
    val isActive: Boolean = false
)

class MainActivity : ComponentActivity() {

    private var showSettingsState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MasjidApp(showSettings = showSettingsState)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            showSettingsState.value = !showSettingsState.value
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}

@Composable
fun MasjidApp(showSettings: MutableState<Boolean>) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("masjid_prefs", Context.MODE_PRIVATE) }

    var upiId by remember { mutableStateOf(prefs.getString("upi_id", "jamaamasjid@upi") ?: "jamaamasjid@upi") }
    var countdownMinutesTrigger by remember { mutableIntStateOf(prefs.getInt("cd_minutes", 3)) }
    var audioAlertEnabled by remember { mutableStateOf(prefs.getBoolean("audio_alert", true)) }

    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Calendar.getInstance()
            delay(1000)
        }
    }

    val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
    val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())

    val nextJamaatCal = remember(currentTime) {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 16)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
        }
    }

    val millisRemaining = nextJamaatCal.timeInMillis - currentTime.timeInMillis
    val isCountdownActive = millisRemaining in 1..(countdownMinutesTrigger * 60 * 1000L)

    LaunchedEffect(millisRemaining) {
        if (millisRemaining in 1..1000 && audioAlertEnabled) {
            playAudioAlert(context)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isCountdownActive) {
            CountdownScreen(
                prayerName = "ASR",
                millisRemaining = millisRemaining,
                totalMinutes = countdownMinutesTrigger,
                currentTimeString = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(currentTime.time),
                formattedDate = dateFormat.format(currentTime.time),
                upiId = upiId
            )
        } else {
            MainDashboardScreen(
                currentTime = currentTime,
                digitalTimeString = timeFormat.format(currentTime.time),
                formattedDate = dateFormat.format(currentTime.time),
                upiId = upiId
            )
        }

        if (showSettings.value) {
            SettingsOverlay(
                upiId = upiId,
                onUpiChange = {
                    upiId = it
                    prefs.edit().putString("upi_id", it).apply()
                },
                countdownMinutes = countdownMinutesTrigger,
                onCountdownChange = {
                    countdownMinutesTrigger = it
                    prefs.edit().putInt("cd_minutes", it).apply()
                },
                audioEnabled = audioAlertEnabled,
                onAudioToggle = {
                    audioAlertEnabled = it
                    prefs.edit().putBoolean("audio_alert", it).apply()
                },
                onClose = { showSettings.value = false }
            )
        }
    }
}

@Composable
fun MainDashboardScreen(
    currentTime: Calendar,
    digitalTimeString: String,
    formattedDate: String,
    upiId: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F3223),
                        Color(0xFF092218),
                        Color(0xFF04120C)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val colorSil = Color(0x1F00331A)

            drawCircle(
                color = colorSil,
                radius = w * 0.22f,
                center = Offset(w * 0.5f, h * 0.55f)
            )

            val minaretPathRight = Path().apply {
                moveTo(w * 0.78f, h)
                lineTo(w * 0.78f, h * 0.35f)
                lineTo(w * 0.79f, h * 0.28f)
                lineTo(w * 0.80f, h * 0.35f)
                lineTo(w * 0.80f, h)
                close()
            }
            drawPath(path = minaretPathRight, color = colorSil)

            val minaretPathLeft = Path().apply {
                moveTo(w * 0.22f, h)
                lineTo(w * 0.22f, h * 0.38f)
                lineTo(w * 0.225f, h * 0.30f)
                lineTo(w * 0.23f, h * 0.38f)
                lineTo(w * 0.23f, h)
                close()
            }
            drawPath(path = minaretPathLeft, color = colorSil)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HeaderSection(formattedDate)

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                JumuahCard(modifier = Modifier.weight(0.24f))

                MainCenterClockSection(
                    modifier = Modifier.weight(0.52f),
                    currentTime = currentTime,
                    digitalTimeString = digitalTimeString,
                    formattedDate = formattedDate
                )

                QrCodeCard(upiId = upiId, modifier = Modifier.weight(0.24f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            PrayerTimesRow(modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(10.dp))

            BottomTickerSection()
        }
    }
}

@Composable
fun MainCenterClockSection(
    modifier: Modifier = Modifier,
    currentTime: Calendar,
    digitalTimeString: String,
    formattedDate: String
) {
    Row(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0x88061C13))
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ModernAnalogClock(currentTime = currentTime, modifier = Modifier.size(200.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🌅 ", fontSize = 22.sp)
                Text(
                    text = "Asr Jamaat",
                    color = TextWhite,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "04:30 PM",
                color = TextWhite,
                fontSize = 46.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "($digitalTimeString)",
                color = GoldAccent,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Rabi' al-Thani 4, 1448 AH",
                color = TextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = formattedDate,
                color = TextMuted,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun CountdownScreen(
    prayerName: String,
    millisRemaining: Long,
    totalMinutes: Int,
    currentTimeString: String,
    formattedDate: String,
    upiId: String
) {
    val totalSecs = (millisRemaining / 1000).coerceAtLeast(0)
    val mins = totalSecs / 60
    val secs = totalSecs % 60
    val progress = (totalSecs.toFloat() / (totalMinutes * 60f)).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(CdBgTop, CdBgBottom)))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🌅 ", fontSize = 20.sp)
                Column {
                    Text("SUNRISE", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("05:34 AM", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(20.dp))
                Text("🌡️ 26°C", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0x33FFFFFF),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = prayerName,
                        color = TextWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }
                Text(
                    text = "JAMAAT IN",
                    color = TextWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(280.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 12.dp.toPx()
                    val diameter = size.minDimension - stroke
                    val topLeft = Offset(stroke / 2, stroke / 2)

                    drawArc(
                        color = CdArcTrack,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(diameter, diameter),
                        style = Stroke(stroke)
                    )

                    drawArc(
                        color = CdArcProgress,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(diameter, diameter),
                        style = Stroke(stroke)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%02d:%02d", mins, secs),
                        color = TextWhite,
                        fontSize = 58.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Row {
                        Text("MIN  ", color = TextMuted, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("SEC", color = TextMuted, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val qrBitmap = remember(upiId) { generateUpiQrBitmap(upiId) }
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "UPI QR Code",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Scan To Stay\nConnected", color = TextWhite, fontSize = 12.sp, textAlign = TextAlign.Center)
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currentTimeString,
                color = TextWhite,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(text = formattedDate, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = "Rabi' al-Thani 4, 1448 AH", color = TextMuted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun QrCodeCard(upiId: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(135.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(2.dp, MetallicRingOuter, RoundedCornerShape(12.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            val qrBitmap = remember(upiId) { generateUpiQrBitmap(upiId) }
            if (qrBitmap != null) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "Masjid UPI QR",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text("QR CODE", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Scan & Pay via UPI\n$upiId",
            color = TextWhite,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun SettingsOverlay(
    upiId: String,
    onUpiChange: (String) -> Unit,
    countdownMinutes: Int,
    onCountdownChange: (Int) -> Unit,
    audioEnabled: Boolean,
    onAudioToggle: (Boolean) -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF14281D)),
            modifier = Modifier
                .width(420.dp)
                .border(2.dp, GoldAccent, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("⚙️ MASJID TV SETTINGS", color = GoldAccent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = upiId,
                    onValueChange = onUpiChange,
                    label = { Text("Masjid UPI ID", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = CardBorder
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Countdown Window:", color = TextWhite, fontSize = 14.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { if (countdownMinutes > 1) onCountdownChange(countdownMinutes - 1) },
                            colors = ButtonDefaults.buttonColors(containerColor = CardBg)
                        ) { Text("-", color = TextWhite) }

                        Text(" $countdownMinutes Min ", color = GoldAccent, fontWeight = FontWeight.Bold)

                        Butt
