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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
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

    var upiId by remember { mutableStateOf(prefs.getString("upi_id", "ejazh10472@ybl") ?: "ejazh10472@ybl") }
    var countdownMinutesTrigger by remember { mutableIntStateOf(prefs.getInt("cd_minutes", 3)) }
    var audioAlertEnabled by remember { mutableStateOf(prefs.getBoolean("audio_alert", true)) }

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
                onUpiChange = { newId ->
                    upiId = newId
                    prefs.edit().putString("upi_id", newId).apply()
                },
                countdownMinutes = countdownMinutesTrigger,
                onCountdownChange = { mins ->
                    countdownMinutesTrigger = mins
                    prefs.edit().putInt("cd_minutes", mins).apply()
                },
                audioEnabled = audioAlertEnabled,
                onAudioToggle = { enabled ->
                    audioAlertEnabled = enabled
                    prefs.edit().putBoolean("audio_alert", enabled).apply()
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
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.masjid_bg),
            contentDescription = "Masjid Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.55f)
                        )
                    )
                )
        )

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
    fajrAthan: String, onFajrAthanChange: (String) -> Unit,
    fajrJamaat: String, onFajrJamaatChange: (String) -> Unit,
    dhuhrAthan: String, onDhuhrAthanChange: (String) -> Unit,
    dhuhrJamaat: String, onDhuhrJamaatChange: (String) -> Unit,
    asrAthan: String, onAsrAthanChange: (String) -> Unit,
    asrJamaat: String, onAsrJamaatChange: (String) -> Unit,
    maghribAthan: String, onMaghribAthanChange: (String) -> Unit,
    maghribJamaat: String, onMaghribJamaatChange: (String) -> Unit,
    ishaAthan: String, onIshaAthanChange: (String) -> Unit,
    ishaJamaat: String, onIshaJamaatChange: (String) -> Unit,
    jumuahAthan: String, onJumuahAthanChange: (String) -> Unit,
    jumuahJamaat: String, onJumuahJamaatChange: (String) -> Unit,
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
                .width(550.dp)
                .heightIn(max = 520.dp)
                .border(2.dp, GoldAccent, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                    ),
                    modifier = Modifier.fillMaxWidth()
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

                        Button(
                            onClick = { if (countdownMinutes < 15) onCountdownChange(countdownMinutes + 1) },
                            colors = ButtonDefaults.buttonColors(containerColor = CardBg)
                        ) { Text("+", color = TextWhite) }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Athan/Jamaat Alert Sound:", color = TextWhite, fontSize = 14.sp)
                    Switch(checked = audioEnabled, onCheckedChange = onAudioToggle)
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = CardBorder)
                Spacer(modifier = Modifier.height(12.dp))

                Text("🕌 PRAYER TIMES SETTINGS", color = GoldAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                PrayerEditRow("Fajr", fajrAthan, onFajrAthanChange, fajrJamaat, onFajrJamaatChange)
                PrayerEditRow("Dhuhr", dhuhrAthan, onDhuhrAthanChange, dhuhrJamaat, onDhuhrJamaatChange)
                PrayerEditRow("Asr", asrAthan, onAsrAthanChange, asrJamaat, onAsrJamaatChange)
                PrayerEditRow("Maghrib", maghribAthan, onMaghribAthanChange, maghribJamaat, onMaghribJamaatChange)
                PrayerEditRow("Isha", ishaAthan, onIshaAthanChange, ishaJamaat, onIshaJamaatChange)
                PrayerEditRow("Jumu'ah", jumuahAthan, onJumuahAthanChange, jumuahJamaat, onJumuahJamaatChange)

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Text("SAVE & CLOSE", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PrayerEditRow(
    name: String,
    athan: String,
    onAthanChange: (String) -> Unit,
    jamaat: String,
    onJamaatChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.25f))

        OutlinedTextField(
            value = athan,
            onValueChange = onAthanChange,
            label = { Text("Athan", color = TextMuted, fontSize = 10.sp) },
            singleLine = true,
            modifier = Modifier
                .weight(0.35f)
                .padding(end = 4.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedBorderColor = GoldAccent,
                unfocusedBorderColor = CardBorder
            )
        )

        OutlinedTextField(
            value = jamaat,
            onValueChange = onJamaatChange,
            label = { Text("Jamaat", color = TextMuted, fontSize = 10.sp) },
            singleLine = true,
            modifier = Modifier
                .weight(0.35f)
                .padding(start = 4.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedBorderColor = GoldAccent,
                unfocusedBorderColor = CardBorder
            )
        )
    }
}

fun generateUpiQrBitmap(upiId: String): Bitmap? {
    return try {
        val uri = "upi://pay?pa=$upiId&pn=Jama%20Masjid&cu=INR"
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(uri, BarcodeFormat.QR_CODE, 300, 300)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bmp
    } catch (e: Exception) {
        null
    }
}

fun playAudioAlert(context: Context) {
    try {
        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val r = RingtoneManager.getRingtone(context, notification)
        r.play()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun HeaderSection(formattedDate: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFE2ECE5), Color(0xFFB5C9BC))
                )
            )
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "🕌 ", fontSize = 24.sp)
            Column {
                Text(
                    text = "JAMA MASJID KODWATAND, LALPANIA",
                    color = Color(0xFF091C13),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "BOKARO, JHARKHAND",
                    color = Color(0xFF2A4738),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "🌅 ", fontSize = 20.sp)
            Column {
                Text(text = "Sunrise", color = Color(0xFF2A4738), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(text = "05:34 AM", color = Color(0xFF091C13), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(modifier = Modifier.width(20.dp))
            Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color(0xFF8BA594)))
            Spacer(modifier = Modifier.width(20.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Rabi' al-Thani 4, 1448 AH", color = Color(0xFF091C13), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                Text(text = formattedDate, color = Color(0xFF2A4738), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun JumuahCard(modifier: Modifier = Modifier, athan: String, jamaat: String) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("📋 ", fontSize = 18.sp)
            Text("Jumu'ah", color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Text("🕌", fontSize = 16.sp)
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CardBorder))
        PrayerTimeDetail("Athan", athan)
        PrayerTimeDetail("Jamaat", jamaat)
        PrayerTimeDetail("Khutba", jamaat)
    }
}

@Composable
fun PrayerTimeDetail(label: String, time: String) {
    Column {
        Text(text = label, color = TextMuted, fontSize = 12.sp)
        Text(text = time, color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PrayerTimesRow(modifier: Modifier = Modifier, prayers: List<PrayerTime>) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        prayers.forEach { prayer ->
            PrayerCard(prayer = prayer, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun PrayerCard(prayer: PrayerTime, modifier: Modifier = Modifier) {
    val borderColor by animateColorAsState(
        targetValue = if (prayer.isActive) ActiveBorder else CardBorder, label = "borderAnim"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (prayer.isActive) Color(0x33FFD700) else CardBg)
            .border(if (prayer.isActive) 2.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(prayer.icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = prayer.name, color = TextWhite, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CardBorder))
        Spacer(modifier = Modifier.height(4.dp))

        Text(text = "Athan", color = TextMuted, fontSize = 11.sp)
        Text(text = prayer.athan, color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)

        Spacer(modifier = Modifier.height(4.dp))

        Text(text = "Jamaat", color = TextMuted, fontSize = 11.sp)
        Text(
            text = prayer.jamaat,
            color = if (prayer.isActive) ActiveBorder else TextWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun BottomTickerSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "📖  “And establish prayer and give zakah, and bow with those who bow [in worship and obedience].” — Surah Al-Baqarah (2:43)",
            color = TextWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "🔊 Keep Our Masjid Clean For A Better Tomorrow",
            color = GoldAccent,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ModernAnalogClock(currentTime: Calendar, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2

        drawCircle(
            brush = Brush.sweepGradient(
                colors = listOf(MetallicRingOuter, MetallicRingInner, MetallicRingOuter)
            ),
            radius = radius,
            center = center,
            style = Stroke(width = 10.dp.toPx())
        )

        drawCircle(
            color = Color(0xFF091C13),
            radius = radius - 5.dp.toPx(),
            center = center
        )

        for (i in 1..12) {
            val angle = Math.toRadians((i * 30 - 90).toDouble())
            val isMainTick = i % 3 == 0
            val tickLength = if (isMainTick) 14.dp.toPx() else 8.dp.toPx()

            val startX = center.x + (radius - 20.dp.toPx()) * cos(angle).toFloat()
            val startY = center.y + (radius - 20.dp.toPx()) * sin(angle).toFloat()
            val endX = center.x + (radius - (20.dp.toPx() + tickLength)) * cos(angle).toFloat()
            val endY = center.y + (radius - (20.dp.toPx() + tickLength)) * sin(angle).toFloat()

            drawLine(
                color = GoldAccent,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = if (isMainTick) 4.dp.toPx() else 2.dp.toPx()
            )
        }

        val hour = currentTime.get(Calendar.HOUR)
        val minute = currentTime.get(Calendar.MINUTE)
        val second = currentTime.get(Calendar.SECOND)

        val hourAngle = Math.toRadians(((hour + minute / 60.0) * 30.0 - 90.0))
        drawLine(
            color = Color.White,
            start = center,
            end = Offset(
                center.x + (radius * 0.45f) * cos(hourAngle).toFloat(),
                center.y + (radius * 0.45f) * sin(hourAngle).toFloat()
            ),
            strokeWidth = 6.dp.toPx()
        )

        val minuteAngle = Math.toRadians(((minute + second / 60.0) * 6.0 - 90.0))
        drawLine(
            color = GoldAccent,
            start = center,
            end = Offset(
                center.x + (radius * 0.65f) * cos(minuteAngle).toFloat(),
                center.y + (radius * 0.65f) * sin(minuteAngle).toFloat()
            ),
            strokeWidth = 4.dp.toPx()
        )

        val secondAngle = Math.toRadians((second * 6.0 - 90.0))
        drawLine(
            color = Color.Red,
            start = center,
            end = Offset(
                center.x + (radius * 0.75f) * cos(secondAngle).toFloat(),
                center.y + (radius * 0.75f) * sin(secondAngle).toFloat()
            ),
            strokeWidth = 2.dp.toPx()
        )

        drawCircle(color = GoldAccent, radius = 6.dp.toPx(), center = center)
    }
}
