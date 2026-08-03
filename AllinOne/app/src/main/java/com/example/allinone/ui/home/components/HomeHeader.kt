package com.example.allinone.ui.home.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allinone.R
import com.example.allinone.ui.home.DashboardState
import com.example.allinone.LocalAppStyle
import com.example.allinone.core.utils.UIUtils
import java.util.*

@Composable
fun HomeHeader(
    state: DashboardState,
    isSearchVisible: Boolean,
    onSearchToggle: () -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onMoodSelected: (String) -> Unit,
    onSearchRequested: (String) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    moodTheme: Pair<Color, String>,
    smartGreeting: String,
    showRedDot: Boolean
) {
    val style = LocalAppStyle.current
    val interactionSource = remember { MutableInteractionSource() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var isMessageExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(moodTheme.first.copy(alpha = 0.6f), Color.Black)
                )
            )
            .statusBarsPadding()
            .padding(top = 0.dp, bottom = 12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Profile + Personal Greeting Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .animateContentSize()
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { isMessageExpanded = !isMessageExpanded },
                    horizontalAlignment = Alignment.Start
                ) {
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onProfileClick(); isMessageExpanded = false }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(style.surfaceColor)
                                .border(1.5.dp, moodTheme.first.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (state.userProfileImageUri != null) {
                                val context = LocalContext.current
                                val bitmap = remember(state.userProfileImageUri) {
                                    try {
                                        state.userProfileImageUri?.let { uriString ->
                                            context.contentResolver.openInputStream(Uri.parse(uriString))?.use {
                                                BitmapFactory.decodeStream(it)
                                            }
                                        }
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Profile",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = state.userAvatarRes),
                                        contentDescription = "Profile",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            } else {
                                Image(
                                    painter = painterResource(id = state.userAvatarRes),
                                    contentDescription = "Profile",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        Box(modifier = Modifier.size(12.dp).background(Color(0xFF2EC4B6), CircleShape).border(2.dp, Color.Black, CircleShape))
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = if (state.currentMood != null) "Current Vibe" else UIUtils.formatTitleCase(smartGreeting.split(",")[0]), 
                        color = Color.White.copy(alpha = 0.4f), 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Medium, 
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (state.currentMood != null) UIUtils.formatTitleCase(smartGreeting) else UIUtils.formatTitleCase(state.userName),
                        color = Color.White, 
                        fontSize = 24.sp, 
                        fontWeight = FontWeight.Black, 
                        letterSpacing = (-0.5).sp,
                        maxLines = if (isMessageExpanded) Int.MAX_VALUE else 1,
                        overflow = if (isMessageExpanded) androidx.compose.ui.text.style.TextOverflow.Visible else androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    
                    val formattedName = UIUtils.formatTitleCase(state.userName)
                    val rawMilestone = smartGreeting.split(",").getOrNull(1)?.trim() ?: ""
                    val milestoneText = if (state.currentMood != null) formattedName else UIUtils.formatTitleCase(rawMilestone)
                    if (milestoneText.length > 2) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (state.currentMood != null) "Active: $formattedName" else milestoneText,
                            color = moodTheme.first,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { onNotificationsClick(); isMessageExpanded = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        if (showRedDot) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-2).dp, y = 2.dp)
                                    .background(Color.Red, CircleShape)
                                    .border(1.5.dp, Color.Black, CircleShape)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { onSearchToggle(); isMessageExpanded = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSearchVisible) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Toggle Search",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isSearchVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { onSearchQueryChange(it) },
                        placeholder = { Text("Search your ecosystem...", color = Color.White.copy(alpha = 0.3f), fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = style.accentColor.copy(alpha = 0.4f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                            focusedContainerColor = Color.White.copy(alpha = 0.03f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                            cursorColor = style.accentColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        leadingIcon = { Icon(Icons.Default.Search, "Search", tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(18.dp)) },
                        trailingIcon = { 
                            IconButton(
                                onClick = { 
                                    if (searchQuery.isNotEmpty()) {
                                        onSearchRequested(searchQuery)
                                        keyboardController?.hide()
                                    }
                                },
                                modifier = Modifier.padding(end = 12.dp)
                            ) {
                                Icon(
                                    imageVector = if (searchQuery.isEmpty()) Icons.Default.Mic else Icons.AutoMirrored.Filled.Send, 
                                    "Action", 
                                    tint = style.accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            if (searchQuery.isNotEmpty()) {
                                onSearchRequested(searchQuery)
                                keyboardController?.hide()
                            }
                        }),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Current Focus", color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Text(state.dateString, color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))

            val moodDensityValue = remember(state.isSystemAppearanceEnabled, state.globalDisplaySize, state.homeDisplaySize, state.homeFocusSize) {
                UIUtils.getIsolatedMoodDensity(state)
            }
            val systemFontScale = LocalConfiguration.current.fontScale
            val moodDensity = remember(moodDensityValue, systemFontScale) {
                Density(density = moodDensityValue, fontScale = systemFontScale)
            }
            
            CompositionLocalProvider(LocalDensity provides moodDensity) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val moods = listOf("🔥", "⚡", "🧘", "💼", "😴", "🧠")
                    items(moods) { mood ->
                        val isSelected = state.currentMood == mood
                        val backgroundColor = if (isSelected) {
                            Color(UIUtils.darkenColor(style.accentColor.toArgb(), 0.5f))
                        } else {
                            style.surfaceColor
                        }

                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(backgroundColor)
                                .clickable { onMoodSelected(mood) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(mood, fontSize = 22.sp)
                        }
                    }
                }
            }
        }
    }
}
