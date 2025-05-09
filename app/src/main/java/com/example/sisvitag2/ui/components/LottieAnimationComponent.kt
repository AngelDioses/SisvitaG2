package com.example.sisvitag2.ui.components

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntSize
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun LottieAnimationComponent(
    resId: Int,
    modifier: Modifier = Modifier,
    iterations: Int = LottieConstants.IterateForever,
    isPlaying: Boolean = true,
    speed: Float = 1f,
    scaleY: Float = 1f
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(resId))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = iterations,
        isPlaying = isPlaying,
        speed = speed,
    )

    val intrinsicSize = composition?.bounds?.let {
        IntSize(it.width(), it.height())
    }

    // Aplicar el aspectRatio solo si no se ha especificado un tamaño en el modifier
    val modifierWithSize = if (intrinsicSize != null && modifier == Modifier) {
        modifier
            .fillMaxWidth()
            .aspectRatio(intrinsicSize.width.toFloat() / intrinsicSize.height.toFloat())
    } else {
        modifier
    }

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifierWithSize.graphicsLayer(scaleY = scaleY)
    )
}

