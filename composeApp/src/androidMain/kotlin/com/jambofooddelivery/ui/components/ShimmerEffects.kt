package com.jambofooddelivery.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

//@Composable
//fun FeaturedRestaurantsShimmer() {
//    LazyRow(
//        horizontalArrangement = Arrangement.spacedBy(16.dp),
//        contentPadding = PaddingValues(horizontal = 16.dp)
//    ) {
//        items(3) {
//            ShimmerCard(
//                modifier = Modifier.width(280.dp).height(240.dp)
//            )
//        }
//    }
//}

//@Composable
//fun NearbyRestaurantsShimmer() {
//    LazyColumn(
//        verticalArrangement = Arrangement.spacedBy(16.dp),
//        contentPadding = PaddingValues(horizontal = 16.dp)
//    ) {
//        items(4) {
//            ShimmerCard(
//                modifier = Modifier.fillMaxWidth().height(112.dp)
//            )
//        }
//    }
//}

@Composable
fun ShimmerCard(
    modifier: Modifier = Modifier
) {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    )

    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        )
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim, translateAnim),
        end = Offset(translateAnim + 500f, translateAnim + 500f)
    )

    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush)
        )
    }

}
//
