package com.clove.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clove.ui.components.clickableCompat
import com.clove.ui.theme.LocalSafariPalette
import com.composables.icons.lucide.EyeOff
import com.composables.icons.lucide.Lucide

private data class Favorite(val title: String, val url: String, val color: Color)

private val defaultFavorites = listOf(
    Favorite("Google", "https://www.google.com", Color(0xFF4285F4)),
    Favorite("YouTube", "https://www.youtube.com", Color(0xFFFF0000)),
    Favorite("Wikipedia", "https://www.wikipedia.org", Color(0xFF636466)),
    Favorite("Reddit", "https://www.reddit.com", Color(0xFFFF4500)),
    Favorite("X", "https://www.x.com", Color(0xFF111111)),
    Favorite("GitHub", "https://www.github.com", Color(0xFF24292E)),
    Favorite("Apple", "https://www.apple.com", Color(0xFF555555)),
    Favorite("Amazon", "https://www.amazon.com", Color(0xFFFF9900)),
)

@Composable
fun StartPage(
    isPrivate: Boolean,
    onOpen: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalSafariPalette.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.pageBackground),
    ) {
        if (isPrivate) {
            PrivateNotice()
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 160.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = "Favorites",
                        color = palette.textPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
                    )
                }
                items(defaultFavorites) { fav ->
                    FavoriteTile(fav, onOpen)
                }
            }
        }
    }
}

@Composable
private fun FavoriteTile(fav: Favorite, onOpen: (String) -> Unit) {
    val palette = LocalSafariPalette.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.then(clickableCompat(onClick = { onOpen(fav.url) })),
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(fav.color),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = fav.title.take(1),
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Text(
            text = fav.title,
            color = palette.textPrimary,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Composable
private fun PrivateNotice() {
    val palette = LocalSafariPalette.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(palette.glassElevated),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Lucide.EyeOff,
                contentDescription = null,
                tint = palette.textPrimary,
                modifier = Modifier.size(34.dp),
            )
        }
        Text(
            text = "Private Browsing Enabled",
            color = palette.textPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 20.dp),
        )
        Text(
            text = "Safari won't remember the pages you visit, your search history, or your AutoFill information.",
            color = palette.textSecondary,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 10.dp),
        )
    }
}
