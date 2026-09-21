package com.masjid.clockdisplay

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

private val White = Color.White
private val Muted = Color(0xD9FFFFFF)
private val Glass = Color(0xA80A3158)
private val Border = Color(0x668ED7FF)
private val Gold = Color(0xFFFFD54F)

private data class Prayer(val name:String,val icon:String,val athan:String,val jamaat:String,val minutes:Int)
private enum class PrayerPeriod { FAJR, DHUHR, ASR, MAGHRIB, ISHA }

private fun parseMinutes(v:String):Int {
    val p=v.trim().uppercase(Locale.getDefault()).split(" ")
    val hm=p[0].split(":")
    var h=hm[0].toInt()
    val m=hm[1].toInt()
    if(p.getOrNull(1)=="PM"&&h!=12) h+=12
    if(p.getOrNull(1)=="AM"&&h==12) h=0
    return h*60+m
}
private fun periodFor(now:Calendar,ps:List<Prayer>):PrayerPeriod {
    val m=now.get(Calendar.HOUR_OF_DAY)*60+now.get(Calendar.MINUTE)
    return when {
        m<ps[0].minutes->PrayerPeriod.FAJR
        m<ps[1].minutes->PrayerPeriod.DHUHR
        m<ps[2].minutes->PrayerPeriod.ASR
        m<ps[3].minutes->PrayerPeriod.MAGHRIB
        else->PrayerPeriod.ISHA
    }
}
private fun nextPrayer(ps:List<Prayer>,now:Calendar):Pair<Prayer,Long> {
    val current=now.get(Calendar.HOUR_OF_DAY)*60+now.get(Calendar.MINUTE)
    val upcoming=ps.firstOrNull{it.minutes>current}
    val target=Calendar.getInstance().apply{
        timeInMillis=now.timeInMillis
        set(Calendar.SECOND,0);set(Calendar.MILLISECOND,0)
    }
    val prayer=upcoming?:ps.first()
    if(upcoming==null) target.add(Calendar.DAY_OF_YEAR,1)
    target.set(Calendar.HOUR_OF_DAY,prayer.minutes/60)
    target.set(Calendar.MINUTE,prayer.minutes%60)
    return prayer to (target.timeInMillis-now.timeInMillis).coerceAtLeast(0)
}
private fun countdown(ms:Long):String {
    val total=ms/1000
    return String.format(Locale.getDefault(),"%02d:%02d:%02d",total/3600,(total%3600)/60,total%60)
}

class MainActivity:ComponentActivity(){
    override fun onCreate(state:Bundle?){
        super.onCreate(state)
        window.decorView.systemUiVisibility=View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        setContent{MasjidDashboard()}
    }
}

@Composable
private fun MasjidDashboard(){
    val prayers=remember{listOf(
        Prayer("Fajr","☀","5:47 AM","5:55 AM",parseMinutes("5:47 AM")),
        Prayer("Dhuhr","☀","12:15 PM","12:30 PM",parseMinutes("12:15 PM")),
        Prayer("Asr","☀","3:30 PM","3:45 PM",parseMinutes("3:30 PM")),
        Prayer("Maghrib","☀","5:49 PM","5:50 PM",parseMinutes("5:49 PM")),
        Prayer("Isha","☾","6:50 PM","6:55 PM",parseMinutes("6:50 PM"))
    )}
    var now by remember{mutableStateOf(Calendar.getInstance())}
    LaunchedEffect(Unit){while(true){now=Calendar.getInstance();delay(1000)}}
    val currentPeriod=periodFor(now,prayers)
    val next=nextPrayer(prayers,now)
    val date=SimpleDateFormat("EEE, dd MMM yyyy",Locale.getDefault()).format(now.time)

    Box(Modifier.fillMaxSize()){
        Image(painterResource(R.drawable.masjid_bg),null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Color(0x4D00152F)))
        Column(
            Modifier.fillMaxSize().padding(horizontal=38.dp,vertical=18.dp),
            verticalArrangement=Arrangement.spacedBy(10.dp)
        ){
            Header(date)
            Row(Modifier.fillMaxWidth().weight(1.12f),horizontalArrangement=Arrangement.spacedBy(14.dp)){
                JummahCard(Modifier.weight(.26f))
                CenterPanel(Modifier.weight(.48f),now,next.first,countdown(next.second))
                RightInfo(Modifier.weight(.26f))
            }
            PrayerRow(prayers,currentPeriod,Modifier.fillMaxWidth().weight(.94f))
            Footer()
        }
    }
}

@Composable
private fun Header(date:String){
    Row(Modifier.fillMaxWidth(),Arrangement.SpaceBetween,Alignment.CenterVertically){
        Row(verticalAlignment=Alignment.CenterVertically){
            Text("🕌",color=White,fontSize=44.sp)
            Spacer(Modifier.width(12.dp))
            Column{
                Text("Jama Masjid KODWATAND, Lalpania",color=White,fontSize=25.sp,fontWeight=FontWeight.ExtraBold)
                Text("Bokaro, Jharkhand, India",color=Muted,fontSize=16.sp)
            }
        }
        Column(horizontalAlignment=Alignment.End){
            Text(date,color=White,fontSize=20.sp,fontWeight=FontWeight.Bold)
            Text("29 Rabi' al-Awwal 1448 AH",color=Muted,fontSize=15.sp)
        }
    }
}

@Composable
private fun GlassCard(modifier:Modifier,content:@Composable ColumnScope.()->Unit){
    Column(
        modifier.clip(RoundedCornerShape(18.dp)).background(Glass).border(1.dp,Border,RoundedCornerShape(18.dp)).padding(16.dp),
        content=content
    )
}

@Composable
private fun JummahCard(modifier:Modifier){
    GlassCard(modifier){
        Column(Modifier.fillMaxSize(),Arrangement.SpaceEvenly){
            Row(verticalAlignment=Alignment.CenterVertically){
                Text("🕌",color=White,fontSize=31.sp)
                Spacer(Modifier.width(9.dp))
                Text("Jummah",color=White,fontSize=28.sp,fontWeight=FontWeight.ExtraBold)
            }
            HorizontalDivider(color=Border)
            Row(Modifier.fillMaxWidth(),Arrangement.SpaceEvenly){
                TimePair("Adhan","1:15 PM")
                Box(Modifier.width(1.dp).height(58.dp).background(Border))
                TimePair("Jamaat","1:30 PM")
            }
        }
    }
}

@Composable
private fun TimePair(label:String,value:String){
    Column(horizontalAlignment=Alignment.CenterHorizontally){
        Text(label,color=Muted,fontSize=15.sp)
        Text(value,color=White,fontSize=21.sp,fontWeight=FontWeight.ExtraBold)
    }
}

@Composable
private fun CenterPanel(modifier:Modifier,now:Calendar,next:Prayer,cd:String){
    Box(modifier.fillMaxHeight()){
        Box(
            Modifier.fillMaxSize().padding(start=86.dp).clip(RoundedCornerShape(20.dp))
                .background(Color(0x990A3158)).border(1.dp,Border,RoundedCornerShape(20.dp))
        ){
            Column(
                Modifier.fillMaxSize().padding(start=88.dp,end=18.dp),
                horizontalAlignment=Alignment.CenterHorizontally,
                verticalArrangement=Arrangement.Center
            ){
                Row(verticalAlignment=Alignment.CenterVertically){
                    Text(if(next.name=="Isha")"☾" else "☀",color=if(next.name=="Isha")White else Gold,fontSize=39.sp)
                    Spacer(Modifier.width(12.dp))
                    Column{
                        Text("Next Prayer",color=Muted,fontSize=18.sp)
                        Text(next.name,color=White,fontSize=35.sp,fontWeight=FontWeight.ExtraBold)
                    }
                }
                Text(cd,color=White,fontSize=48.sp,fontWeight=FontWeight.ExtraBold,letterSpacing=1.sp)
                Row(horizontalArrangement=Arrangement.spacedBy(42.dp)){
                    Text("HOURS",color=Muted,fontSize=10.sp)
                    Text("MINUTES",color=Muted,fontSize=10.sp)
                    Text("SECONDS",color=Muted,fontSize=10.sp)
                }
            }
        }
        AnalogClock(now,Modifier.size(205.dp).align(Alignment.CenterStart))
    }
}

@Composable
private fun AnalogClock(now:Calendar,modifier:Modifier){
    Canvas(modifier){
        val center=Offset(size.width/2f,size.height/2f)
        val radius=size.minDimension/2f
        drawCircle(Color(0xFF111827),radius)
        drawCircle(Color(0xFFF7F7F5),radius-7f)
        for(i in 1..12){
            val a=Math.toRadians((i*30-90).toDouble())
            drawCircle(Color(0xFF172033),if(i%3==0)4f else 2.2f,Offset(center.x+(radius-25f)*cos(a).toFloat(),center.y+(radius-25f)*sin(a).toFloat()))
        }
        val h=now.get(Calendar.HOUR)
        val m=now.get(Calendar.MINUTE)
        val s=now.get(Calendar.SECOND)
        fun point(length:Float,degrees:Double)=Offset(
            center.x+length*cos(Math.toRadians(degrees-90)).toFloat(),
            center.y+length*sin(Math.toRadians(degrees-90)).toFloat()
        )
        drawLine(Color(0xFF182230),center,point(radius*.46f,h*30.0+m/2.0),6f,StrokeCap.Round)
        drawLine(Color(0xFF182230),center,point(radius*.68f,m*6.0+s/10.0),4f,StrokeCap.Round)
        drawLine(Color(0xFFE1A900),center,point(radius*.76f,s*6.0),2.5f,StrokeCap.Round)
        drawCircle(Color(0xFFE1A900),6f,center)
    }
}

@Composable
private fun RightInfo(modifier:Modifier){
    Column(modifier,Arrangement.spacedBy(12.dp)){
        GlassCard(Modifier.fillMaxWidth().weight(1f)){
            Column(Modifier.fillMaxSize(),Arrangement.SpaceEvenly){
                Text("☀",color=Gold,fontSize=35.sp,modifier=Modifier.fillMaxWidth(),textAlign=TextAlign.Center)
                Row(Modifier.fillMaxWidth(),Arrangement.SpaceEvenly){
                    TimePair("Fajar Start","4:37 AM")
                    Box(Modifier.width(1.dp).height(58.dp).background(Border))
                    TimePair("Fajar End","6:05 AM")
                }
            }
        }
        GlassCard(Modifier.fillMaxWidth().weight(.62f)){
            Row(Modifier.fillMaxSize(),Arrangement.SpaceEvenly,Alignment.CenterVertically){
                TimePair("☀  Sunrise","6:40 AM")
                Box(Modifier.width(1.dp).height(42.dp).background(Border))
                TimePair("🌇  Sunset","7:32 PM")
            }
        }
    }
}

@Composable
private fun PrayerRow(prayers:List<Prayer>,activePeriod:PrayerPeriod,modifier:Modifier){
    Row(modifier,Arrangement.spacedBy(12.dp)){
        prayers.forEach{prayer->
            val active=when(activePeriod){
                PrayerPeriod.FAJR->prayer.name=="Fajr"
                PrayerPeriod.DHUHR->prayer.name=="Dhuhr"
                PrayerPeriod.ASR->prayer.name=="Asr"
                PrayerPeriod.MAGHRIB->prayer.name=="Maghrib"
                PrayerPeriod.ISHA->prayer.name=="Isha"
            }
            PrayerTile(prayer,active,Modifier.weight(1f))
        }
    }
}

@Composable
private fun PrayerTile(prayer:Prayer,active:Boolean,modifier:Modifier){
    val borderColor by animateColorAsState(if(active)Color(0xFF36D8FF)else Color(0x6685CFF0),label="prayerBorder")
    Column(
        modifier.clip(RoundedCornerShape(18.dp))
            .background(if(active)Color(0xB51A5682)else Color(0x8F0B3152))
            .border(if(active)2.dp else 1.dp,borderColor,RoundedCornerShape(18.dp))
            .padding(horizontal=12.dp,vertical=10.dp),
        horizontalAlignment=Alignment.CenterHorizontally,
        verticalArrangement=Arrangement.SpaceEvenly
    ){
        Row(verticalAlignment=Alignment.CenterVertically){
            Text(prayer.icon,color=if(prayer.name=="Isha")White else Color(0xFFFFD21F),fontSize=25.sp)
            Spacer(Modifier.width(7.dp))
            Text(prayer.name,color=White,fontSize=23.sp,fontWeight=FontWeight.ExtraBold)
        }
        Text("Adhan",color=Muted,fontSize=14.sp)
        Text(prayer.athan,color=White,fontSize=20.sp,fontWeight=FontWeight.ExtraBold)
        Box(Modifier.width(80.dp).height(1.dp).background(Color(0x99FFFFFF)))
        Text("Iqamah",color=Muted,fontSize=14.sp)
        Text(prayer.jamaat,color=White,fontSize=20.sp,fontWeight=FontWeight.ExtraBold)
    }
}

@Composable
private fun Footer(){
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xB80A223C))
            .border(1.dp,Border,RoundedCornerShape(10.dp)).padding(horizontal=16.dp,vertical=9.dp),
        verticalAlignment=Alignment.CenterVertically,
        horizontalArrangement=Arrangement.SpaceBetween
    ){
        Text("🔊",fontSize=23.sp)
        Text("Welcome to Jama Masjid KODWATAND, Lalpania",color=White,fontSize=14.sp)
        Text("|",color=Muted,fontSize=17.sp)
        Text("May Allah accept our prayers",color=White,fontSize=14.sp)
        Text("|",color=Muted,fontSize=17.sp)
        Text("Stay connected with our community",color=White,fontSize=14.sp)
        Text("LIVE",color=Muted,fontSize=13.sp,fontWeight=FontWeight.Bold)
    }
}
