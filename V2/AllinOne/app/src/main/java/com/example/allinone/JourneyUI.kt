package com.example.allinone

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun JourneyListScreen(
    category: String,
    onJourneySelected: (Journey) -> Unit,
    themeColor: Color
) {
    val journeys = DataManager.predefinedJourneys.filter { it.category == category }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "SELECT YOUR JOURNEY",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(journeys) { journey ->
                JourneyCard(journey, themeColor) { onJourneySelected(journey) }
            }
        }
    }
}

@Composable
fun JourneyCard(journey: Journey, themeColor: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        border = BorderStroke(1.dp, Color(0xFF333333))
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                Image(
                    painter = painterResource(id = journey.bannerRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color.Transparent, Color(0xFF1A1A1A)))
                ))
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                Text(journey.title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(journey.description, color = Color.Gray, fontSize = 13.sp, maxLines = 2)
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    journey.keyResults.take(4).forEach { res ->
                        Text(res.iconEmoji, fontSize = 18.sp, modifier = Modifier.padding(end = 8.dp))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text("VIEW DETAILS", color = themeColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun JourneyDetailScreen(
    journey: Journey,
    onBack: () -> Unit,
    onStart: () -> Unit,
    themeColor: Color
) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
            Image(
                painter = painterResource(id = journey.bannerRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, Color(0xFF000000)))
            ))
            
            IconButton(onClick = onBack, modifier = Modifier.padding(16.dp).align(Alignment.TopStart)) {
                Icon(painterResource(id = R.drawable.icons8_arrow_100_2), contentDescription = null, tint = Color.White)
            }
        }

        Column(modifier = Modifier.padding(20.dp)) {
            Text(journey.title.uppercase(), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(journey.description, color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))

            Spacer(modifier = Modifier.height(24.dp))

            Text("KEY RESULTS", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            journey.keyResults.forEach { result ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp)) {
                    Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF1A1A1A)), contentAlignment = Alignment.Center) {
                        Text(result.iconEmoji, fontSize = 20.sp)
                    }
                    Text(result.title, color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(start = 12.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("PHASES", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            journey.phases.forEach { phase ->
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text(phase.dayRange, color = themeColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(phase.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(phase.description, color = Color.Gray, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("START MY JOURNEY", fontWeight = FontWeight.Bold, color = Color.White)
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
