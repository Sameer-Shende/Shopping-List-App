package com.example.myshoppingapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import com.example.myshoppingapp.ui.theme.MyShoppingAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyShoppingAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Navigation()
                }
            }
        }
    }
}

@Composable
fun Navigation(){
    val navController = rememberNavController()
    val viewModel: LocationViewModel = viewModel()
    val context = LocalContext.current
    val locationUtilities  = LocationUtilities(context)

    NavHost(navController = navController, startDestination = "shoppingListScreen",
        builder = {
            composable("shoppingListScreen"){
                ShoppingApp(
                    locationUtilities = locationUtilities,
                    viewModel = viewModel,
                    navController = navController,
                    context = context,
                    address = viewModel.address.value.firstOrNull()?.formatted_address ?: "No Address"
                )
            }

            dialog("locationScreen"){
                viewModel.location.value?.let{locationData ->
                    LocationSelectionScreen(location = locationData,
                        onLocationSelected = {
                            viewModel.fetchAddress("${it.latitude}, ${it.longitude}")
                            navController.popBackStack()
                        }
                    )

                }
            }
        }
    )
}