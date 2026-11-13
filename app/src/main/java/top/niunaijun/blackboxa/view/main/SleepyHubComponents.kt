package top.niunaijun.blackboxa.view.main

import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.Icons
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun CardGroup(
    title: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (!title.isNullOrBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun CardGroupDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 60.dp, end = 14.dp)
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.32f)),
    )
}

@Composable
fun CardGroupItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    trailing: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val rowModifier = if (onClick != null) {
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = enabled, onClick = onClick)
    } else {
        modifier.fillMaxWidth()
    }

    Row(
        modifier = rowModifier
            .defaultMinSize(minHeight = if (subtitle.isNullOrBlank()) 64.dp else 76.dp)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (icon != null) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(21.dp),
                    )
                }
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (trailing != null) trailing()
    }
}

@Composable
fun ProfileAvatar(
    name: String,
    uri: String?,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val shape = CircleShape
    if (uri.isNullOrBlank()) {
        Box(
            modifier = modifier
                .size(size)
                .clip(shape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = name.trim().firstOrNull()?.uppercase() ?: "S",
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = (size.value * 0.42f).sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    } else {
        AsyncImage(
            model = ImageRequest.Builder(context).data(uri).crossfade(260).build(),
            contentDescription = "$name avatar",
            contentScale = ContentScale.Crop,
            modifier = modifier.size(size).clip(shape),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LargeFlexibleTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    LargeTopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        windowInsets = windowInsets,
        colors = colors,
        scrollBehavior = scrollBehavior,
    )
}

@Composable
fun GooglePlayBadge(
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
) {
    val context = LocalContext.current
    val playIcon: Drawable? = remember(context) {
        runCatching {
            context.packageManager.getApplicationIcon("com.android.vending")
        }.getOrNull()
    }

    if (playIcon != null) {
        AndroidView(
            factory = { ImageView(it).apply { scaleType = ImageView.ScaleType.CENTER_CROP } },
            update = { it.setImageDrawable(playIcon) },
            modifier = modifier.size(size).clip(CircleShape),
        )
    } else {
        Canvas(modifier = modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val blue = Path().apply {
                moveTo(w * 0.12f, h * 0.08f)
                lineTo(w * 0.58f, h * 0.50f)
                lineTo(w * 0.12f, h * 0.92f)
                close()
            }
            val red = Path().apply {
                moveTo(w * 0.12f, h * 0.08f)
                lineTo(w * 0.88f, h * 0.42f)
                lineTo(w * 0.60f, h * 0.50f)
                close()
            }
            val yellow = Path().apply {
                moveTo(w * 0.60f, h * 0.50f)
                lineTo(w * 0.88f, h * 0.42f)
                lineTo(w * 0.88f, h * 0.58f)
                close()
            }
            val green = Path().apply {
                moveTo(w * 0.12f, h * 0.92f)
                lineTo(w * 0.60f, h * 0.50f)
                lineTo(w * 0.88f, h * 0.58f)
                close()
            }
            drawPath(blue, Color(0xFF4285F4))
            drawPath(red, Color(0xFFEA4335))
            drawPath(yellow, Color(0xFFFBBC04))
            drawPath(green, Color(0xFF34A853))
        }
    }
}

@Composable
fun RikkaSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = CircleShape,
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        trailingIcon = if (value.isNotBlank()) {
            {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(Icons.Outlined.Close, contentDescription = "Clear search")
                }
            }
        } else null,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceBright,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceBright,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceBright,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
    )
}

@Composable
fun ProfileWorkspaceBackground(
    profile: ProfileItem?,
    modifier: Modifier = Modifier,
) {
    val uri = profile?.backgroundUri
    val opacity = profile?.backgroundOpacity ?: 0f
    val alpha by animateFloatAsState(
        targetValue = if (uri.isNullOrBlank()) 0f else opacity.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 420),
        label = "profile-background-opacity",
    )
    if (!uri.isNullOrBlank()) {
        val context = LocalContext.current
        AsyncImage(
            model = ImageRequest.Builder(context).data(uri).crossfade(420).build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.fillMaxSize(),
            alpha = alpha,
        )
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.16f),
                            Color.Black.copy(alpha = 0.44f),
                        ),
                    ),
                ),
        )
    }
}
