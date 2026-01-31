package com.example.jambofooddelivery.ui.screens


import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.jambofooddelivery.R
import com.example.jambofooddelivery.data.Category
import com.example.jambofooddelivery.models.Restaurant
import com.example.jambofooddelivery.repositories.AuthRepository
import com.example.jambofooddelivery.ui.ViewModels.HomeViewModel
import com.example.jambofooddelivery.ui.components.FeaturedRestaurantsShimmer
import com.example.jambofooddelivery.ui.components.NearbyRestaurantsShimmer
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject


/**
 * Helper function to optimize Cloudinary URLs by adding transformation parameters.
 */
fun getCloudinaryUrl(url: String?, width: Int = 800, height: Int = 400): String {
    if (url.isNullOrBlank()) return ""
    
    val cloudName = "dahss2ggd"
    
    // Clean input path: remove leading/trailing whitespace and slashes
    val cleanPath = url.trim().removePrefix("/").removeSuffix("/")
    
    // Use the full URL directly if it starts with http
    var fullUrl = if (cleanPath.startsWith("http")) {
        cleanPath
    } else {
        "https://res.cloudinary.com/$cloudName/image/upload/$cleanPath"
    }

    // Ensure we don't have double slashes after upload/
    fullUrl = fullUrl.replace("/upload//", "/upload/")

    // Inject parameters if it's a Cloudinary URL and doesn't already have them
    return if (fullUrl.contains("cloudinary.com") && fullUrl.contains("/upload/") && !fullUrl.contains("/w_")) {
        fullUrl.replace("/upload/", "/upload/w_$width,h_$height,c_limit,q_auto:eco,f_auto/")
    } else {
        fullUrl
    }
}




@Composable
fun HomeScreen(
    onNavigateToRestaurant: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToNotifications: () -> Unit
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
                onOrdersClick = onNavigateToOrders,
                onProfileClick = onNavigateToProfile
            )
        }
    )  { paddingValues ->
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

            // Featured Restaurants
            FeaturedRestaurantsSection(
                restaurants = state.featuredRestaurants,
                isLoading = state.isLoading,
                onRestaurantClick = onNavigateToRestaurant
            )

            // Nearby Restaurants
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
                BadgedBox(badge = {
                    Text("0")
                }) {
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
    Column(
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
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
                CategoryItem(
                    category = category,
                    onClick = { onCategorySelected(category) }
                )
            }
        }
    }
}

@Composable
fun CategoryItem(
    category: Category,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(80.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.name,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = category.name,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}


@Composable
fun FeaturedRestaurantsSection(
    restaurants: List<Restaurant>,
    isLoading: Boolean,
    onRestaurantClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Featured Restaurants",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            TextButton(onClick = { /* See all */ }) {
                Text("See all")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading && restaurants.isEmpty()) {
            FeaturedRestaurantsShimmer()
        } else if (restaurants.isEmpty()) {
            Text(
                text = "No featured restaurants found",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(restaurants, key = { it.id }) { restaurant ->
                    FeaturedRestaurantCard(
                        restaurant = restaurant,
                        onClick = { onRestaurantClick(restaurant.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun FeaturedRestaurantCard(
    restaurant: Restaurant,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(280.dp),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            Box(modifier = Modifier.height(160.dp)) {
                // Prioritize full URL from API
                val imageUrl = restaurant.coverImageUrl ?: "jgm2_bacz_180706_rrfo50.jpg"

                AsyncImage(
                    model = getCloudinaryUrl(imageUrl, width = 600, height = 300),
                    contentDescription = restaurant.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.placeholder_restaurant),
                    error = painterResource(R.drawable.placeholder_restaurant)
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Card(
                        shape = MaterialTheme.shapes.small,
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Star,
                                contentDescription = "Rating",
                                modifier = Modifier.size(12.dp),
                                tint = Color(0xFFFFA000)
                            )
                            Spacer(modifier = Modifier.width(2.dp))

                            Text(
                                text = "%.1f".format(restaurant.ratingDouble),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = restaurant.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = restaurant.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = restaurant.deliveryTimeRange,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "KSh ${restaurant.deliveryFee} delivery",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun NearbyRestaurantsSection(
    restaurants: List<Restaurant>,
    isLoading: Boolean,
    onRestaurantClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Nearby Restaurants",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            TextButton(onClick = { /* See all */ }) {
                Text("See all")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading && restaurants.isEmpty()) {
            NearbyRestaurantsShimmer()
        } else if (restaurants.isEmpty()) {
            Text(
                text = "No restaurants found nearby",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                restaurants.forEach { restaurant ->
                    NearbyRestaurantCard(
                        restaurant = restaurant,
                        onClick = { onRestaurantClick(restaurant.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun NearbyRestaurantCard(
    restaurant: Restaurant,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Use coverImageUrl as fallback if logoUrl is missing from API
            val imageUrl = restaurant.logoUrl ?: restaurant.coverImageUrl ?: "jgm2_bacz_180706_rrfo50.jpg"

            AsyncImage(
                model = getCloudinaryUrl(imageUrl, width = 200, height = 200),
                contentDescription = restaurant.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.placeholder_restaurant),
                error = painterResource(R.drawable.placeholder_restaurant)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = restaurant.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = restaurant.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Star,
                        contentDescription = "Rating",
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFFFA000)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "%.1f".format(restaurant.ratingDouble),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(
                        Icons.Outlined.Schedule,
                        contentDescription = "Delivery time",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = restaurant.deliveryTimeRange,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    selectedRoute: String,
    onOrdersClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        NavigationBarItem(
            selected = selectedRoute == "home",
            onClick = { /* Already on home */ },
            icon = {
                Icon(Icons.Outlined.Home, contentDescription = "Home")
            },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = selectedRoute == "orders",
            onClick = onOrdersClick,
            icon = {
                Icon(Icons.Outlined.ReceiptLong, contentDescription = "Orders")
            },
            label = { Text("Orders") }
        )

        NavigationBarItem(
            selected = selectedRoute == "favorites",
            onClick = { /* Navigate to favorites */ },
            icon = {
                Icon(Icons.Outlined.Favorite, contentDescription = "Favorites")
            },
            label = { Text("Favorites") }
        )

        NavigationBarItem(
            selected = selectedRoute == "profile",
            onClick = onProfileClick,
            icon = {
                Icon(Icons.Outlined.Person, contentDescription = "Profile")
            },
            label = { Text("Profile") }
        )
    }
}
