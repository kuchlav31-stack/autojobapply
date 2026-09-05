package com.dark.jobai.ui.screens.main.feed

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dark.jobai.data.model.FeedPost
import com.dark.jobai.ui.components.EmptyState
import com.dark.jobai.util.Formatters
import com.dark.jobai.viewmodel.FeedViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen() {
    val context = LocalContext.current
    val feedViewModel: FeedViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    val posts by feedViewModel.posts.collectAsState()
    val isLoading by feedViewModel.isLoading.collectAsState()

    var showCreatePost by remember { mutableStateOf(false) }

    // --- Professional Light Theme Palette ---
    val AppBlue = Color(0xFF0F52FF)
    val BgLight = Color(0xFFF8FAFC)
    val SurfaceWhite = Color(0xFFFFFFFF)
    val TextDark = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)
    val BorderSubtle = Color(0xFFE2E8F0)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BgLight,
        topBar = {
            Column(
                modifier = Modifier
                    .background(BgLight)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Community Feed",
                            color = TextDark,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Insights & Career Trends",
                            color = AppBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(2.dp, RoundedCornerShape(10.dp), ambientColor = Color.Black.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(10.dp),
                        color = SurfaceWhite,
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        IconButton(
                            onClick = { feedViewModel.loadPosts() },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(Icons.Default.Refresh, "Refresh", tint = TextDark, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreatePost = true },
                containerColor = AppBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Post", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Post", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppBlue)
                    }
                }
                posts.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyState(
                            icon = Icons.Default.ChatBubbleOutline,
                            title = "Community is quiet",
                            description = "Be the first to share a job insight or ask a career question!"
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(BgLight),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(posts, key = { it.id }) { post ->
                            FeedPostCard(
                                post = post,
                                onLike = { feedViewModel.likePost(post) },
                                onDelete = {
                                    feedViewModel.deletePost(post.id)
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }

    if (showCreatePost) {
        CreatePostSheet(
            onDismiss = { showCreatePost = false },
            onPostCreated = {
                showCreatePost = false
            }
        )
    }
}

@Composable
fun FeedPostCard(
    post: FeedPost,
    onLike: () -> Unit,
    onDelete: () -> Unit = {}
) {
    var isLiked by remember(post.id) { mutableStateOf(post.likedBy.contains(FirebaseAuth.getInstance().currentUser?.uid)) }
    var likeCount by remember(post.id) { mutableLongStateOf(post.likes) }
    var showMenu by remember { mutableStateOf(false) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    val AppBlue = Color(0xFF0F52FF)
    val SurfaceWhite = Color(0xFFFFFFFF)
    val TextDark = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)
    val BorderSubtle = Color(0xFFE2E8F0)
    val ErrorRed = Color(0xFFEF4444)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(alpha = 0.04f),
                spotColor = Color.Black.copy(alpha = 0.04f)
            ),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Avatar
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(AppBlue.copy(alpha = 0.08f))
                        .border(1.dp, AppBlue.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (post.userProfileImage.isNotEmpty()) {
                        AsyncImage(
                            model = post.userProfileImage,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Text(
                            post.userName.take(1).uppercase(),
                            color = AppBlue,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.userName,
                        color = TextDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (post.userHeadline.isNotEmpty()) {
                        Text(
                            text = post.userHeadline,
                            color = AppBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = Formatters.formatTimeAgo(post.createdAt),
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }

                if (post.userId == currentUserId) {
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.MoreVert, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(SurfaceWhite)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Delete Post", color = ErrorRed, fontWeight = FontWeight.SemiBold) },
                                onClick = { onDelete(); showMenu = false },
                                leadingIcon = { Icon(Icons.Default.Delete, null, tint = ErrorRed) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Text Content
            Text(
                text = post.content,
                color = TextDark.copy(alpha = 0.9f),
                fontSize = 14.sp,
                lineHeight = 22.sp
            )

            // Optional Image
            if (post.imageUrl.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF1F5F9))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = BorderSubtle)
            Spacer(modifier = Modifier.height(8.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            isLiked = !isLiked
                            if (isLiked) likeCount++ else likeCount--
                            onLike()
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isLiked) ErrorRed else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (likeCount > 0) likeCount.toString() else "Like",
                        color = if (isLiked) ErrorRed else TextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Comment Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { /* Comment Action */ }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ChatBubbleOutline, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                    if (post.comments > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(post.comments.toString(), color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Share Button
                IconButton(
                    onClick = { /* Share Action */ },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Share, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}


// ====================================================================
// CREATE POST SHEET (Upgraded Professional Light UI)
// ====================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostSheet(
    onDismiss: () -> Unit,
    onPostCreated: () -> Unit
) {
    val context = LocalContext.current
    val feedViewModel: FeedViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    var postContent by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val isCreatingPost by feedViewModel.isCreatingPost.collectAsState()

    val AppBlue = Color(0xFF0F52FF)
    val SurfaceWhite = Color(0xFFFFFFFF)
    val TextDark = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)
    val BorderSubtle = Color(0xFFE2E8F0)

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        contentColor = TextDark,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                "Create Post",
                color = TextDark,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Post Content Field
            OutlinedTextField(
                value = postContent,
                onValueChange = { postContent = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                placeholder = { Text("Share an insight or ask a career question...", color = TextMuted, fontSize = 14.sp) },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextDark,
                    unfocusedTextColor = TextDark,
                    focusedBorderColor = AppBlue,
                    unfocusedBorderColor = BorderSubtle,
                    cursorColor = AppBlue,
                    focusedContainerColor = Color(0xFFF8FAFC),
                    unfocusedContainerColor = Color(0xFFF8FAFC)
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Image Upload Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .clickable { imagePicker.launch("image/*") }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (imageUri != null) Icons.Default.CheckCircle else Icons.Default.Image,
                    contentDescription = null,
                    tint = if (imageUri != null) Color(0xFF10B981) else TextMuted
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    if (imageUri != null) "Image Selected ✓" else "Attach an image (Optional)",
                    color = if (imageUri != null) Color(0xFF10B981) else TextMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Show selected image preview
            if (imageUri != null) {
                Spacer(modifier = Modifier.height(10.dp))
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Post Button
            Button(
                onClick = {
                    if (postContent.isBlank()) {
                        Toast.makeText(context, "Please write something", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    feedViewModel.createPost(postContent, imageUri) { success ->
                        if (success) {
                            Toast.makeText(context, "Community notified! ✅", Toast.LENGTH_SHORT).show()
                            onPostCreated()
                        } else {
                            val error = feedViewModel.errorMessage.value ?: "Unknown error"
                            Toast.makeText(context, "Failed: $error", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !isCreatingPost,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppBlue,
                    contentColor = Color.White,
                    disabledContainerColor = AppBlue.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                if (isCreatingPost) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Publish Post", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}