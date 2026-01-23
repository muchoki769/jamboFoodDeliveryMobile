package com.example.jambofooddelivery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.jambofooddelivery.R
import com.example.jambofooddelivery.models.MenuItem
import com.example.jambofooddelivery.ui.ViewModels.RestaurantViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailScreen(
    restaurantId: String,
    onBack: () -> Unit,
    onAddToCart: () -> Unit
) {
    val viewModel: RestaurantViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(restaurantId) {
        viewModel.loadRestaurant(restaurantId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.restaurant?.name ?: "Restaurant") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    BadgedBox(
                        badge = {
                            if (state.totalCartItems > 0) {
                                Badge { Text(state.totalCartItems.toString()) }
                            }
                        },
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        IconButton(onClick = onAddToCart) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (state.totalCartItems > 0) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Button(
                        onClick = onAddToCart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${state.totalCartItems} items")
                            Text("View Cart", fontWeight = FontWeight.Bold)
                            Text("KSh ${"%.0f".format(state.totalCartAmount)}")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Restaurant Header
                item {
                    RestaurantHeader(state.restaurant?.coverImageUrl, state.restaurant?.name, state.restaurant?.ratingDouble ?: 0.0)
                }

                // Restaurant Info
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = state.restaurant?.name ?: "",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = state.restaurant?.description ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                // Categories Row
                item {
                    val categories = state.restaurant?.categories ?: emptyList()
                    if (categories.isNotEmpty()) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            items(categories, key = { it.id }) { category ->
                                FilterChip(
                                    selected = state.selectedCategory == category.name,
                                    onClick = { viewModel.selectCategory(category.name) },
                                    label = { Text(category.name) }
                                )
                            }
                        }
                    }
                }

                // Menu Items for Selected Category
                val selectedCategory = state.restaurant?.categories?.find { it.name == state.selectedCategory }
                val menuItems = selectedCategory?.items ?: emptyList()

                if (menuItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No items available in this category",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else {
                    items(menuItems, key = { it.id }) { menuItem ->
                        MenuItemRow(
                            menuItem = menuItem,
                            onAddClick = { viewModel.addToCart(menuItem) }
                        )
                    }
                }
                
                // Extra spacer for bottom bar
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun RestaurantHeader(imageUrl: String?, name: String?, rating: Double) {
    Box(modifier = Modifier.height(200.dp).fillMaxWidth()) {
        AsyncImage(
            model = getCloudinaryUrl(imageUrl, width = 800, height = 400),
            contentDescription = name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.placeholder_restaurant),
            error = painterResource(R.drawable.placeholder_restaurant)
        )

        Card(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Star, contentDescription = null, tint = Color(0xFFFFA000), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "%.1f".format(rating), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun MenuItemRow(menuItem: MenuItem, onAddClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = menuItem.name, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = menuItem.description ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "KSh ${"%.0f".format(menuItem.price)}", 
                    style = MaterialTheme.typography.titleSmall, 
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Box {
                AsyncImage(
                    model = getCloudinaryUrl(menuItem.imageUrl, width = 200, height = 200),
                    contentDescription = menuItem.name,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.placeholder_restaurant),
                    error = painterResource(R.drawable.placeholder_restaurant)
                )

                IconButton(
                    onClick = onAddClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 8.dp, y = 8.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Add, 
                        contentDescription = "Add", 
                        tint = Color.White, 
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
