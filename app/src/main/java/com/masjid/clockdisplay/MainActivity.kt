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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
private val Muted = Color(0xCCFFFFFF)
private val Glass = Color(0x8A06274A)
private val Border = Color(0x55FFFFFF)
private val Gold = Color(0xFFFFD54F)

private data class Prayer(val name:String,val icon:String,val athan:String,val jamaat:String,val minutes:Int)
private enum class PrayerPeriod { FAJR, DHUHR, ASR, MAGHRIB, ISHA }
private data class ThemePalette(val top:Color,val middle:Color,val bottom:Color,val accent:Color,val activeBg:Color)

private fun palette(p:PrayerPeriod)=when(p){
    PrayerPeriod.FAJR->ThemePalette(Color(0xFF092B63),Color(0xFF684BA4),Color(0xFF102A44),Color(0xFF66D9FF),Color(0xFF007EC9))
    PrayerPeriod.DHUHR->ThemePalette(Color(0xFF0789E8),Color(0xFF54B8F2),Color(0xFF2E7D55),Color(0xFFFFD54F),Color(0xFFFCF2A6))
    PrayerPeriod.ASR->ThemePalette(Color(0xFF3A75AE),Color(0xFFF0A34A),Color(0xFF456B45),Color(0xFFFFC44D),Color(0xFFB56B00))
    PrayerPeriod.MAGHRIB->ThemePalette(Color(0xFF5B244D),Color(0xFFFF5A24),Color(0xFF1E172F),Color(0xFFFF7A2E),Color(0xFFB62E0C))
    PrayerPeriod.ISHA->ThemePalette(Color(0xFF03153F),Color(0xFF0B2F67),Color(0xFF020A1D),Color(0xFF2BD7FF),Color(0xFF005EAA))
}
private fun parseMinutes(v:String):Int{val p=v.trim().uppercase(Locale.getDefault()).split(" ");val h0=p[0].split(":");var h=h0[0].toInt();val m=h0[1].toInt();if(p.getOrNull(1)=="PM"&&h!=12)h+=12;if(p.getOrNull(1)=="AM"&&h==12)h=0;return h*60+m}
private fun periodFor(n:Calendar,ps:List<Prayer>):PrayerPeriod{val m=n.get(Calendar.HOUR_OF_DAY)*60+n.get(Calendar.MINUTE);return when{m<ps[0].minutes->PrayerPeriod.FAJR;m<ps[1].minutes->PrayerPeriod.DHUHR;m<ps[2].minutes->PrayerPeriod.ASR;m<ps[3].minutes->PrayerPeriod.MAGHRIB;else->PrayerPeriod.ISHA}}
private fun nextPrayer(ps:List<Prayer>,n:Calendar):Pair<Prayer,Long>{val now=n.get(Calendar.HOUR_OF_DAY)*60+n.get(Calendar.MINUTE);val c=ps.firstOrNull{it.minutes>now};val t=Calendar.getInstance().apply{timeInMillis=n.timeInMillis;set(Calendar.SECOND,0);set(Calendar.MILLISECOND,0)};val p=c?:ps.first();if(c==null)t.add(Calendar.DAY_OF_YEAR,1);t.set(Calendar.HOUR_OF_DAY,p.minutes/60);t.set(Calendar.MINUTE,p.minutes%60);return p to (t.timeInMillis-n.timeInMillis).coerceAtLeast(0)}
private fun countdown(ms:Long):String{val x=ms/1000;return String.format(Locale.getDefault(),"%02d:%02d:%02d",x/3600,(x%3600)/60,x%60)}

class MainActivity:ComponentActivity(){
    override fun onCreate(b:Bundle?){super.onCreate(b);window.decorView.systemUiVisibility=View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE;setContent{MasjidDashboard()}}
}

@Composable private fun MasjidDashboard(){
    val ps=remember{listOf(
        Prayer("Fajr","☀","5:47 AM","5:55 AM",parseMinutes("5:47 AM")),
        Prayer("Dhuhr","☀","12:15 PM","12:30 PM",parseMinutes("12:15 PM")),
        Prayer("Asr","☀","3:30 PM","3:45 PM",parseMinutes("3:30 PM")),
        Prayer("Maghrib","◉","5:49 PM","5:50 PM",parseMinutes("5:49 PM")),
        Prayer("Isha","☾","6:50 PM","6:55 PM",parseMinutes("6:50 PM"))
    )}
    var now by remember{mutableStateOf(Calendar.getInstance())}
    LaunchedEffect(Unit){while(true){now=Calendar.getInstance();delay(1000)}}
    val period=periodFor(now,ps);val theme=palette(period);val np=nextPrayer(ps,now);val date=SimpleDateFormat("EEE, dd MMM yyyy",Locale.getDefault()).format(now.time)
    Box(Modifier.fillMaxSize()){
        Image(painterResource(R.drawable.masjid_bg),null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(theme.top.copy(.60f),theme.middle.copy(.48f),theme.bottom.copy(.78f)))))
        Column(Modifier.fillMaxSize().padding(horizontal=28.dp,vertical=20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
            Header(date,"29 Rabi' al-Awwal 1448 AH")
            Row(Modifier.fillMaxWidth().weight(1f),horizontalArrangement=Arrangement.spacedBy(14.dp)){
                JumuahCard(Modifier.weight(.25f))
                CenterPanel(Modifier.weight(.50f),now,np.first,countdown(np.second),theme)
                RightInfo(Modifier.weight(.25f))
            }
            PrayerRow(ps,period,Modifier.fillMaxWidth().weight(.72f),theme)
            Footer()
        }
    }
}

@Composable private fun Header(date:String,hijri:String){
    Row(Modifier.fillMaxWidth(),Arrangement.SpaceBetween,Alignment.CenterVertically){
        Row(verticalAlignment=Alignment.CenterVertically){Text("🕌",fontSize=44.sp);Spacer(Modifier.width(12.dp));Column{Text("Jama Masjid KODWATAND, Lalpania",color=White,fontSize=25.sp,fontWeight=FontWeight.ExtraBold);Text("Bokaro, Jharkhand, India",color=White.copy(.92f),fontSize=16.sp)}}
        Column(horizontalAlignment=Alignment.End){Text(date,color=White,fontSize=20.sp,fontWeight=FontWeight.Bold);Text(hijri,color=White.copy(.9f),fontSize=15.sp)}
    }
}
@Composable private fun Glass(modifier:Modifier,content:@Composable ColumnScope.()->Unit){
    Column(modifier.clip(RoundedCornerShape(20.dp)).background(Glass).border(1.dp,Border,RoundedCornerShape(20.dp)).padding(16.dp),content=content)
}
@Composable private fun JumuahCard(modifier:Modifier){
    Glass(modifier){Column(Modifier.fillMaxSize(),Arrangement.SpaceEvenly){
        Row(verticalAlignment=Alignment.CenterVertically){Text("🕌",fontSize=30.sp);Spacer(Modifier.width(8.dp));Text("Jummah",color=White,fontSize=27.sp,fontWeight=FontWeight.ExtraBold)}
        HorizontalDivider(color=Border)
        Row(Modifier.fillMaxWidth(),Arrangement.SpaceEvenly){PairInfo("Adhan","1:15 PM");Box(Modifier.width(1.dp).height(58.dp).background(Border));PairInfo("Jamaat","1:30 PM")}
    }}
}
@Composable private fun PairInfo(label:String,value:String){
    Column(horizontalAlignment=Alignment.CenterHorizontally){Text(label,color=Muted,fontSize=15.sp);Text(value,color=White,fontSize=21.sp,fontWeight=FontWeight.ExtraBold)}
}
@Composable private fun CenterPanel(modifier:Modifier,now:Calendar,next:Prayer,cd:String,theme:ThemePalette){
    Box(modifier.fillMaxHeight()){
        Box(Modifier.fillMaxSize().padding(start=82.dp).clip(RoundedCornerShape(22.dp)).background(Color(0x8A09213B)).border(1.dp,Border,RoundedCornerShape(22.dp))){
            Row(Modifier.fillMaxSize().padding(start=105.dp,end=24.dp),verticalAlignment=Alignment.CenterVertically){
                Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally){
                    Row(verticalAlignment=Alignment.CenterVertically){Text(if(next.name=="Isha")"☾" else "☀",color=theme.accent,fontSize=38.sp);Spacer(Modifier.width(12.dp));Column{Text("Next Prayer",color=Muted,fontSize=18.sp);Text(next.name,color=White,fontSize=35.sp,fontWeight=FontWeight.ExtraBold)}}
                    Text(cd,color=White,fontSize=48.sp,fontWeight=FontWeight.ExtraBold,letterSpacing=2.sp)
                    Row(horizontalArrangement=Arrangement.spacedBy(42.dp)){Text("HOURS",color=Muted,fontSize=10.sp);Text("MINUTES",color=Muted,fontSize=10.sp);Text("SECONDS",color=Muted,fontSize=10.sp)}
                }
            }
        }
        AnalogClock(now,Modifier.size(205.dp).align(Alignment.CenterStart))
    }
}
@Composable private fun AnalogClock(now:Calendar,modifier:Modifier){
    Canvas(modifier){
        val c=Offset(size.width/2f,size.height/2f);val r=size.minDimension/2f;drawCircle(Color(0xFF111827),r);drawCircle(Color(0xFFF6F6F4),r-7f)
        for(i in 1..12){val a=Math.toRadians((i*30-90).toDouble());drawCircle(Color(0xFF172033),if(i%3==0)4f else 2.2f,Offset(c.x+(r-24f)*cos(a).toFloat(),c.y+(r-24f)*sin(a).toFloat()))}
        val h=now.get(Calendar.HOUR);val m=now.get(Calendar.MINUTE);val s=now.get(Calendar.SECOND);val ha=Math.toRadians(((h+m/60.0)*30-90));val ma=Math.toRadians(((m+s/60.0)*6-90));val sa=Math.toRadians((s*6-90).toDouble())
        drawLine(Color(0xFF1A2333),c,Offset(c.x+r*.46f*cos(ha).toFloat(),c.y+r*.46f*sin(ha).toFloat()),6f,StrokeCap.Round)
        drawLine(Color(0xFF1A2333),c,Offset(c.x+r*.68f*cos(ma).toFloat(),c.y+r*.68f*sin(ma).toFloat()),4f,StrokeCap.Round)
        drawLine(Color(0xFFE1A900),c,Offset(c.x+r*.76f*cos(sa).toFloat(),c.y+r*.76f*sin(sa).toFloat()),2.5f,StrokeCap.Round);drawCircle(Color(0xFFE1A900),6f,c)
    }
}
@Composable private fun RightInfo(modifier:Modifier){
    Column(modifier,verticalArrangement=Arrangement.spacedBy(12.dp)){
        Glass(Modifier.fillMaxWidth().weight(1f)){Column(Modifier.fillMaxSize(),Arrangement.SpaceEvenly){Text("☀",color=Gold,fontSize=34.sp,textAlign=TextAlign.Center,modifier=Modifier.fillMaxWidth());Row(Modifier.fillMaxWidth(),Arrangement.SpaceEvenly){PairInfo("Fajar Start","4:37 AM");Box(Modifier.width(1.dp).height(58.dp).background(Border));PairInfo("Fajar End","6:05 AM")}}}
        Glass(Modifier.fillMaxWidth().weight(.62f)){Row(Modifier.fillMaxSize(),Arrangement.SpaceEvenly,Alignment.CenterVertically){PairInfo("☀  Sunrise","6:40 AM");Box(Modifier.width(1.dp).height(42.dp).background(Border));PairInfo("🌇  Sunset","7:32 PM")}}
    }
}
@Composable private fun PrayerRow(ps:List<Prayer>,period:PrayerPeriod,modifier:Modifier,theme:ThemePalette){
    Row(modifier,horizontalArrangement=Arrangement.spacedBy(12.dp)){ps.forEach{p->val active=when(period){PrayerPeriod.FAJR->p.name=="Fajr";PrayerPeriod.DHUHR->p.name=="Dhuhr";PrayerPeriod.ASR->p.name=="Asr";PrayerPeriod.MAGHRIB->p.name=="Maghrib";PrayerPeriod.ISHA->p.name=="Isha"};PrayerTile(p,active,theme,Modifier.weight(1f))}}
}
@Composable private fun PrayerTile(p:Prayer,active:Boolean,theme:ThemePalette,modifier:Modifier){
    val bc by animateColorAsState(if(active)theme.accent else Color(0x663A516B),label="active")
    Column(modifier.clip(RoundedCornerShape(20.dp)).background(if(active)theme.activeBg.copy(.82f) else Color(0xDDF2F4F7)).border(if(active)2.dp else 1.dp,bc,RoundedCornerShape(20.dp)).padding(horizontal=12.dp,vertical=10.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.SpaceEvenly){
        Row(verticalAlignment=Alignment.CenterVertically){Text(p.icon,color=if(active)theme.accent else Color(0xFFFFC928),fontSize=25.sp);Spacer(Modifier.width(7.dp));Text(p.name,color=if(active)White else Color(0xFF17233A),fontSize=23.sp,fontWeight=FontWeight.ExtraBold)}
        Text("Adhan",color=if(active)Muted else Color(0xFF26364D),fontSize=14.sp);Text(p.athan,color=if(active)White else Color(0xFF17233A),fontSize=20.sp,fontWeight=FontWeight.ExtraBold)
        Box(Modifier.width(80.dp).height(1.dp).background(if(active)Border else Color(0x55203045)))
        Text("Iqamah",color=if(active)Muted else Color(0xFF26364D),fontSize=14.sp);Text(p.jamaat,color=if(active)White else Color(0xFF17233A),fontSize=20.sp,fontWeight=FontWeight.ExtraBold)
    }
}
@Composable private fun Footer(){
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0x9A061B32)).border(1.dp,Border,RoundedCornerShape(10.dp)).padding(horizontal=16.dp,vertical=9.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){
        Text("🔊",fontSize=23.sp);Text("Welcome to Jama Masjid KODWATAND, Lalpania",color=White,fontSize=14.sp);Text("|",color=Muted,fontSize=17.sp);Text("May Allah accept our prayers",color=White,fontSize=14.sp);Text("|",color=Muted,fontSize=17.sp);Text("Stay connected with our community",color=White,fontSize=14.sp)
    }
}
