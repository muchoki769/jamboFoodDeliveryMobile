package com.jambofooddelivery.Ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.jambofooddelivery.R
import com.jambofooddelivery.data.Category
import com.jambofooddelivery.models.Restaurant
import com.jambofooddelivery.repositories.AuthRepository
import com.jambofooddelivery.Ui.ViewModels.HomeViewModel
import com.jambofooddelivery.Ui.components.BottomNavigationBar
import com.jambofooddelivery.Ui.components.FeaturedRestaurantsShimmer
import com.jambofooddelivery.Ui.components.NearbyRestaurantsShimmer
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun HomeScreen(
    onNavigateToRestaurant: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val viewModel: HomeViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.loadRestaurants()
    }

    Scaffold(
        topBar = {
            HomeAppBar(
                address = state.address,
                onProfileClick = onNavigateToProfile,
                onCartClick = onNavigateToCart,
                onNotificationClick = onNavigateToNotifications
            )
        },
        bottomBar = {
            BottomNavigationBar(
                selectedRoute = "home",
                onHomeClick = { /* Already here */ },
                onOrdersClick = onNavigateToOrders,
                onProfileClick = onNavigateToProfile,
                onSettingsClick = onNavigateToSettings
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                onSearch = { query -> viewModel.searchRestaurants(query) }
            )

            CategoriesSection(
                categories = state.categories,
                onCategorySelected = { category -> viewModel.filterByCategory(category) }
            )

            FeaturedRestaurantsSection(
                restaurants = state.featuredRestaurants,
                isLoading = state.isLoading,
                onRestaurantClick = onNavigateToRestaurant
            )

            NearbyRestaurantsSection(
                restaurants = state.nearbyRestaurants,
                isLoading = state.isLoading,
                onRestaurantClick = onNavigateToRestaurant
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAppBar(
    address: String?,
    onProfileClick: () -> Unit,
    onCartClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    val authRepository: AuthRepository = koinInject()
    val currentUser by authRepository.getCurrentUser().collectAsState(initial = null)

    TopAppBar(
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = address ?: "Update Location",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = if (currentUser != null) "Hello, ${currentUser?.firstName}" else "Welcome to Jambo Food",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        actions = {
            IconButton(onClick = onNotificationClick) {
                Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
            }

            IconButton(onClick = onCartClick) {
                BadgedBox(badge = { Text("0") }) {
                    Icon(Icons.Outlined.ShoppingCart, contentDescription = "Cart")
                }
            }

            IconButton(onClick = onProfileClick) {
                AsyncImage(
                    model = getCloudinaryUrl(currentUser?.avatarUrl, width = 100, height = 100),
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                    placeholder = painterResource(R.drawable.placeholder_profile),
                    error = painterResource(R.drawable.placeholder_profile),
                    contentScale = ContentScale.Crop
                )
            }
        }
    )
}

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    onSearch: (String) -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(4.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            TextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                    onSearch(it)
                },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search for restaurants or food...") },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )
            if (searchText.isNotBlank()) {
                IconButton(onClick = { searchText = "" }) {
                    Icon(Icons.Outlined.Close, contentDescription = "Clear")
                }
            }
        }
    }
}

@Composable
fun CategoriesSection(
    categories: List<Category>,
    onCategorySelected: (Category) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = "Categories",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(categories, key = { it.name }) { category ->
                CategoryItem(category = category, onClick = { onCategorySelected(category) })
            }
        }
    }
}

@Composable
fun CategoryItem(category: Category, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(80.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = category.icon, contentDescription = category.name, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = category.name, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, maxLines = 2)
        }
    }
}

@Composable
fun FeaturedRestaurantsSection(restaurants: List<Restaurant>, isLoading: Boolean, onRestaurantClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Featured Restaurants", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            TextButton(onClick = { /* See all */ }) { Text("See all") }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (isLoading && restaurants.isEmpty()) {
            FeaturedRestaurantsShimmer()
        } else if (restaurants.isEmpty()) {
            Text(text = "No featured restaurants found", modifier = Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(horizontal = 16.dp)) {
                items(restaurants, key = { it.id }) { restaurant ->
                    FeaturedRestaurantCard(restaurant = restaurant, onClick = { onRestaurantClick(restaurant.id) })
                }
            }
        }
    }
}

@Composable
fun FeaturedRestaurantCard(restaurant: Restaurant, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.width(280.dp), shape = MaterialTheme.shapes.large, elevation = CardDefaults.cardElevation(4.dp)) {
        Column {
            Box(modifier = Modifier.height(160.dp)) {
                val imageUrl = restaurant.coverImageUrl ?: "jgm2_bacz_180706_rrfo50.jpg"
                AsyncImage(model = getCloudinaryUrl(imageUrl, width = 600, height = 300), contentDescription = restaurant.name, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, placeholder = painterResource(R.drawable.placeholder_restaurant), error = painterResource(R.drawable.placeholder_restaurant))
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                    Card(shape = MaterialTheme.shapes.small, colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Star, contentDescription = "Rating", modifier = Modifier.size(12.dp), tint = Color(0xFFFFA000))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(text = "%.1f".format(restaurant.ratingDouble), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = restaurant.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = restaurant.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = restaurant.deliveryTimeRange, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text(text = "KSh ${restaurant.deliveryFee} delivery", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@Composable
fun NearbyRestaurantsSection(restaurants: List<Restaurant>, isLoading: Boolean, onRestaurantClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Nearby Restaurants", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            TextButton(onClick = { /* See all */ }) { Text("See all") }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (isLoading && restaurants.isEmpty()) {
            NearbyRestaurantsShimmer()
        } else if (restaurants.isEmpty()) {
            Text(text = "No restaurants found nearby", modifier = Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodyMedium)
        } else {
            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                restaurants.forEach { restaurant ->
                    NearbyRestaurantCard(restaurant = restaurant, onClick = { onRestaurantClick(restaurant.id) })
                }
            }
        }
    }
}

@Composable
fun NearbyRestaurantCard(restaurant: Restaurant, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            val imageUrl = restaurant.logoUrl ?: restaurant.coverImageUrl ?: "jgm2_bacz_180706_rrfo50.jpg"
            AsyncImage(model = getCloudinaryUrl(imageUrl, width = 200, height = 200), contentDescription = restaurant.name, modifier = Modifier.size(80.dp).clip(MaterialTheme.shapes.medium), contentScale = ContentScale.Crop, placeholder = painterResource(R.drawable.placeholder_restaurant), error = painterResource(R.drawable.placeholder_restaurant))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = restaurant.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = restaurant.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Star, contentDescription = "Rating", modifier = Modifier.size(16.dp), tint = Color(0xFFFFA000))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "%.1f".format(restaurant.ratingDouble), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(Icons.Outlined.Schedule, contentDescription = "Delivery time", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = restaurant.deliveryTimeRange, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

fun getCloudinaryUrl(url: String?, width: Int = 800, height: Int = 400): String {
    if (url.isNullOrBlank()) return ""
    val cloudName = "dahss2ggd"
    val cleanPath = url.trim().removePrefix("/").removeSuffix("/")
    var fullUrl = if (cleanPath.startsWith("http")) cleanPath else "https://res.cloudinary.com/$cloudName/image/upload/$cleanPath"
    fullUrl = fullUrl.replace("/upload//", "/upload/")
    return if (fullUrl.contains("cloudinary.com") && fullUrl.contains("/upload/") && !fullUrl.contains("/w_")) {
        fullUrl.replace("/upload/", "/upload/w_$width,h_$height,c_limit,q_auto:eco,f_auto/")
    } else fullUrl
}
