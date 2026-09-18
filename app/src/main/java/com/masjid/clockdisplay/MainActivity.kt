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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

    // Prayer Times State
    var fajrAthan by remember { mutableStateOf(prefs.getString("fajr_athan", "04:30 AM") ?: "04:30 AM") }
    var fajrJamaat by remember { mutableStateOf(prefs.getString("fajr_jamaat", "05:00 AM") ?: "05:00 AM") }

    var dhuhrAthan by remember { mutableStateOf(prefs.getString("dhuhr_athan", "01:00 PM") ?: "01:00 PM") }
    var dhuhrJamaat by remember { mutableStateOf(prefs.getString("dhuhr_jamaat", "01:15 PM") ?: "01:15 PM") }

    var asrAthan by remember { mutableStateOf(prefs.getString("asr_athan", "04:15 PM") ?: "04:15 PM") }
    var asrJamaat by remember { mutableStateOf(prefs.getString("asr_jamaat", "04:30 PM") ?: "04:30 PM") }

    var maghribAthan by remember { mutableStateOf(prefs.getString("maghrib_athan", "05:55 PM") ?: "05:55 PM") }
    var maghribJamaat by remember { mutableStateOf(prefs.getString("maghrib_jamaat", "05:58 PM") ?: "05:58 PM") }

    var ishaAthan by remember { mutableStateOf(prefs.getString("isha_athan", "07:45 PM") ?: "07:45 PM") }
    var ishaJamaat by remember { mutableStateOf(prefs.getString("isha_jamaat", "08:00 PM") ?: "08:00 PM") }

    var jumuahAthan by remember { mutableStateOf(prefs.getString("jumuah_athan", "12:15 PM") ?: "12:15 PM") }
    var jumuahJamaat by remember { mutableStateOf(prefs.getString("jumuah_jamaat", "01:00 PM") ?: "01:00 PM") }

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

    val prayersList = listOf(
        PrayerTime("🌤️", "Fajr", fajrAthan, fajrJamaat),
        PrayerTime("☀️", "Dhuhr", dhuhrAthan, dhuhrJamaat),
        PrayerTime("⛅", "Asr", asrAthan, asrJamaat, isActive = true),
        PrayerTime("📖", "Maghrib", maghribAthan, maghribJamaat),
        PrayerTime("🌙", "Isha", ishaAthan, ishaJamaat)
    )

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
                upiId = upiId,
                prayers = prayersList,
                jumuahAthan = jumuahAthan,
                jumuahJamaat = jumuahJamaat,
                currentJamaatTime = asrJamaat
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
                fajrAthan = fajrAthan, onFajrAthanChange = { fajrAthan = it; prefs.edit().putString("fajr_athan", it).apply() },
                fajrJamaat = fajrJamaat, onFajrJamaatChange = { fajrJamaat = it; prefs.edit().putString("fajr_jamaat", it).apply() },
                dhuhrAthan = dhuhrAthan, onDhuhrAthanChange = { dhuhrAthan = it; prefs.edit().putString("dhuhr_athan", it).apply() },
                dhuhrJamaat = dhuhrJamaat, onDhuhrJamaatChange = { dhuhrJamaat = it; prefs.edit().putString("dhuhr_jamaat", it).apply() },
                asrAthan = asrAthan, onAsrAthanChange = { asrAthan = it; prefs.edit().putString("asr_athan", it).apply() },
                asrJamaat = asrJamaat, onAsrJamaatChange = { asrJamaat = it; prefs.edit().putString("asr_jamaat", it).apply() },
                maghribAthan = maghribAthan, onMaghribAthanChange = { maghribAthan = it; prefs.edit().putString("maghrib_athan", it).apply() },
                maghribJamaat = maghribJamaat, onMaghribJamaatChange = { maghribJamaat = it; prefs.edit().putString("maghrib_jamaat", it).apply() },
                ishaAthan = ishaAthan, onIshaAthanChange = { ishaAthan = it; prefs.edit().putString("isha_athan", it).apply() },
                ishaJamaat = ishaJamaat, onIshaJamaatChange = { ishaJamaat = it; prefs.edit().putString("isha_jamaat", it).apply() },
                jumuahAthan = jumuahAthan, onJumuahAthanChange = { jumuahAthan = it; prefs.edit().putString("jumuah_athan", it).apply() },
                jumuahJamaat = jumuahJamaat, onJumuahJamaatChange = { jumuahJamaat = it; prefs.edit().putString("jumuah_jamaat", it).apply() },
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
    upiId: String,
    prayers: List<PrayerTime>,
    jumuahAthan: String,
    jumuahJamaat: String,
    currentJamaatTime: String
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
                JumuahCard(modifier = Modifier.weight(0.24f), athan = jumuahAthan, jamaat = jumuahJamaat)

                MainCenterClockSection(
                    modifier = Modifier.weight(0.52f),
                    currentTime = currentTime,
                    digitalTimeString = digitalTimeString,
                    formattedDate = formattedDate,
                    currentJamaatTime = currentJamaatTime
                )

                QrCodeCard(upiId = upiId, modifier = Modifier.weight(0.24f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            PrayerTimesRow(modifier = Modifier.fillMaxWidth(), prayers = prayers)

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
    formattedDate: String,
    currentJamaatTime: String
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
                text = currentJamaatTime,
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
fun QrCodeCard(upiId: String, modifier: M
