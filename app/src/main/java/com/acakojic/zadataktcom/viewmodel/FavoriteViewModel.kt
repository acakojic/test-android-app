package com.acakojic.zadataktcom.viewmodel

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.acakojic.zadataktcom.data.Vehicle
import com.acakojic.zadataktcom.service.CustomRepository
import kotlinx.coroutines.launch

/**
 * ViewModel to handle logic for displaying favorite vehicles.
 */
class FavoriteViewModel


/**
 * Displays a list of favorite vehicles.
 *
 * @param viewModel The view model managing vehicle data.
 * @param navController Controller for managing navigation in the app.
 */
@Composable
fun FavoritesScreen(viewModel: MapViewModel, navController: NavHostController) {
    val allVehicles = viewModel.allVehicles.collectAsState().value
    val favoriteVehicles = allVehicles.filter { it.isFavorite }

    Log.d("FavoritesScreen", "Displaying ${favoriteVehicles.size} favorite vehicles.")

    ShowVehiclesInList(
        vehicles = favoriteVehicles,
        viewModel = viewModel,
        navController = navController,
        elementOnTopOfScreen = { CenteredText("Omiljena vozila") })
}


/**
 * Composable function that displays a list of vehicles.
 *
 * @param vehicles List of vehicles to display.
 * @param viewModel ViewModel that contains the business logic.
 * @param navController Navigation controller.
 * @param elementOnTopOfScreen Composable function to display additional content above the list.
 */
@Composable
fun ShowVehiclesInList(
    vehicles: List<Vehicle>,
    viewModel: MapViewModel,
    navController: NavController,
    elementOnTopOfScreen: @Composable () -> Unit
) {
    Column {
        elementOnTopOfScreen()

        LazyColumn {
            items(vehicles) { vehicle ->
                Spacer(modifier = Modifier.height(20.dp))
                VehicleCard(
                    vehicle = vehicle,
                    viewModel = viewModel,
                    modifier = getModifierForFavorite(),
                    navController = navController
                )
            }
        }
    }
}

/**
 * Displays detailed information about a vehicle.
 *
 * @param vehicleID Identifier for the vehicle.
 * @param viewModel ViewModel associated with vehicle operations.
 */
@Composable
fun ShowVehicleInfo(vehicleID: Int, viewModel: MapViewModel) {
    val context = LocalContext.current
    val (vehicle, setVehicle) = remember { mutableStateOf<Vehicle?>(null) }
    val (isLoading, setLoading) = remember { mutableStateOf(true) }
    val repository = remember { CustomRepository(context) }

    LaunchedEffect(vehicleID) {
        viewModel.viewModelScope.launch {
            setLoading(true)
            try {
                val response =
                    repository.getVehicleDetails(context = context, vehicleID = vehicleID)
                if (response.isSuccessful) {
                    Log.d(
                        "ShowVehicleInfo",
                        "Vehicle details fetched successfully for vehicle ID: $vehicleID"
                    )
                    setVehicle(response.body())
                } else {
                    Log.e(
                        "ShowVehicleInfo",
                        "Failed to fetch vehicle details for vehicle ID: $vehicleID"
                    )
                }
            } finally {
                setLoading(false)
            }
        }
    }

    if (isLoading) {
        CircularProgressIndicator() //loading indicator while fetching data
    } else {
        vehicle?.let {
            //Display the vehicle details
            ShowVehicleDescription(
                vehicleDetails = it,
                viewModel = viewModel
            )
        }
    }
}

/**
 * Composable function to display various attributes of a vehicle.
 *
 * @param vehicleDetails The vehicle whose details are to be displayed.
 * @param viewModel ViewModel associated with vehicle operations.
 */
@Composable
fun ShowVehicleDescription(
    vehicleDetails: Vehicle,
    viewModel: MapViewModel
) {

    Column(
        modifier = Modifier
            .background(Color.Black),
    ) {
        CustomTopRow(title = "Vozilo")
        Spacer(modifier = Modifier.height(20.dp))

        Column(modifier = getModifierForFavorite()) {
            VehicleImageWithFavorite(
                vehicle = vehicleDetails,
                viewModel = viewModel
            )
            Spacer(modifier = Modifier.height(22.dp))

            //Model
            CustomRow(
                text = { Text(text = "Model:") },
                value = { Text(text = vehicleDetails.name) },
                icon = null
            )

            //Rating
            CustomRow(
                text = { Text(text = "Rating:") },
                value = { Text(text = vehicleDetails.rating.toString()) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating Star",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                })
            //cena
            CustomRow(
                text = { Text(text = "Cena:") },
                value = { Text(text = vehicleDetails.price.toString()) },
                icon = null
            )
            //Latitude
            CustomRow(
                text = { Text(text = "Latitude:") },
                value = { Text(text = vehicleDetails.location.latitude.toString()) },
                icon = null
            )
            //Longitude
            CustomRow(
                text = { Text(text = "Longitude:") },
                value = { Text(text = vehicleDetails.location.longitude.toString()) },
                icon = null
            )
        }
    }
}

/**
 * Utility function to apply consistent styling for vehicle cards.
 *
 * @return Modifier with the defined styling.
 */
@Composable
fun getModifierForFavorite(): Modifier {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val horizontalPadding = screenWidth * 0.03f // 3% of screen width
    val verticalPadding = screenWidth * 0.03f // 3% for top and bottom

    return Modifier
        .padding(horizontal = horizontalPadding, vertical = verticalPadding)
        .clip(RoundedCornerShape(9.dp))
        .background(Color.Black)
        .shadow(8.dp, RoundedCornerShape(10.dp))
        .fillMaxWidth()
}

/**
 * Displays a text field centered within its parent.
 *
 * @param text Text to be displayed.
 */
@Composable
fun CenteredText(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), // Add some padding around the text
        textAlign = TextAlign.Center, // Center the text horizontally
        style = MaterialTheme.typography.bodyLarge // Apply a text style, optional
    )
}

/**
 * Constructs a custom row layout with an optional icon.
 *
 * @param text Composable function that describes the text to be displayed.
 * @param value Composable function that describes the value to be displayed.
 * @param icon Optional composable function that describes the icon to be displayed.
 */
@Composable
fun CustomRow(
    text: @Composable () -> Unit,
    value: @Composable () -> Unit,
    icon: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        text()
        Spacer(Modifier.weight(1f))
        value()
        icon?.invoke()
    }
    Spacer(modifier = Modifier.height(22.dp))
}

/**
 * Constructs a row layout for the top part of a screen, typically used for navigation.
 *
 * @param title The title text to display.
 */
@Composable
fun CustomTopRow(title: String) {
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.KeyboardArrowLeft,
            contentDescription = "Back icon",
            modifier = Modifier.size(33.dp),
            tint = Color(0xFFFFA500)
        )
        Text(
            text = "Back",
            modifier = Modifier
                .clickable { backDispatcher?.onBackPressed() },
            color = Color(0xFFFFA500)
        )
        Box(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        }
        //Invisible box to balance the title
        Box(
            modifier = Modifier
                .padding(6.dp)
                .sizeIn(minWidth = 64.dp, minHeight = 48.dp)
        )
    }
}