package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.NeighborViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class AppTab { FEED, MAP, POST, MARKETPLACE, PROFILE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppShell(viewModel: NeighborViewModel) {
    var activeTab by remember { mutableStateOf(AppTab.FEED) }
    val currentUser by viewModel.currentUser.collectAsState()
    val currentNeighborhood by viewModel.currentNeighborhood.collectAsState()
    val activeAlerts by viewModel.activeAlerts.collectAsState()
    val activeChatUser by viewModel.activeChatUser.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Trigger toast notification on new alerts
    LaunchedEffect(activeAlerts) {
        if (activeAlerts.isNotEmpty()) {
            val critical = activeAlerts.firstOrNull { it.severity == "critical" }
            if (critical != null) {
                Toast.makeText(context, "🚨 CRITICAL ALERT: ${critical.title}", Toast.LENGTH_LONG).show()
            }
        }
    }

    if (currentUser == null) {
        AuthScreen(viewModel)
    } else {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = currentNeighborhood?.name ?: "Neighborly",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                            Text(
                                text = "${currentNeighborhood?.city ?: ""}, ${currentNeighborhood?.state ?: ""}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { activeTab = AppTab.PROFILE }) {
                            Icon(Icons.Default.Menu, contentDescription = "Sidebar")
                        }
                    },
                    actions = {
                        // Quick status badge
                        Box(modifier = Modifier.padding(end = 12.dp)) {
                            if (currentUser?.isVerified == true) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Verified,
                                        contentDescription = "Verified",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Verified",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.Red.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .clickable { activeTab = AppTab.PROFILE },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = "Unverified",
                                        tint = Color.Red,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Verify Now",
                                        fontSize = 10.sp,
                                        color = Color.Red,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    val navItemColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LeafGreenDark,
                        selectedTextColor = LeafGreenDark,
                        indicatorColor = GoldWarmAccent,
                        unselectedIconColor = LightSageAccent,
                        unselectedTextColor = LightSageAccent
                    )
                    NavigationBarItem(
                        selected = activeTab == AppTab.FEED,
                        onClick = { activeTab = AppTab.FEED; viewModel.endChat() },
                        icon = { Icon(if (activeTab == AppTab.FEED) Icons.Filled.Home else Icons.Outlined.Home, contentDescription = "Feed") },
                        label = { Text("Feed") },
                        colors = navItemColors,
                        modifier = Modifier.testTag("nav_feed")
                    )
                    NavigationBarItem(
                        selected = activeTab == AppTab.MAP,
                        onClick = { activeTab = AppTab.MAP; viewModel.endChat() },
                        icon = { Icon(if (activeTab == AppTab.MAP) Icons.Filled.Map else Icons.Outlined.Map, contentDescription = "Map") },
                        label = { Text("Neighbours") },
                        colors = navItemColors,
                        modifier = Modifier.testTag("nav_map")
                    )
                    NavigationBarItem(
                        selected = activeTab == AppTab.POST,
                        onClick = { activeTab = AppTab.POST; viewModel.endChat() },
                        icon = {
                            Icon(
                                if (activeTab == AppTab.POST) Icons.Filled.AddCircle else Icons.Outlined.AddCircle,
                                contentDescription = "Post",
                                modifier = Modifier.size(30.dp)
                            )
                        },
                        label = { Text("Post") },
                        colors = navItemColors,
                        modifier = Modifier.testTag("nav_post")
                    )
                    NavigationBarItem(
                        selected = activeTab == AppTab.MARKETPLACE,
                        onClick = { activeTab = AppTab.MARKETPLACE; viewModel.endChat() },
                        icon = { Icon(if (activeTab == AppTab.MARKETPLACE) Icons.Filled.Storefront else Icons.Outlined.Storefront, contentDescription = "Market") },
                        label = { Text("Market") },
                        colors = navItemColors,
                        modifier = Modifier.testTag("nav_market")
                    )
                    NavigationBarItem(
                        selected = activeTab == AppTab.PROFILE,
                        onClick = { activeTab = AppTab.PROFILE },
                        icon = { Icon(if (activeTab == AppTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        colors = navItemColors,
                        modifier = Modifier.testTag("nav_profile")
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // If Direct Message chat is active, display it immediately over everything as a sub-route
                if (activeChatUser != null) {
                    ChatScreenView(viewModel = viewModel)
                } else {
                    when (activeTab) {
                        AppTab.FEED -> FeedScreenContent(viewModel = viewModel, onStartChat = { activeTab = AppTab.PROFILE })
                        AppTab.MAP -> NeighborhoodMapScreen(viewModel = viewModel)
                        AppTab.POST -> PostCompositeScreen(viewModel = viewModel) { activeTab = AppTab.FEED }
                        AppTab.MARKETPLACE -> MarketplaceScreenContent(viewModel = viewModel)
                        AppTab.PROFILE -> ProfileScreenContent(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// PHASE 1 — AUTH & SIGNUP & VERIFICATION
// ----------------------------------------------------
@Composable
fun AuthScreen(viewModel: NeighborViewModel) {
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var zipCode by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("resident") } // resident, business_owner, moderator

    val authError by viewModel.authStateError.collectAsState()
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(LeafGreenPrimary.copy(alpha = 0.05f), CreamBackground)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Leaf App Icon Simulation
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Diversity3,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isSignUp) "Create My Hood Account" else "Welcome to Neighborly",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (isSignUp) "Scoped immediately to your zip area" else "Your hyper-local neighborhood social board",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))

                if (authError.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = authError,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(12.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (isSignUp) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("signup_name"),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Person, null) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_email"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    leadingIcon = { Icon(Icons.Default.Email, null) }
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_password"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    leadingIcon = { Icon(Icons.Default.Lock, null) }
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (isSignUp) {
                    Text(
                        "Your Home Location Info",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(top = 8.dp, bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = street,
                        onValueChange = { street = it },
                        label = { Text("Street Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Home, null) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City") },
                            modifier = Modifier.weight(1.5f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        OutlinedTextField(
                            value = state,
                            onValueChange = { state = it },
                            label = { Text("State") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Address autocomplete / quick seed buttons representing realistic neighborhoods in build guidelines
                    OutlinedTextField(
                        value = zipCode,
                        onValueChange = { zipCode = it },
                        label = { Text("Zip / Area Code") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("signup_zip"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Icon(Icons.Default.PinDrop, null) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Role selector
                    Text(
                        "Join Hood As",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(vertical = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("resident" to "Resident", "business_owner" to "Biz Owner", "moderator" to "Mod").forEach { (roleCode, roleName) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { role = roleCode }
                                    .border(
                                        width = 1.dp,
                                        color = if (role == roleCode) MaterialTheme.colorScheme.primary else Color.LightGray,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                RadioButton(
                                    selected = role == roleCode,
                                    onClick = { role = roleCode },
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    roleName,
                                    fontSize = 11.sp,
                                    fontWeight = if (role == roleCode) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Button(
                    onClick = {
                        scope.launch {
                            if (isSignUp) {
                                viewModel.signUp(
                                    name = name,
                                    email = email,
                                    passwordHash = password,
                                    street = street,
                                    city = city,
                                    state = state,
                                    zipCode = zipCode,
                                    role = role
                                )
                            } else {
                                viewModel.login(email, password)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("auth_submit_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(if (isSignUp) "Sign Up" else "Log In Securely", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = { isSignUp = !isSignUp },
                    modifier = Modifier.testTag("toggle_auth_mode")
                ) {
                    Text(
                        if (isSignUp) "Already have an account? Log In" else "Don't have an account? Sign Up now",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Quick setup helper buttons for easy developer evaluation
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Try quick demo login with:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    listOf("john@example.com" to "Resident", "sarah@example.com" to "Biz Owner", "mike@example.com" to "Moderator").forEach { (demoEmail, desc) ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                .clickable {
                                    email = demoEmail
                                    password = "password"
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(desc, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// PHASE 2 & 3 — COMMUNITY FEED + ALERTS
// ----------------------------------------------------
@Composable
fun FeedScreenContent(viewModel: NeighborViewModel, onStartChat: () -> Unit) {
    val posts by viewModel.posts.collectAsState()
    val activeAlerts by viewModel.activeAlerts.collectAsState()
    val currentFilter by viewModel.selectedCategoryFilter.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val scope = rememberCoroutineScope()
    var commentPostId by remember { mutableStateOf<Long?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Upper Critical Alerts Display Banner
        if (activeAlerts.isNotEmpty()) {
            val criticalAlert = activeAlerts.firstOrNull { it.severity == "critical" }
            if (criticalAlert != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Emergency,
                            contentDescription = "ALERT",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier
                                .size(36.dp)
                                .padding(end = 4.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = criticalAlert.title,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = criticalAlert.body,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Category Filter Horizontal bar
        val categories = listOf("All", "General", "Safety", "Lost_Pet", "Recommendation", "Event", "Free_Item", "For_Sale")
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = currentFilter == category
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setCategoryFilter(category) },
                    label = {
                        Text(
                            text = category.replace("_", " "),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("filter_chip_$category")
                )
            }
        }

        // Main Feed Items list
        if (posts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.Forum,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No discussions in your neighborhood yet.",
                        fontSize = 13.sp,
                        color = Color.LightGray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(posts, key = { it.id }) { post ->
                    NeighborhoodPostCard(
                        post = post,
                        viewModel = viewModel,
                        onCommentClick = { commentPostId = post.id },
                        onReportClick = { scope.launch { viewModel.reportPost(post.id) } }
                    )
                }
            }
        }
    }

    // Comment Threads Dialog Sheet (1 level deep comment thread popup)
    if (commentPostId != null) {
        CommentThreadSheet(
            postId = commentPostId!!,
            viewModel = viewModel,
            onDismiss = { commentPostId = null }
        )
    }
}

// Color coding cards based on prompt requirements ("safety = red, events = blue, free = green, alerts = yellow/orange")
@Composable
fun NeighborhoodPostCard(
    post: PostEntity,
    viewModel: NeighborViewModel,
    onCommentClick: () -> Unit,
    onReportClick: () -> Unit
) {
    val commentsFlow = remember(post.id) { viewModel.repository.getCommentsForPost(post.id) }
    val comments by commentsFlow.collectAsState(initial = emptyList())
    val reactionsFlow = remember(post.id) { viewModel.repository.getReactionsForPost(post.id) }
    val reactions by reactionsFlow.collectAsState(initial = emptyList())
    val currentUser by viewModel.currentUser.collectAsState()

    val scope = rememberCoroutineScope()
    val isUnverifiedUser = currentUser?.isVerified == false

    // Color code post categories per requirement
    val categoryColor = when (post.category.lowercase()) {
        "safety" -> CategorySafety
        "event" -> CategoryEvents
        "free_item" -> CategoryFree
        "recommendation" -> CategoryRecommendations
        "lost_pet" -> CategoryLostPet
        "alert" -> CategoryAlerts
        else -> CategoryGeneral
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("post_card_${post.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = if (post.isUrgent) BorderStroke(1.5.dp, CategorySafety) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Post header containing color code category
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(categoryColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = post.category.uppercase().replace("_", " "),
                        color = categoryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Date Time
                val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                Text(
                    text = sdf.format(Date(post.createdAt)),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )

                // Report button
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = onReportClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Flag,
                        contentDescription = "Report",
                        tint = Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Post content
            Text(
                text = post.title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = post.body,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            // Post Image with preview before posting simulation
            if (post.imageUrls.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                AsyncImage(
                    model = post.imageUrls,
                    contentDescription = "Post Item",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(8.dp))

            // Action section: Reactions, Comment counts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reactions segment (Helpful, Thanks, Agree)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val reactionsMap = reactions.groupBy { it.type }
                    listOf("helpful" to "👍", "thanks" to "🙏", "agree" to "✅").forEach { (type, emoji) ->
                        val count = reactionsMap[type]?.size ?: 0
                        val hasUserReacted = reactions.any { it.type == type && it.userId == currentUser?.id }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (hasUserReacted) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                                )
                                .clickable {
                                    if (isUnverifiedUser) return@clickable
                                    scope.launch { viewModel.toggleReaction(post.id, type) }
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(emoji, fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = count.toString(),
                                fontSize = 11.sp,
                                fontWeight = if (hasUserReacted) FontWeight.Bold else FontWeight.Normal,
                                color = if (hasUserReacted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Comment list button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        .clickable { onCommentClick() }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Comment,
                        contentDescription = "Comments",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = comments.size.toString(),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

// Comments Sheet Composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentThreadSheet(
    postId: Long,
    viewModel: NeighborViewModel,
    onDismiss: () -> Unit
) {
    val commentsFlow = remember(postId) { viewModel.repository.getCommentsForPost(postId) }
    val comments by commentsFlow.collectAsState(initial = emptyList())
    val currentUser by viewModel.currentUser.collectAsState()
    val isUnverifiedUser = currentUser?.isVerified == false

    var newCommentText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Discussion Thread",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // List of comments with author lookup
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (comments.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No comments yet. Start the conversation!", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    } else {
                        items(comments) { comment ->
                            // Local async lookup of usernames to support live updates in simulation
                            var commenterName by remember { mutableStateOf("Neighbor") }
                            var commenterAvatar by remember { mutableStateOf("") }

                            LaunchedEffect(comment.userId) {
                                val user = viewModel.repository.getUserById(comment.userId)
                                if (user != null) {
                                    commenterName = user.name
                                    commenterAvatar = user.avatarUrl
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                if (commenterAvatar.isNotEmpty()) {
                                    AsyncImage(
                                        model = commenterAvatar,
                                        contentDescription = "Avatar",
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(commenterName.take(1), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(commenterName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(comment.body, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f))
                                }
                            }
                        }
                    }
                }

                // Add comment entry (unverified block check)
                if (isUnverifiedUser) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.05f)),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Text(
                            text = "🔒 Address verification required to comment.",
                            color = Color.Red,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newCommentText,
                            onValueChange = { newCommentText = it },
                            placeholder = { Text("Write a response...", fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("comment_input_box"),
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 12.sp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (newCommentText.isNotBlank()) {
                                    scope.launch {
                                        viewModel.addComment(postId, newCommentText)
                                        newCommentText = ""
                                    }
                                }
                            },
                            modifier = Modifier.testTag("submit_comment_btn")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// DIRECT MESSAGING & BLOCKING UI
// ----------------------------------------------------
@Composable
fun ChatScreenView(viewModel: NeighborViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val activeChatUser by viewModel.activeChatUser.collectAsState()
    val messages by viewModel.directMessages.collectAsState()

    val scope = rememberCoroutineScope()
    var messageText by remember { mutableStateOf("") }

    // Read receipts tracker
    LaunchedEffect(messages) {
        viewModel.markActiveChatMessagesAsRead()
    }

    if (activeChatUser != null && currentUser != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Chat header with Block and Back Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.endChat() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }

                AsyncImage(
                    model = activeChatUser!!.avatarUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(activeChatUser!!.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        text = if (activeChatUser!!.isVerified) "Verified Resident" else "Unverified Resident",
                        fontSize = 11.sp,
                        color = if (activeChatUser!!.isVerified) MaterialTheme.colorScheme.primary else Color.Red
                    )
                }

                // Block/unblock target user per security mandates
                var isAlreadyBlocked by remember { mutableStateOf(false) }
                LaunchedEffect(activeChatUser) {
                    isAlreadyBlocked = viewModel.repository.isUserBlocked(currentUser!!.id, activeChatUser!!.id)
                }

                Button(
                    onClick = {
                        scope.launch {
                            viewModel.toggleBlockUser(activeChatUser!!.id)
                            isAlreadyBlocked = viewModel.repository.isUserBlocked(currentUser!!.id, activeChatUser!!.id)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAlreadyBlocked) Color.Gray else Color.Red
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text(if (isAlreadyBlocked) "Unblock" else "Block", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

            // Body Messages Bubbles Stream
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { msg ->
                    val isMyMsg = msg.senderId == currentUser!!.id
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (isMyMsg) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isMyMsg) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.secondaryContainer
                            ),
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isMyMsg) 16.dp else 2.dp,
                                bottomEnd = if (isMyMsg) 2.dp else 16.dp
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = msg.body,
                                    fontSize = 13.sp,
                                    color = if (isMyMsg) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                // Read indicator receipt "Read" or checkmark
                                Row(
                                    modifier = Modifier.align(Alignment.End),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (msg.readAt != null) "Read" else "Sent",
                                        fontSize = 9.sp,
                                        color = if (isMyMsg) Color.White.copy(alpha = 0.6f) else Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Message Composer area
            var activeBlockStatus by remember { mutableStateOf(false) }
            LaunchedEffect(activeChatUser) {
                activeBlockStatus = viewModel.repository.isUserBlocked(currentUser!!.id, activeChatUser!!.id)
            }

            if (activeBlockStatus) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Red.copy(alpha = 0.05f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Chat disabled while active block is on.", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp)
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Send private message...", fontSize = 12.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_compose_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                scope.launch {
                                    viewModel.sendDirectMessage(messageText)
                                    messageText = ""
                                }
                            }
                        },
                        modifier = Modifier.testTag("chat_send_btn")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// MAP SCREEN
// ----------------------------------------------------
@Composable
fun NeighborhoodMapScreen(viewModel: NeighborViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsersList.collectAsState()
    var selectedPinUser by remember { mutableStateOf<UserEntity?>(null) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    "Interactive Neighborhood Map",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Tap any house or business location pin on the neighborhood grid to contact them directly or view status.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        // Custom Canvas drawing beautiful stylized map instead of slow external web maps
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .background(CreamBackground)
        ) {
            val pins = listOf(
                Triple(0.2f, 0.3f, "Mod Mike (Mod)"),
                Triple(0.7f, 0.25f, "Sarah's Bakery (Biz)"),
                Triple(0.45f, 0.6f, "John's House (Res)"),
                Triple(0.8f, 0.75f, "Elena's Garden (Res)")
            )

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val gridW = size.width
                            val gridH = size.height

                            var foundUserEmail = ""
                            if (offset.x < gridW * 0.4f && offset.y < gridH * 0.45f) {
                                foundUserEmail = "mike@example.com"
                            } else if (offset.x > gridW * 0.55f && offset.y < gridH * 0.45f) {
                                foundUserEmail = "sarah@example.com"
                            } else if (offset.x < gridW * 0.6f && offset.y > gridH * 0.45f) {
                                foundUserEmail = "john@example.com"
                            } else {
                                foundUserEmail = "elena@example.com"
                            }

                            scope.launch {
                                selectedPinUser = viewModel.repository.getUserByEmail(foundUserEmail)
                            }
                        }
                    }
            ) {
                val canvasW = size.width
                val canvasH = size.height

                // Draw central park landscape
                drawRect(
                    color = Color(0xFFE8F5E9),
                    topLeft = Offset(canvasW * 0.15f, canvasH * 0.15f),
                    size = Size(canvasW * 0.7f, canvasH * 0.7f)
                )

                // Draw some streets
                val streetBrush = Brush.linearGradient(colors = listOf(Color(0xFFECEFF1), Color(0xFFCFD8DC)))
                // Main street horizontal
                drawRect(
                    brush = streetBrush,
                    topLeft = Offset(0f, canvasH * 0.45f),
                    size = Size(canvasW, canvasH * 0.1f)
                )
                // Street Vertical
                drawRect(
                    brush = streetBrush,
                    topLeft = Offset(canvasW * 0.5f, 0f),
                    size = Size(canvasW * 0.08f, canvasH)
                )

                // Draw pretty visual trees in park
                drawCircle(color = Color(0xFF81C784), radius = 30f, center = Offset(canvasW * 0.3f, canvasH * 0.25f))
                drawCircle(color = Color(0xFF4CAF50), radius = 24f, center = Offset(canvasW * 0.35f, canvasH * 0.3f))
                drawCircle(color = Color(0xFF81C784), radius = 26f, center = Offset(canvasW * 0.7f, canvasH * 0.65f))

                // Draw map pin locations
                pins.forEach { (px, py, label) ->
                    val x = canvasW * px
                    val y = canvasH * py

                    // Draw pin housing shadow base
                    drawCircle(color = Color.Black.copy(alpha = 0.2f), radius = 12f, center = Offset(x, y + 8f))

                    // Draw actual marker
                    val pinColor = if (label.contains("Biz")) LeafGreenPrimary else if (label.contains("Mod")) CategorySafety else GoldWarmAccent
                    drawCircle(color = pinColor, radius = 14f, center = Offset(x, y))
                    drawCircle(color = Color.White, radius = 6f, center = Offset(x, y))

                    // Label line and text
                    drawLine(color = pinColor, start = Offset(x, y), end = Offset(x, y - 18f), strokeWidth = 3f)
                }
            }

            // Legend indicators
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(CategorySafety))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Moderator", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(LeafGreenPrimary))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Business Owner", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(GoldWarmAccent))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Resident", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Instruction tooltip helper
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Tap on map regions to select neighbors!", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Slide up neighbor card details if user taps coordinates
        AnimatedVisibility(
            visible = selectedPinUser != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            val user = selectedPinUser
            if (user != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = user.avatarUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(user.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                if (user.isVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.Verified, "Verified", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                }
                            }
                            Text(user.street, fontSize = 11.sp, color = Color.Gray)
                            Text("Role: ${user.role.uppercase()}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        // Start private messaging seller/neighbor directly (Satisfies Phase 7 requirements)
                        if (currentUser?.isVerified == true && user.isVerified && user.id != currentUser?.id) {
                            Button(
                                onClick = { viewModel.startChat(user) },
                                modifier = Modifier.testTag("msg_from_map")
                            ) {
                                Text("Chat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// PHASE 4 — MARKETPLACE MODULE
// ----------------------------------------------------
@Composable
fun MarketplaceScreenContent(viewModel: NeighborViewModel) {
    val listings by viewModel.marketplaceListings.collectAsState()
    val isFreeOnly by viewModel.marketplaceFreeOnly.collectAsState()
    val conditionFilter by viewModel.marketplaceConditionFilter.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val scope = rememberCoroutineScope()
    var selectedListing by remember { mutableStateOf<MarketplaceListingEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "Neighborhood Marketplace 🛒",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Pick up goods directly from Maple Hills residents. Fully local, safe purchases.",
                    fontSize = 11.sp,
                )
            }
        }

        // Filters Panel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Free Only toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isFreeOnly,
                    onCheckedChange = { viewModel.setMarketplaceFreeOnly(it) },
                    modifier = Modifier.testTag("free_checkbox")
                )
                Text("Free only 🎁", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // Condition filtering Board
            listOf("All", "New", "Good").forEach { cond ->
                FilterChip(
                    selected = conditionFilter == cond,
                    onClick = { viewModel.setMarketplaceCondition(cond) },
                    label = { Text(cond, fontSize = 10.sp) }
                )
            }
        }

        // Double column visual grid items
        if (listings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No marketplace items match your filters.", color = Color.Gray, fontSize = 12.sp)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listings) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedListing = item }
                            .testTag("listing_card_${item.id}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column {
                            // Listing Image
                            if (item.imageUrls.isNotEmpty()) {
                                AsyncImage(
                                    model = item.imageUrls,
                                    contentDescription = item.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .background(Color.LightGray.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Storefront, contentDescription = null, tint = Color.LightGray)
                                }
                            }

                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = item.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (item.price == null || item.price == 0.0) "FREE 🎁" else "$${String.format("%.2f", item.price)}",
                                    color = if (item.price == null) CategoryFree else MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.condition,
                                        fontSize = 10.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Bold
                                    )
                                    // Status Badge available/sold/reserved
                                    val badgeColor = when (item.status.lowercase()) {
                                        "available" -> CategoryFree
                                        "reserved" -> CategoryAlerts
                                        else -> Color.Gray
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(badgeColor.copy(alpha = 0.15f))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            item.status.uppercase(),
                                            color = badgeColor,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail Modal Dialog for selected listing
    if (selectedListing != null) {
        val item = selectedListing!!
        var sellerUser by remember { mutableStateOf<UserEntity?>(null) }
        LaunchedEffect(item.userId) {
            sellerUser = viewModel.repository.getUserById(item.userId)
        }

        Dialog(onDismissRequest = { selectedListing = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            item.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { selectedListing = null }) {
                            Icon(Icons.Default.Close, null)
                        }
                    }

                    if (item.imageUrls.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        AsyncImage(
                            model = item.imageUrls,
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (item.price == null || item.price == 0.0) "FREE 🎁" else "$${String.format("%.2f", item.price)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.price == null) CategoryFree else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(item.description, fontSize = 13.sp)

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(10.dp))

                    if (sellerUser != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = sellerUser!!.avatarUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Sold by: ${sellerUser!!.name}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Rating: Verified Resident", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                            }

                            // Contact seller directly starts DMs
                            if (currentUser?.isVerified == true && sellerUser!!.isVerified && sellerUser!!.id != currentUser?.id) {
                                Button(
                                    onClick = {
                                        viewModel.startChat(sellerUser!!)
                                        selectedListing = null
                                    },
                                    modifier = Modifier.testTag("contact_seller_btn")
                                ) {
                                    Text("Message Seller", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            } else if (currentUser?.id == sellerUser!!.id) {
                                // Double actions to let the seller themselves change status sold or reserved (Rule Phase 4 requirement)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                viewModel.updateListingStatus(item.id, "sold")
                                                selectedListing = null
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                        contentPadding = PaddingValues(horizontal = 6.dp)
                                    ) {
                                        Text("Mark Sold", fontSize = 9.sp)
                                    }
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                viewModel.updateListingStatus(item.id, "reserved")
                                                selectedListing = null
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CategoryAlerts),
                                        contentPadding = PaddingValues(horizontal = 6.dp)
                                    ) {
                                        Text("Reserve", fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// PHASE 5 & 6 & 7 — PROFILE, DIRECT MESSAGE CONTACTS, GROUPS & BUSINESS PANELS
// ----------------------------------------------------
@Composable
fun ProfileScreenContent(viewModel: NeighborViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val currentNeighborhood by viewModel.currentNeighborhood.collectAsState()

    // Sub views controller in Profile Hub page to reduce complexity
    var profileTabState by remember { mutableStateOf("Main") } // Main, Verification, Switch, Groups, Messages, BusinessDir

    if (currentUser != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Screen Breadcrumb
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (profileTabState != "Main") {
                    IconButton(onClick = { profileTabState = "Main" }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
                Text(
                    text = when (profileTabState) {
                        "Verification" -> "Neighborhood Address Certification"
                        "Switch" -> "Testing Account Emulator"
                        "Groups" -> "Sub-Neighborhood Clubs"
                        "Messages" -> "Direct Messages Box"
                        "BusinessDir" -> "Local Business Directory"
                        else -> "My Neighborhood Hub"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f), modifier = Modifier.padding(bottom = 12.dp))

            when (profileTabState) {
                "Main" -> {
                    // Profile Info Header Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = currentUser!!.avatarUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(currentUser!!.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text(currentUser!!.email, fontSize = 12.sp, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = currentUser!!.role.uppercase(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("📍 Address: ${currentUser!!.street}, ${currentUser!!.city}, ${currentUser!!.zipCode}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Navigation Sub panels inside Profile Hub
                    val panels = listOf(
                        Triple("Verification", Icons.Default.GppGood, "Address Verification Hub"),
                        Triple("Messages", Icons.Default.Forum, "Direct Chat inbox"),
                        Triple("Groups", Icons.Default.Groups, "Explore local groups"),
                        Triple("BusinessDir", Icons.Default.CardMembership, "Local businesses directory"),
                        Triple("Switch", Icons.Default.SwitchAccount, "Account Switcher (Testing emulator)")
                    )

                    panels.forEach { (route, icon, label) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { profileTabState = route }
                                .testTag("hub_btn_$route"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray)
                            }
                        }
                    }

                    // Logout Button
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.logout() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("logout_btn")
                    ) {
                        Text("Log Out Account", fontWeight = FontWeight.Bold)
                    }
                }

                "Verification" -> VerificationSubScreen(viewModel)
                "Switch" -> SwitchAccountEmulator(viewModel) { profileTabState = "Main" }
                "Groups" -> GroupsSubScreen(viewModel)
                "Messages" -> DirectMessagesListBox(viewModel)
                "BusinessDir" -> BusinessDirectorySubScreen(viewModel)
            }
        }
    }
}

// Sub components details

@Composable
fun VerificationSubScreen(viewModel: NeighborViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val mockCode by viewModel.verificationCode.collectAsState()
    val error by viewModel.verificationError.collectAsState()

    var inputCode by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    if (currentUser?.isVerified == true) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Verified, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text("Your Address is fully Certified!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    "You have complete access to make discussions, reply/comment, rate local businesses, and direct message neighbors.",
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxWidth()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("🔒 Safety Verification Pending", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Red)
                    Text(
                        "We send physical letters with a 4-digit security PIN to confirm your home address is real. Unverified users can browse the community feed and marketplace grid, but cannot post or chat.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mock letter simulator to satisfy prompt: "Send a verification code to confirm their address"
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📬 Simulated Letter from HoodLink", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Text("Delivered to: ${currentUser!!.street}", fontSize = 10.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Your verification PIN is: $mockCode",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (error.isNotEmpty()) {
                Text(error, color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
            }

            OutlinedTextField(
                value = inputCode,
                onValueChange = { inputCode = it },
                label = { Text("Enter 4-Digit PIN Code") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("verification_code_input"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    scope.launch {
                        viewModel.verifyAddress(inputCode)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("verify_address_submit"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Unlock Fully Verified Acccess")
            }
        }
    }
}

// Allows developers to quick switch active roles John (Resident), Sarah (Business), Mike (Moderator)
@Composable
fun SwitchAccountEmulator(viewModel: NeighborViewModel, onBack: () -> Unit) {
    val usersList = listOf(
        "mike@example.com" to "Mod Mike (Moderator)",
        "sarah@example.com" to "Sarah Baker (Cupcake Business Owner)",
        "john@example.com" to "John Resident (Verified Resident)",
        "elena@example.com" to "Elena Green (Verified Resident)",
        "ka0967711@gmail.com" to "Developer Neighbor"
    )
    val currentUsr by viewModel.currentUser.collectAsState()
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Role Switcher - Verification Assistant", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Toggle between accounts to test moderator alerts publishing, review postings, marketplace direct chats, or blocking restrictions.", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        usersList.forEach { (email, desc) ->
            val isActive = currentUsr?.email == email
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        scope.launch {
                            viewModel.login(email, "password")
                            onBack()
                        }
                    }
                    .testTag("switch_account_$email"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    else MaterialTheme.colorScheme.surface
                ),
                border = if (isActive) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(desc, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    if (isActive) {
                        Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun DirectMessagesListBox(viewModel: NeighborViewModel) {
    val contacts by viewModel.chatContacts.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    if (contacts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No active direct messages. Go to Map or Feed to select neighbors!", color = Color.Gray, fontSize = 12.sp)
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            contacts.forEach { contact ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.startChat(contact) }
                        .testTag("dm_row_${contact.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = contact.avatarUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Click to view thread...", fontSize = 11.sp, color = Color.Gray)
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.LightGray)
                    }
                }
            }
        }
    }
}

// Groups section - Join and participate in scoped clubs (Phase 6)
@Composable
fun GroupsSubScreen(viewModel: NeighborViewModel) {
    val groups by viewModel.groups.collectAsState()
    val selectedGroup by viewModel.selectedGroup.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val scope = rememberCoroutineScope()
    var showCreateGroupDialog by remember { mutableStateOf(false) }

    if (selectedGroup == null) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Clubs & Sub-Feeds", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { showCreateGroupDialog = true },
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text("New Group", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            groups.forEach { group ->
                // Check membership
                var isMember by remember { mutableStateOf(false) }
                LaunchedEffect(group.id, currentUser) {
                    if (currentUser != null) {
                        isMember = viewModel.repository.isGroupMember(group.id, currentUser!!.id)
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { viewModel.selectGroup(group) }
                        .testTag("group_row_${group.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Groups, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(group.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isMember) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        else Color.LightGray.copy(alpha = 0.2f)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(if (isMember) "Member" else "Join", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(group.description, fontSize = 11.sp, color = Color.Gray, maxLines = 1)
                    }
                }
            }

            if (groups.isEmpty()) {
                Text("No interest clubs created yet.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(16.dp))
            }
        }
    } else {
        // Group Detail View & private discussion
        val group = selectedGroup!!
        val groupFeedPosts by viewModel.groupPosts.collectAsState()
        val membersList by viewModel.groupMembers.collectAsState()
        var isMember by remember { mutableStateOf(false) }

        var showPostComposerInGroup by remember { mutableStateOf(false) }

        LaunchedEffect(group.id, currentUser) {
            if (currentUser != null) {
                isMember = viewModel.repository.isGroupMember(group.id, currentUser!!.id)
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.selectGroup(null) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(group.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Total Members: ${membersList.size}", fontSize = 10.sp, color = Color.Gray)
                }

                Button(
                    onClick = {
                        scope.launch {
                            if (isMember) {
                                viewModel.leaveGroup(group.id)
                            } else {
                                viewModel.joinGroup(group.id)
                            }
                            isMember = !isMember
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMember) Color.Gray else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(if (isMember) "Leave Group" else "Join Group", fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(group.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(10.dp))

            // Write post button in group
            if (isMember) {
                Button(
                    onClick = { showPostComposerInGroup = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Launch Group Thread", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.1f))
                ) {
                    Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                        Text("🔒 Discussion limited to group members only.", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Staggered list of discussion inside group
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(groupFeedPosts) { post ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row {
                                Text(post.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
                                Text(sdf.format(Date(post.createdAt)), fontSize = 9.sp, color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(post.body, fontSize = 11.sp)
                        }
                    }
                }

                if (groupFeedPosts.isEmpty()) {
                    item {
                        Text("No active discussions in this club yet.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(12.dp))
                    }
                }
            }
        }

        // Mini inside Group Post Composer Modal
        if (showPostComposerInGroup) {
            var gTitle by remember { mutableStateOf("") }
            var gBody by remember { mutableStateOf("") }

            Dialog(onDismissRequest = { showPostComposerInGroup = false }) {
                Card(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("New Group Thread", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = gTitle,
                            onValueChange = { gTitle = it },
                            label = { Text("Topic Title") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = gBody,
                            onValueChange = { gBody = it },
                            label = { Text("Message Body") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { showPostComposerInGroup = false }) { Text("Cancel") }
                            Button(onClick = {
                                if (gTitle.isNotBlank() && gBody.isNotBlank()) {
                                    scope.launch {
                                        viewModel.createGroupPost(group.id, gTitle, gBody)
                                        showPostComposerInGroup = false
                                    }
                                }
                            }) { Text("Post Thread") }
                        }
                    }
                }
            }
        }
    }

    // New Group Creation Overlay dialog
    if (showCreateGroupDialog) {
        var gName by remember { mutableStateOf("") }
        var gDesc by remember { mutableStateOf("") }
        var isPrivate by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showCreateGroupDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Launch Neighborhood Club", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = gName,
                        onValueChange = { gName = it },
                        label = { Text("Club/Group Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = gDesc,
                        onValueChange = { gDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isPrivate, onCheckedChange = { isPrivate = it })
                        Text("Make group private", fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showCreateGroupDialog = false }) { Text("Cancel") }
                        Button(onClick = {
                            if (gName.isNotBlank() && gDesc.isNotBlank()) {
                                scope.launch {
                                    viewModel.createGroup(gName, gDesc, isPrivate)
                                    showCreateGroupDialog = false
                                }
                            }
                        }) { Text("Create & Join") }
                    }
                }
            }
        }
    }
}

// Business directories & ratings section (Phase 5)
@Composable
fun BusinessDirectorySubScreen(viewModel: NeighborViewModel) {
    val directory by viewModel.businessDirectory.collectAsState()
    val selectedBusiness by viewModel.selectedBusiness.collectAsState()
    val reviews by viewModel.businessReviews.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val scope = rememberCoroutineScope()
    var showReviewDialog by remember { mutableStateOf(false) }
    var showRegisterBizDialog by remember { mutableStateOf(false) }

    if (selectedBusiness == null) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Maple Hills Directory", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.weight(1f))
                if (currentUser?.role == "business_owner") {
                    Button(
                        onClick = { showRegisterBizDialog = true },
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Claim Business", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            directory.forEach { biz ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { viewModel.selectBusiness(biz) }
                        .testTag("biz_row_${biz.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row {
                            Text(biz.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.weight(1f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, "Rating", tint = GoldWarmAccent, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = if (biz.averageRating == 0.0) "N/A" else String.format("%.1f", biz.averageRating),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(biz.category.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(biz.description, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            if (directory.isEmpty()) {
                Text("No business profile is registered here.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(14.dp))
            }
        }
    } else {
        // Business detail profile card & reviews rating dashboard
        val biz = selectedBusiness!!
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.selectBusiness(null) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
                Column {
                    Text(biz.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    Text(biz.category, fontSize = 11.sp, color = Color.Gray)
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("ℹ️ Business Details", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Owner Address: ${biz.address}", fontSize = 11.sp)
                    Text("Hours: ${biz.hours}", fontSize = 11.sp)
                    Text("Phone: ${biz.phone}", fontSize = 11.sp)
                    Text("Web: ${biz.website}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Neighbor Reviews (${reviews.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.weight(1f))
                if (currentUser?.isVerified == true) {
                    Button(
                        onClick = { showReviewDialog = true },
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Add Review", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Reviews history
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(reviews) { r ->
                    var reviewerName by remember { mutableStateOf("Resident") }
                    LaunchedEffect(r.userId) {
                        val u = viewModel.repository.getUserById(r.userId)
                        if (u != null) reviewerName = u.name
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row {
                                Text(reviewerName, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                Row {
                                    repeat(r.rating) {
                                        Icon(Icons.Default.Star, null, tint = GoldWarmAccent, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(r.body, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f))
                        }
                    }
                }

                if (reviews.isEmpty()) {
                    item {
                        Text("No reviews written for this bakery/business yet.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(12.dp))
                    }
                }
            }
        }

        // Add Review star sheet
        if (showReviewDialog) {
            var ratingValue by remember { mutableIntStateOf(5) }
            var feedbackInput by remember { mutableStateOf("") }

            Dialog(onDismissRequest = { showReviewDialog = false }) {
                Card(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Review: ${biz.name}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            (1..5).forEach { star ->
                                IconButton(onClick = { ratingValue = star }) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (star <= ratingValue) GoldWarmAccent else Color.LightGray,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = feedbackInput,
                            onValueChange = { feedbackInput = it },
                            placeholder = { Text("Write your feedback...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { showReviewDialog = false }) { Text("Cancel") }
                            Button(onClick = {
                                if (feedbackInput.isNotBlank()) {
                                    scope.launch {
                                        viewModel.addReview(biz.id, ratingValue, feedbackInput)
                                        showReviewDialog = false
                                    }
                                }
                            }) { Text("Post Review") }
                        }
                    }
                }
            }
        }
    }

    // Business claim dialog sheet
    if (showRegisterBizDialog) {
        var bName by remember { mutableStateOf("") }
        var bCat by remember { mutableStateOf("Food & Drink") }
        var bDesc by remember { mutableStateOf("") }
        var bHours by remember { mutableStateOf("Mon-Sat: 9AM - 5PM") }
        var bPhone by remember { mutableStateOf("") }
        var bWeb by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showRegisterBizDialog = false }) {
            Card(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Claim Business Directory Profile", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = bName,
                        onValueChange = { bName = it },
                        label = { Text("Business Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = bCat,
                        onValueChange = { bCat = it },
                        label = { Text("Category (e.g., Food, Retail, Services)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = bDesc,
                        onValueChange = { bDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = bPhone,
                        onValueChange = { bPhone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = bWeb,
                        onValueChange = { bWeb = it },
                        label = { Text("Website") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showRegisterBizDialog = false }) { Text("Cancel") }
                        Button(onClick = {
                            if (bName.isNotBlank() && bDesc.isNotBlank()) {
                                scope.launch {
                                    viewModel.registerBusinessPage(
                                        name = bName, cat = bCat, desc = bDesc,
                                        address = currentUser!!.street, phone = bPhone, web = bWeb, hours = bHours
                                    )
                                    showRegisterBizDialog = false
                                }
                            }
                        }) { Text("Claim Profile") }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// CENTER POST FAB COMPOSER SCREEN/OVERLAY (PHASE 2 & 4 & 5 Core Composer)
// ----------------------------------------------------
@Composable
fun PostCompositeScreen(viewModel: NeighborViewModel, onComplete: () -> Unit) {
    val currentUser by viewModel.currentUser.collectAsState()
    val scope = rememberCoroutineScope()

    var postTypeSelection by remember { mutableStateOf("general") } // general, marketplace, alert

    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var imageLink by remember { mutableStateOf("") }
    var categorySelection by remember { mutableStateOf("general") }
    var isUrgent by remember { mutableStateOf(false) }

    // Marketplace extra fields
    var priceInput by remember { mutableStateOf("") }
    var conditionSelection by remember { mutableStateOf("Good") } // New, Like New, Good, Fair

    // Alerts extra fields
    var alertSeverity by remember { mutableStateOf("medium") } // low, medium, high, critical
    var alertType by remember { mutableStateOf("emergency") } // weather, crime, outage, emergency

    val isUnverifiedUser = currentUser?.isVerified == false

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Share with Neighborhood Board",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text("Your message will be scoped strictly to the residents of ${currentUser?.neighborhoodId ?: ""}.", fontSize = 11.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(14.dp))

        if (isUnverifiedUser) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Lock, "Locked", tint = Color.Red)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("🔒 Verification Required", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Red)
                    Text("Go to Profile tab and enter the delivered mailing code to activate full posting permissions.", fontSize = 11.sp, textAlign = TextAlign.Center)
                }
            }
        } else {
            // Post type selector tabs
            val toggles = mutableListOf("general" to "Discussion", "marketplace" to "Market Sale")
            if (currentUser?.role == "moderator") {
                toggles.add("alert" to "Emergency Alert")
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                toggles.forEach { (typeCode, label) ->
                    val isActive = postTypeSelection == typeCode
                    Button(
                        onClick = { postTypeSelection = typeCode },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isActive) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            contentColor = if (isActive) Color.White else MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (postTypeSelection) {
                "general" -> {
                    // Category Badge Selector Scroll box
                    Text("Select Tag Label", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    val categories = listOf("general", "safety", "lost_pet", "recommendation", "event", "free_item")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = categorySelection == cat,
                                onClick = { categorySelection = cat },
                                label = { Text(cat.uppercase().replace("_", " "), fontSize = 9.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Topic Headline Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("post_headline_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        label = { Text("What's on your mind?") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("post_body_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = imageLink,
                        onValueChange = { imageLink = it },
                        label = { Text("Photo Link URL (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Image Live Review Box
                    if (imageLink.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Live Image Preview before sending:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        AsyncImage(
                            model = imageLink,
                            contentDescription = "Preview",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isUrgent, onCheckedChange = { isUrgent = it })
                        Text("Mark Urgent Alert (Show banner at top)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (title.isNotBlank() && body.isNotBlank()) {
                                scope.launch {
                                    viewModel.createPost(title, body, categorySelection, imageLink, isUrgent)
                                    onComplete()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_general_post"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Publish Post", fontWeight = FontWeight.Bold)
                    }
                }

                "marketplace" -> {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Item For Sale Headline") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        label = { Text("Description (Condition, size, details...)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = priceInput,
                            onValueChange = { priceInput = it },
                            label = { Text("Price ($ or empty for Free)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = conditionSelection,
                            onValueChange = { conditionSelection = it },
                            label = { Text("Condition (New/Good/Fair)") },
                            modifier = Modifier.weight(1.2f),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = imageLink,
                        onValueChange = { imageLink = it },
                        label = { Text("Product Photo URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (title.isNotBlank() && body.isNotBlank()) {
                                scope.launch {
                                    viewModel.createListing(
                                        title = title,
                                        desc = body,
                                        price = priceInput.toDoubleOrNull(),
                                        condition = conditionSelection,
                                        imageUrl = imageLink
                                    )
                                    onComplete()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("List Material Item", fontWeight = FontWeight.Bold)
                    }
                }

                "alert" -> {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Emergency Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        label = { Text("Emergency Instructions Detail") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = alertSeverity,
                            onValueChange = { alertSeverity = it },
                            label = { Text("Severity (critical/high/medium)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = alertType,
                            onValueChange = { alertType = it },
                            label = { Text("Type (weather/crime/outage)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (title.isNotBlank() && body.isNotBlank()) {
                                scope.launch {
                                    viewModel.createAlert(
                                        title = title,
                                        body = body,
                                        type = alertType,
                                        severity = alertSeverity
                                    )
                                    onComplete()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Publish Official Safety Alert ⚠️", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
