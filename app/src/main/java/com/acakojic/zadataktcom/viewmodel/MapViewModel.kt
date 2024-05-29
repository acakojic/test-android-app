package com.acakojic.zadataktcom.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.preference.PreferenceManager
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.acakojic.zadataktcom.service.CustomRepository

import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.acakojic.zadataktcom.R
import com.acakojic.zadataktcom.data.Vehicle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory


/**
 * ViewModel to manage the map and vehicle-related operations.
 */
class MapViewModel(private val customRepository: CustomRepository, private val context: Context) :
    ViewModel() {
    private val _vehiclesByType = MutableLiveData<List<Vehicle>?>()
    val vehiclesByType: LiveData<List<Vehicle>?> = _vehiclesByType

    private val _allVehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val allVehicles: StateFlow<List<Vehicle>> = _allVehicles.asStateFlow()

    var selectedVehicleType = mutableStateOf(VehicleType.Auto)
        private set

    val searchQuery = mutableStateOf("")

    init {
        fetchVehicles()
    }

    /**
     * Fetches vehicles based on the selected vehicle type.
     */
    fun fetchVehicles() {
        viewModelScope.launch {
            val response = customRepository.getAllVehicles(context)
            if (response.isSuccessful && response.body() != null) {
                _vehiclesByType.value = response.body()!!.filter {
                    it.vehicleTypeID == selectedVehicleType.value.typeId
                }
                _allVehicles.value = response.body()!!
            } else {
                Log.d("MapViewModel", "Failed to fetch vehicles")
                _vehiclesByType.value = emptyList()
                _allVehicles.value = emptyList()
            }
        }
    }

    /**
     * Sets the vehicle type and fetches vehicles accordingly.
     */
    fun setVehicleType(type: VehicleType) {
        selectedVehicleType.value = type
        fetchVehicles()
        searchQuery.value = ""
        Log.d("MapViewModel", "Vehicle type set and vehicles fetched for type: ${type.name}")
    }


    /**
     * Toggles the favorite status of a vehicle.
     */
    fun toggleFavorite(vehicleId: Int, isFavorite: Boolean) {
        viewModelScope.launch {

            viewModelScope.launch {
                val result = customRepository.addToFavorites(context, vehicleId)
                if (result.isSuccess) {
                    Log.d("MapViewModel", "Favorite status toggled for vehicle ID: $vehicleId")
                    _allVehicles.value = _allVehicles.value.map { vehicle ->
                        if (vehicle.vehicleID == vehicleId) vehicle.copy(isFavorite = isFavorite) else vehicle
                    }
                    _vehiclesByType.value = _vehiclesByType.value?.map { vehicle ->
                        if (vehicle.vehicleID == vehicleId) vehicle.copy(isFavorite = isFavorite) else vehicle
                    }
                } else {
                    Log.e("MapViewModel", "Failed to toggle favorite status for vehicle ID: $vehicleId")
                }
            }
        }
    }

    /**
     * Updates the map markers based on the vehicle data.
     */
    fun updateMapMarkers(
        mapView: MapView,
        vehicles: List<Vehicle>,
        onVehicleClick: (Vehicle) -> Unit
    ) {
        _vehiclesByType.value = vehicles
        createMarkerForVehicle(context, mapView, vehicles, onVehicleClick)
        Log.d("MapViewModel", "Map markers updated")
    }

    /**
     * Responds to changes in search query by refetching vehicles.
     */
    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query

        if (query.isEmpty()) {
            setVehicleType(selectedVehicleType.value)
        } else {
        }
    }

    /**
     * Creates a marker for a vehicle on the map.
     */
    fun createMarkerForVehicle(
        context: Context,
        mapView: MapView,
        vehicles: List<Vehicle>,
        onVehicleClick: (Vehicle) -> Unit
    ) {
        mapView.overlays.clear()

        vehicles.forEach { vehicle ->
            val marker = Marker(mapView).apply {
                position = GeoPoint(vehicle.location.latitude, vehicle.location.longitude)
                title = vehicle.name
                snippet = "${vehicle.price}€"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = getMarkerIconWithText(
                    context = context,
                    isFavorite = vehicle.isFavorite,
                    price = vehicle.price
                )

                setOnMarkerClickListener { marker, mapView ->
                    onVehicleClick(vehicle)
                    true  //true to indicate that the event has been handled
                }
            }
            mapView.overlays.add(marker)
        }
        mapView.invalidate()  //Refresh the map
    }

    fun getMarkerIconWithText(context: Context, isFavorite: Boolean, price: Double): Drawable {
        val drawable = if (isFavorite) {
            ContextCompat.getDrawable(context, R.drawable.circle_orange) //cars
        } else {
            ContextCompat.getDrawable(context, R.drawable.circle_black) // Default case
        }

        drawable?.let {
            // bitmap from the drawable
            val width = if (it.intrinsicWidth > 0) it.intrinsicWidth else 100
            val height = if (it.intrinsicHeight > 0) it.intrinsicHeight else 100

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            it.setBounds(0, 0, canvas.width, canvas.height)
            it.draw(canvas)

            // Draw text
            val text = "$${price}"
            val paint = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 30f
                textAlign = Paint.Align.CENTER
            }
            // Calculate vertical position to center text
            val textHeight = paint.descent() - paint.ascent()
            val textOffset = (textHeight / 2) - paint.descent()
            canvas.drawText(text, canvas.width / 2f, canvas.height / 2f + textOffset, paint)

            return BitmapDrawable(context.resources, bitmap)
        }
        return ContextCompat.getDrawable(context, R.drawable.circle_black)!! // Fallback
    }
}

fun initializeMap(context: Context): MapView {
    Configuration.getInstance()
        .load(context, PreferenceManager.getDefaultSharedPreferences(context))

    val mapView = MapView(context)
    mapView.setTileSource(TileSourceFactory.MAPNIK)
    mapView.setBuiltInZoomControls(true)
//    mapView.isMultiTouchControls = true
    mapView.controller.setZoom(12.0)
    mapView.controller.setCenter(
        //Belgrade starting location
        GeoPoint(
            44.81722374773659,
            20.460807455759323
        )
    )

    return mapView
}

@Composable
fun VehicleMapScreen(viewModel: MapViewModel, onVehicleClick: (Vehicle) -> Unit) {
    val vehicles by viewModel.vehiclesByType.observeAsState(emptyList())

    val context = LocalContext.current
    val mapView = remember { initializeMap(context) }

    LaunchedEffect(vehicles) {
        mapView.overlays.clear()
        vehicles?.forEach { _ ->
            viewModel.updateMapMarkers(mapView, vehicles!!, onVehicleClick)
        }
        mapView.invalidate()
    }

    AndroidView(factory = { mapView })
}

@Composable
fun VehicleImageWithFavorite(
    vehicle: Vehicle,
    viewModel: MapViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { CustomRepository(context) }

    val vehicles = viewModel.allVehicles.collectAsState()
    val vehicle = vehicles.value.find { it.vehicleID == vehicle.vehicleID }


    Box(contentAlignment = Alignment.TopEnd) {

        if (vehicle != null) {
            Image(
                painter = rememberImagePainter(
                    data = vehicle.imageURL,
                    builder = {
                        crossfade(true)
                    }
                ),
                contentDescription = "Vehicle Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }

        vehicle?.let {
            IconToggleButton(
                checked = it.isFavorite,
                onCheckedChange = { isChecked ->
                    viewModel.toggleFavorite(it.vehicleID, isChecked)
                }
            ) {
                Icon(
                    imageVector = if (it.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Toggle Favorite",
                    modifier = Modifier.size(33.dp),
                )
            }
        }
    }
}

@Composable
fun VehicleDetailDialog(
    vehicle: Vehicle?,
    onDismiss: () -> Unit,
    viewModel: MapViewModel,
    navController: NavController
) {
    if (vehicle != null) {

        AlertDialog(
            onDismissRequest = onDismiss,
            text = {
                VehicleCard(
                    vehicle = vehicle, viewModel = viewModel,
                    modifier = Modifier.fillMaxWidth(),
                    navController = navController,
                    onDismiss = onDismiss
                )
            },
            confirmButton = {}
        )
    }
}

@Composable
fun VehicleCard(
    vehicle: Vehicle,
    viewModel: MapViewModel,
    modifier: Modifier,
    navController: NavController?,
    onDismiss: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
    ) {

        VehicleImageWithFavorite(
            vehicle = vehicle,
            viewModel = viewModel
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (navController != null) {
                        Log.d("VehicleCard", "Clickable: ${vehicle.vehicleID}")
                        onDismiss?.invoke()
                        navController.navigate("vehicleDetails/${vehicle.vehicleID}")
                    }
                }
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = vehicle.name)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${vehicle.rating}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize * 0.7
                    )
                )
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rating Star",
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
                Spacer(Modifier.weight(1f))
                Text("${vehicle.price}€")
            }
        }
        //end of this
    }
}



