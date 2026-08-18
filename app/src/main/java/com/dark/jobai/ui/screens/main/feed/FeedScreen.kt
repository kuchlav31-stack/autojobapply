package com.dark.jobai.ui.screens.main.feed

import android.Manifest
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dark.jobai.data.model.FeedPost
import com.dark.jobai.ui.theme.*
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
    var hasStoragePermission by remember { mutableStateOf(false) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasStoragePermission = permissions.values.all { it }

        if (hasStoragePermission) {
            showCreatePost = true
        } else {
            Toast.makeText(context, "Storage permission required to add images", Toast.LENGTH_LONG).show()
        }
    }

    // Check and request permission
    fun checkPermissionAndCreatePost() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ - No storage permission needed for image picker
            showCreatePost = true
        } else {
            // Android 12 and below - Need storage permission
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
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
                onClick = { checkPermissionAndCreatePost() },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(PrimaryGreen)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Create Post",
                    tint = Color.Black
                )
            }
        }

        // Content
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
                            "Tap + to create your first post!",
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
                    items(posts, key = { it.id }) { post ->
                        FeedPostCard(
                            post = post,
                            onLike = { feedViewModel.likePost(post) },
                            onDelete = {
                                if (post.userId == FirebaseAuth.getInstance().currentUser?.uid) {
                                    feedViewModel.deletePost(post.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Create Post Sheet
    if (showCreatePost) {
        CreatePostSheet(
            onDismiss = { showCreatePost = false },
            onPostCreated = {
                showCreatePost = false
                Toast.makeText(context, "✅ Post created!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

// ====================================================================
// FEED POST CARD
// ====================================================================

@Composable
fun FeedPostCard(
    post: FeedPost,
    onLike: () -> Unit,
    onDelete: () -> Unit = {}
) {
    var isLiked by remember { mutableStateOf(post.isLiked) }
    var likeCount by remember { mutableStateOf(post.likes) }
    var showMenu by remember { mutableStateOf(false) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, BorderGray.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // User Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Image
                if (post.userProfileImage.isNotEmpty()) {
                    AsyncImage(
                        model = post.userProfileImage,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                    )
                } else {
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
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
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
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        Formatters.formatTimeAgo(post.createdAt),
                        color = TextGray.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                }

                // Delete button (only for own posts)
                if (post.userId == currentUserId) {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = TextGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete", color = ErrorRed) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, null, tint = ErrorRed, modifier = Modifier.size(18.dp))
                            }
                        )
                    }
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
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            isLiked = !isLiked
                            if (isLiked) likeCount++ else likeCount--
                            onLike()
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) ErrorRed else TextGray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "$likeCount",
                        color = if (isLiked) ErrorRed else TextGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Comment
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
// CREATE POST SHEET (Permission Already Handled)
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

    // Image picker - No permission needed for Android 13+
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
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

            // Post Content
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
                    focusedContainerColor = BackgroundDark,
                    unfocusedContainerColor = BackgroundDark
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Image Upload
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BackgroundDark)
                    .clickable { imagePicker.launch("image/*") }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (imageUri != null) Icons.Default.CheckCircle else Icons.Default.Image,
                    contentDescription = null,
                    tint = if (imageUri != null) PrimaryGreen else TextGray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (imageUri != null) "Image Selected ✓" else "Add Image (Optional)",
                    color = TextGray,
                    fontSize = 14.sp
                )
            }

            // Show selected image
            if (imageUri != null) {
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(10.dp))
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
                    feedViewModel.createPost(postContent, imageUri)
                    onPostCreated()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isCreatingPost,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = Color.Black,
                    disabledContainerColor = PrimaryGreen.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isCreatingPost) {
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