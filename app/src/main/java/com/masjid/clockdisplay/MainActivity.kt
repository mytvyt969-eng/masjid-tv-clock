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

val BgTop = Color(0xFF0F2C20)
val BgBottom = Color(0xFF06140E)
val CardBg = Color(0x7F0B2218)
val CardBorder = Color(0x33A0D6B4)
val ActiveBorder = Color(0xFFEADB80)
val TextWhite = Color(0xFFFFFFFF)
val TextMuted = Color(0xB3FFFFFF)
val GoldAccent = Color(0xFFF3C759)
val MetallicRingOuter = Color(0xFFDCDCDC)
val MetallicRingInner = Color(0xFF888888)

data class PrayerTime(
    val icon: String,
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
                    colors = listOf(BgTop, BgBottom)
                )
            )
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HeaderSection(dateFormat.format(currentTime.time))

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
                    digitalTimeString = timeFormat.format(currentTime.time),
                    formattedDate = dateFormat.format(currentTime.time)
                )

                QrCodeCard(modifier = Modifier.weight(0.24f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            PrayerTimesRow(modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(10.dp))

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
fun JumuahCard(modifier: Modifier = Modifier) {
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
        PrayerTimeDetail("Athan", "12:15 PM")
        PrayerTimeDetail("Jamaat", "01:00 PM")
        PrayerTimeDetail("Khutba", "01:00 PM")
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
            .background(CardBg)
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
fun QrCodeCard(modifier: Modifier = Modifier) {
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
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("📱\nQR CODE", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, textAlign = TextAlign.Center)
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Scan To Stay\nConnected!",
            color = TextWhite,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun PrayerTimesRow(modifier: Modifier = Modifier) {
    val prayers = listOf(
        PrayerTime("🌤️", "Fajr", "04:30 AM", "05:00 AM"),
        PrayerTime("☀️", "Dhuhr", "01:00 PM", "01:15 PM"),
        PrayerTime("⛅", "Asr", "04:15 PM", "04:30 PM", isActive = true),
        PrayerTime("📖", "Maghrib", "05:55 PM", "05:58 PM"),
        PrayerTime("🌙", "Isha", "07:45 PM", "08:00 PM")
    )

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

        val hourAngle = Math.toRadians(((hour + minute / 60.0) * 30 - 90))
        drawLine(
            color = Color.White,
            start = center,
            end = Offset(
                center.x + (radius * 0.45f) * cos(hourAngle).toFloat(),
                center.y + (radius * 0.45f) * sin(hourAngle).toFloat()
            ),
            strokeWidth = 6.dp.toPx()
        )

        val minuteAngle = Math.toRadians(((minute + second / 60.0) * 6 - 90))
        drawLine(
            color = GoldAccent,
            start = center,
            end = Offset(
                center.x + (radius * 0.65f) * cos(minuteAngle).toFloat(),
                center.y + (radius * 0.65f) * sin(minuteAngle).toFloat()
            ),
            strokeWidth = 4.dp.toPx()
        )

        val secondAngle = Math.toRadians((second * 6 - 90).toDouble())
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
