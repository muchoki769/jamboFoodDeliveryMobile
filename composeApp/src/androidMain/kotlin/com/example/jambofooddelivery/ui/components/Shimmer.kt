package com.example.jambofooddelivery.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FeaturedRestaurantsShimmer() {
    Box(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Loading...")
    }
}

@Composable
fun NearbyRestaurantsShimmer() {
    Box(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Loading...")
    }
}