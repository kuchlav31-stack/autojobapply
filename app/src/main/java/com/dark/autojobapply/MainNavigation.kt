package com.dark.autojobapply

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dark.jobai.ui.theme.BackgroundDark
import com.dark.jobai.ui.theme.BorderGray
import com.dark.jobai.ui.theme.ErrorRed
import com.dark.jobai.ui.theme.PrimaryGreen
import com.dark.jobai.ui.theme.TextGray
import com.dark.jobai.ui.theme.TextWhite
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// ====================================================================
// MAIN NAVIGATION SCREEN - 5 TABS
// ====================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigationScreen(
    onLogout: () -> Unit = {},
    onProfileNeeded: () -> Unit = {},
    onEmailTemplateNeeded: () -> Unit = {},
    onUpgradeClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val emailManager = remember { EmailApplicationManager(context) }
    val scope = rememberCoroutineScope()

    // Selected tab - 5 tabs
    var selectedTab by remember { mutableStateOf(0) }

    // Email sending state
    var isEmailSending by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundDark,
        bottomBar = {
            BottomNavigationBar5(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundDark)
        ) {
            when (selectedTab) {
                0 -> FeedScreen()  // NEW: Community Feed
                1 -> JobsScreen(
                    emailManager = emailManager,
                    isEmailSending = isEmailSending,
                    onEmailSendingChange = { isEmailSending = it },
                    onProfileNeeded = onProfileNeeded,
                    onEmailTemplateNeeded = onEmailTemplateNeeded
                )
                2 -> SavedJobsScreen()
                3 -> ApplicationsScreen()
                4 -> ProfileScreen(
                    onLogout = onLogout,
                    onUpgradeClick = onUpgradeClick
                )
            }
        }
    }
}

// ====================================================================
// BOTTOM NAVIGATION - 5 TABS
// ====================================================================

@Composable
fun BottomNavigationBar5(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        containerColor = Color(0xFF0D0D0D),
        contentColor = TextWhite,
        tonalElevation = 8.dp
    ) {
        // Tab 0: Feed
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = {
                Icon(
                    if (selectedTab == 0) Icons.Default.DynamicFeed else Icons.Default.DynamicFeed,
                    contentDescription = "Feed"
                )
            },
            label = { Text("Feed", fontSize = 9.sp) },
            colors = navColors(selectedTab == 0)
        )

        // Tab 1: Jobs
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = {
                Icon(
                    if (selectedTab == 1) Icons.Default.Work else Icons.Default.Work,
                    contentDescription = "Jobs"
                )
            },
            label = { Text("Jobs", fontSize = 9.sp) },
            colors = navColors(selectedTab == 1)
        )

        // Tab 2: Saved
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            icon = {
                Icon(
                    if (selectedTab == 2) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Saved"
                )
            },
            label = { Text("Saved", fontSize = 9.sp) },
            colors = navColors(selectedTab == 2)
        )

        // Tab 3: Applied
        NavigationBarItem(
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) },
            icon = {
                Icon(
                    Icons.Default.Description,
                    contentDescription = "Applied"
                )
            },
            label = { Text("Applied", fontSize = 9.sp) },
            colors = navColors(selectedTab == 3)
        )

        // Tab 4: Profile
        NavigationBarItem(
            selected = selectedTab == 4,
            onClick = { onTabSelected(4) },
            icon = {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile"
                )
            },
            label = { Text("Profile", fontSize = 9.sp) },
            colors = navColors(selectedTab == 4)
        )
    }
}

@Composable
private fun navColors(isSelected: Boolean) = NavigationBarItemDefaults.colors(
    selectedIconColor = PrimaryGreen,
    selectedTextColor = PrimaryGreen,
    unselectedIconColor = TextGray,
    unselectedTextColor = TextGray,
    indicatorColor = Color(0xFF1A1A1A)
)

// ====================================================================
// FEED SCREEN (Community Posts)
// ====================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen() {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()

    var posts by remember { mutableStateOf<List<FeedPost>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showCreatePost by remember { mutableStateOf(false) }

    // Fetch posts
    LaunchedEffect(Unit) {
        db.collection("feed")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    isLoading = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val postList = mutableListOf<FeedPost>()
                    for (doc in snapshot.documents) {
                        val post = FeedPost(
                            id = doc.id,
                            userId = doc.getString("userId") ?: "",
                            userName = doc.getString("userName") ?: "Anonymous",
                            userHeadline = doc.getString("userHeadline") ?: "",
                            content = doc.getString("content") ?: "",
                            imageUrl = doc.getString("imageUrl") ?: "",
                            likes = doc.getLong("likes") ?: 0L,
                            comments = doc.getLong("comments") ?: 0L,
                            createdAt = doc.getLong("createdAt") ?: 0L,
                            isLiked = false
                        )
                        postList.add(post)
                    }
                    posts = postList
                    isLoading = false
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Community Feed",
                    color = TextWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${posts.size} posts",
                    color = TextGray,
                    fontSize = 12.sp
                )
            }

            // Create Post Button
            IconButton(
                onClick = { showCreatePost = true },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(PrimaryGreen)
            ) {
                Icon(Icons.Default.Add, "Create Post", tint = Color.Black)
            }
        }

        // Posts List
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            }
            posts.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.DynamicFeed,
                            contentDescription = null,
                            tint = TextGray.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No Posts Yet", color = TextGray, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Be the first to share something!",
                            color = TextGray.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(posts) { post ->
                        FeedPostCard(post = post)
                    }
                }
            }
        }
    }

    // Create Post Bottom Sheet
    if (showCreatePost) {
        CreatePostSheet(
            onDismiss = { showCreatePost = false },
            onPostCreated = { showCreatePost = false }
        )
    }
}

// ====================================================================
// FEED POST CARD
// ====================================================================

@Composable
fun FeedPostCard(post: FeedPost) {
    var isLiked by remember { mutableStateOf(post.isLiked) }
    var likeCount by remember { mutableStateOf(post.likes) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        border = BorderStroke(1.dp, BorderGray.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // User Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(PrimaryGreen, Color(0xFF00D2A0))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        post.userName.take(1).uppercase(),
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        post.userName,
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (post.userHeadline.isNotEmpty()) {
                        Text(
                            post.userHeadline,
                            color = TextGray,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        formatTimeAgo(post.createdAt),
                        color = TextGray.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Content
            Text(
                post.content,
                color = TextWhite.copy(alpha = 0.9f),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            // Image
            if (post.imageUrl.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = "Post Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = BorderGray.copy(alpha = 0.3f))

            Spacer(modifier = Modifier.height(8.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Like
                Row(
                    modifier = Modifier.clickable {
                        isLiked = !isLiked
                        if (isLiked) likeCount++ else likeCount--
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) ErrorRed else TextGray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("$likeCount", color = TextGray, fontSize = 12.sp)
                }

                // Comment
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Comment,
                        contentDescription = "Comment",
                        tint = TextGray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${post.comments}", color = TextGray, fontSize = 12.sp)
                }

                // Share
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share",
                        tint = TextGray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share", color = TextGray, fontSize = 12.sp)
                }
            }
        }
    }
}

// ====================================================================
// CREATE POST SHEET
// ====================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostSheet(
    onDismiss: () -> Unit,
    onPostCreated: () -> Unit
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()

    var postContent by remember { mutableStateOf("") }
    var postImageUrl by remember { mutableStateOf("") }
    var isPosting by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            // Upload image to Firebase Storage
            scope.launch {
                val storage = com.google.firebase.storage.FirebaseStorage.getInstance()
                val fileRef = storage.reference.child("feedImages/${UUID.randomUUID()}.jpg")
                fileRef.putFile(uri).await()
                val downloadUrl = fileRef.downloadUrl.await()
                postImageUrl = downloadUrl.toString()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        contentColor = TextWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                "Create Post",
                color = TextWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = postContent,
                onValueChange = { postContent = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                placeholder = { Text("Share something with the community...") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = BorderGray,
                    cursorColor = PrimaryGreen,
                    focusedContainerColor = Color(0xFF0D0D0D),
                    unfocusedContainerColor = Color(0xFF0D0D0D)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Image upload
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0D0D0D))
                    .clickable { imagePicker.launch("image/*") }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (postImageUrl.isNotEmpty()) Icons.Default.CheckCircle else Icons.Default.Image,
                    contentDescription = null,
                    tint = if (postImageUrl.isNotEmpty()) PrimaryGreen else TextGray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (postImageUrl.isNotEmpty()) "Image Added ✓" else "Add Image (Optional)",
                    color = TextGray,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val userId = auth.currentUser?.uid ?: return@Button

                    if (postContent.isBlank()) {
                        Toast.makeText(context, "Please write something", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isPosting = true

                    db.collection("users").document(userId).get()
                        .addOnSuccessListener { userDoc ->
                            val userName = userDoc.getString("fullName") ?: "Anonymous"
                            val userHeadline = userDoc.getString("headline") ?: ""

                            val postData = hashMapOf(
                                "userId" to userId,
                                "userName" to userName,
                                "userHeadline" to userHeadline,
                                "content" to postContent,
                                "imageUrl" to postImageUrl,
                                "likes" to 0L,
                                "comments" to 0L,
                                "createdAt" to System.currentTimeMillis()
                            )

                            db.collection("feed").add(postData)
                                .addOnSuccessListener {
                                    isPosting = false
                                    Toast.makeText(context, "✅ Post created!", Toast.LENGTH_SHORT).show()
                                    onPostCreated()
                                }
                                .addOnFailureListener { e ->
                                    isPosting = false
                                    Toast.makeText(context, "❌ Failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isPosting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Post", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ====================================================================
// FEED POST DATA CLASS
// ====================================================================

data class FeedPost(
    val id: String = "",
    val userId: String = "",
    val userName: String = "Anonymous",
    val userHeadline: String = "",
    val content: String = "",
    val imageUrl: String = "",
    val likes: Long = 0L,
    val comments: Long = 0L,
    val createdAt: Long = 0L,
    val isLiked: Boolean = false
)

// ====================================================================
// JOBS SCREEN (Placeholder - तुम्हारा existing code)
// ====================================================================

@Composable
fun JobsScreen(
    emailManager: EmailApplicationManager,
    isEmailSending: Boolean,
    onEmailSendingChange: (Boolean) -> Unit,
    onProfileNeeded: () -> Unit,
    onEmailTemplateNeeded: () -> Unit
) {
    // तुम्हारा existing Jobs code यहाँ paste करो
    // HomeTab का content
    Text(
        "Jobs Screen",
        color = TextWhite,
        fontSize = 20.sp,
        modifier = Modifier.padding(16.dp)
    )
}

// ====================================================================
// SAVED JOBS SCREEN
// ====================================================================

@Composable
fun SavedJobsScreen() {
    Text(
        "Saved Jobs",
        color = TextWhite,
        fontSize = 20.sp,
        modifier = Modifier.padding(16.dp)
    )
}

// ====================================================================
// APPLICATIONS SCREEN
// ====================================================================

@Composable
fun ApplicationsScreen() {
    Text(
        "My Applications",
        color = TextWhite,
        fontSize = 20.sp,
        modifier = Modifier.padding(16.dp)
    )
}

// ====================================================================
// PROFILE SCREEN
// ====================================================================

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onUpgradeClick: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(PrimaryGreen, Color(0xFF00D2A0))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                (user?.displayName ?: "U").take(1).uppercase(),
                color = Color.Black,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(user?.displayName ?: "User", color = TextWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(user?.email ?: "", color = TextGray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onUpgradeClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFD700),
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Star, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Upgrade to Premium", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                auth.signOut()
                onLogout()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ErrorRed,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ====================================================================
// HELPER
// ====================================================================

private fun formatTimeAgo(timestamp: Long): String {
    if (timestamp == 0L) return "Just now"
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / (60 * 1000)
    val hours = diff / (60 * 60 * 1000)
    val days = diff / (24 * 60 * 60 * 1000)

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))
    }
}