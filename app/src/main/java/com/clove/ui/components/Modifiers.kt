package com.clove.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

/**
 * Swallows tap gestures (no ripple) so an opaque overlay doesn't let touches fall
 * through to the WebView rendered beneath it.
 */
fun Modifier.consumeClicks(): Modifier = composed {
    val source = remember { MutableInteractionSource() }
    clickable(interactionSource = source, indication = null) {}
}

/** combinedClickable that tolerates a null long-press handler and a disabled state. */
@OptIn(ExperimentalFoundationApi::class)
fun clickableCompat(
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
): Modifier = Modifier.composed {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    combinedClickable(
        interactionSource = source,
        indication = LocalIndication.current,
        enabled = enabled,
        onClick = onClick,
        onLongClick = onLongClick,
    )
}
