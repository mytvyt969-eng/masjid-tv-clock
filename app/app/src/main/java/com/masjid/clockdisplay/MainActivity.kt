package com.masjid.clockdisplay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

val BackgroundDeepGreen = Color(0xFF0D251C)
val BackgroundGradientLight = Color(0xFF143E2E)
val CardBackground = Color(0x66112C21)
val GlassBorder = Color(0x33A0D6B4)
val ActiveBorderYellow = Color(0xFFFFD700)
val TextWhite = Color(0xFFFFFFFF)
val TextMuted = Color(0xB3FFFFFF)
val GoldAccent = Color(0xFFE5C158)

data class PrayerTime(
    val name: String,
    val athan: String,
    val jamaat: String,
    val isActive: Boolean = false
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MasjidDashboard()
        }
    }
}

@Composable
fun MasjidDashboard() {
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Calendar.getInstance()
            delay(1000)
        }
    }

    val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
    val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(BackgroundGradientLight, BackgroundDeepGreen)
                )
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HeaderSection(dateFormat.format(currentTime.time))

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                JumuahCard(modifier = Modifier.weight(0.22f))

                MainCenterClockSection(
                    modifier = Modifier.weight(0.56f),
                    currentTime = currentTime,
                    digitalTimeString = timeFormat.format(currentTime.time)
                )

                QrCodeCard(modifier = Modifier.weight(0.22f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            PrayerTimesRow(modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(12.dp))

            BottomTickerSection()
        }
    }
}

@Composable
fun HeaderSection(formattedDate: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "JAMA MASJID KODWATAND, LALPANIA",
                color = TextWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "BOKARO, JHARKHAND",
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Sunrise", color = TextMuted, fontSize = 12.sp)
                Text(text = "05:34 AM", color = GoldAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(24.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Rabi' al-Thani 4, 1448 AH", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(text = formattedDate, color = TextMuted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun JumuahCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text("🕌 Jumu'ah", color = GoldAccent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(GlassBorder))
        PrayerTimeDetail("Athan", "12:15 PM")
        PrayerTimeDetail("Jamaat", "01:00 PM")
        PrayerTimeDetail("Khutba", "01:00 PM")
    }
}

@Composable
fun PrayerTimeDetail(label: String, time: String) {
    Column {
        Text(text = label, color = TextMuted, fontSize = 11.sp)
        Text(text = time, color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun MainCenterClockSection(
    modifier: Modifier = Modifier,
    currentTime: Calendar,
    digitalTimeString: String
) {
    Row(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        ModernAnalogClock(currentTime = currentTime, modifier = Modifier.size(190.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Asr Jamaat",
                color = TextWhite,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "04:30 PM",
                color = TextWhite,
                fontSize = 44.sp,
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
fun QrCodeCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("QR CODE", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Scan To Stay Connected!",
            color = TextWhite,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PrayerTimesRow(modifier: Modifier = Modifier) {
    val prayers = listOf(
        PrayerTime("Fajr", "04:30 AM", "05:00 AM"),
        PrayerTime("Dhuhr", "01:00 PM", "01:15 PM"),
        PrayerTime("Asr", "04:15 PM", "04:30 PM", isActive = true),
        PrayerTime("Maghrib", "05:55 PM", "05:58 PM"),
        PrayerTime("Isha", "07:45 PM", "08:00 PM")
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        prayers.forEach { prayer ->
            PrayerCard(prayer = prayer, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun PrayerCard(prayer: PrayerTime, modifier: Modifier = Modifier) {
    val borderColor by animateColorAsState(
        targetValue = if (prayer.isActive) ActiveBorderYellow else GlassBorder, label = "borderAnim"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (prayer.isActive) Color(0x99174533) else CardBackground)
            .border(if (prayer.isActive) 2.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = prayer.name, color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(GlassBorder))
        Spacer(modifier = Modifier.height(6.dp))

        Text(text = "Athan", color = TextMuted, fontSize = 11.sp)
        Text(text = prayer.athan, color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)

        Spacer(modifier = Modifier.height(6.dp))

        Text(text = "Jamaat", color = TextMuted, fontSize = 11.sp)
        Text(
            text = prayer.jamaat,
            color = if (prayer.isActive) ActiveBorderYellow else TextWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun BottomTickerSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CardBackground)
            .border(1.dp, GlassBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "📖 “And establish prayer and give zakah, and bow with those who bow [in worship and obedience].” — Surah Al-Baqarah (2:43)",
            color = TextWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "🔔 Keep Our Masjid Clean For A Better Tomorrow",
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
            color = Color(0xFFC5A059),
            radius = radius,
            center = center,
            style = Stroke(width = 8.dp.toPx())
        )

        drawCircle(
            color = Color(0xFF091A13),
            radius = radius - 4.dp.toPx(),
            center = center
        )

        for (i in 1..12) {
            val angle = Math.toRadians((i * 30 - 90).toDouble())
            val startX = center.x + (radius - 20.dp.toPx()) * cos(angle).toFloat()
            val startY = center.y + (radius - 10.dp.toPx()) * cos(angle).toFloat()
            val endX = center.x + (radius - 10.dp.toPx()) * cos(angle).toFloat()
            val endY = center.y + (radius - 10.dp.toPx()) * sin(angle).toFloat()

            drawLine(
                color = GoldAccent,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 3.dp.toPx()
            )
        }

        val hour = currentTime.get(Calendar.HOUR)
        val minute = currentTime.get(Calendar.MINUTE)
        val second = currentTime.get(Calendar.SECOND)

        val hourAngle = Math.toRadians(((hour + minute / 60.0) * 30 - 90))
        drawLine(
            color = Color.White,
            start = center,
            end = Offset(
                center.x + (radius * 0.5f) * cos(hourAngle).toFloat(),
                center.y + (radius * 0.5f) * sin(hourAngle).toFloat()
            ),
            strokeWidth = 5.dp.toPx()
        )

        val minuteAngle = Math.toRadians(((minute + second / 60.0) * 6 - 90))
        drawLine(
            color = GoldAccent,
            start = center,
            end = Offset(
                center.x + (radius * 0.7f) * cos(minuteAngle).toFloat(),
                center.y + (radius * 0.7f) * sin(minuteAngle).toFloat()
            ),
            strokeWidth = 3.5.dp.toPx()
        )

        drawCircle(color = GoldAccent, radius = 5.dp.toPx(), center = center)
    }
}
