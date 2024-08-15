package com.example.myshoppingapp

import android.Manifest
import android.content.Context
import android.graphics.drawable.Icon
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.example.myshoppingapp.ui.theme.MyShoppingAppTheme

data class Item (
    var id: Int,
    var itemName: String,
    var qty: String = "Qty not filled",
    var isEditing: Boolean = false,
    var address: String = ""
)

@Composable
fun ShoppingApp(
    locationUtilities: LocationUtilities,
    viewModel: LocationViewModel,
    navController: NavController,
    context: Context,
    address: String
) {
    var showDialog by remember { mutableStateOf(false) }
    var itemList by remember { mutableStateOf(listOf<Item>()) }
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }

    val requestPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = {
                permissions->
            if(permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                && permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true){

                locationUtilities.requestLocationUpdates(viewModel)
            }else{
                // Display a toast to explain why this permission is required
                val rationaleRequired = ActivityCompat.shouldShowRequestPermissionRationale(
                    context as MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) || ActivityCompat.shouldShowRequestPermissionRationale(
                    context as MainActivity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )

                if(rationaleRequired){
                    Toast.makeText(
                        context,
                        "Location permission is required add location",
                        Toast.LENGTH_LONG
                    ).show()
                }else{
                    Toast.makeText(
                        context,
                        "Permission to access precise location denied. Please change it in the settings",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Button(
            onClick = { showDialog = true}
        ) {
            Text("Add Item")
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ){
            items(itemList){
                currItemDetails ->
                if(!currItemDetails.isEditing){
                    ItemDisplay(
                        itemDetails = currItemDetails,
                        onEditClick = {
                            itemList = itemList.map { it.copy(isEditing = it.id==currItemDetails.id) }
                        },
                        onDeleteClick = {
                            itemList = itemList - currItemDetails
                        }
                    )
                }else{
                    ItemEditorDisplay(
                        oldItem = currItemDetails,
                        onClickSave = {
                            newName, newQuantity ->
                            itemList = itemList.map { it.copy(isEditing = false) }
                            itemList.find{it.id == currItemDetails.id}?.itemName = newName
                            itemList.find{it.id == currItemDetails.id}?.qty = newQuantity
                            itemList.find{it.id == currItemDetails.id}?.address = address

                        }
                    )
                }
            }
        }
    }

    if(showDialog){
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                name = ""
                quantity = ""
            },
            confirmButton = {
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Button(
                        onClick = {
                            if(name.isNotBlank()) {
                                val listItem = Item(
                                    id = itemList.size + 1,
                                    itemName = name,
                                    qty = quantity,
                                    address = address

                                )
                                itemList = itemList + listItem
                                showDialog = false
                                name = ""
                                quantity = ""
                            }
                        }
                    ) {
                        Text(text = "Add")
                    }
                    Button(
                        onClick = {
                            showDialog=false
                            name = ""
                            quantity = ""
                        }
                    ) {
                        Text(text = "Cancel")
                    }
                }
            },
            title = { Text(text = "Add Item Details") },
            text = {
                Column(
                    modifier = Modifier.padding(8.dp)
                ){
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                             name =  it
                        },
                        label = { Text(text = "Enter Item Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = {
                            quantity = it
                        },
                        label = { Text(text = "Enter Quantity") },
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            if(locationUtilities.hasLocationPermission(context)){
                                locationUtilities.requestLocationUpdates(viewModel)
                                navController.navigate("locationScreen"){
                                    this.launchSingleTop
                                }
                            }else{
                                requestPermission.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    )
                                )
                            }
                        }
                    ) {
                        Text(text = "Add address")
                    }

                }

            }
        )
    }

}

@Composable
fun ItemDisplay(
    itemDetails: Item,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
  ){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border
                (
                border = BorderStroke(2.dp, Color.Blue),
                shape = RoundedCornerShape(20)
            ),
        horizontalArrangement = Arrangement.SpaceBetween

    ){
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        ) {
            Row {
                Text(text = itemDetails.itemName, modifier = Modifier.padding(8.dp))
                Text(text = "Qty: ${itemDetails.qty}", modifier = Modifier.padding(8.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                androidx.compose.material3.Icon(imageVector = Icons.Default.LocationOn, contentDescription = null)
                Text(text = itemDetails.address)
            }
        }

        Row(modifier = Modifier.padding(8.dp)){
            IconButton(
                onClick = onEditClick
            ) {
                androidx.compose.material3.Icon(imageVector = Icons.Default.Edit, contentDescription = null)
            }

            IconButton(
                onClick = onDeleteClick
            ) {
                androidx.compose.material3.Icon(imageVector = Icons.Default.Delete, contentDescription = null)
            }


        }
    }
}

@Composable
fun ItemEditorDisplay(
    oldItem: Item,
    onClickSave: (String,String)->Unit
){
    var editedName by remember { mutableStateOf(oldItem.itemName) }
    var editedQty by remember { mutableStateOf(oldItem.qty) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            BasicTextField(
                value = editedName,
                onValueChange = {editedName = it},
                singleLine = true,
                modifier = Modifier.wrapContentWidth()

            )
            BasicTextField(
                value = editedQty,
                onValueChange = {editedQty = it},
                singleLine = true,
                modifier = Modifier.wrapContentWidth()

            )
        }

        Button(
            onClick = {
                onClickSave(editedName, editedQty)
            }
        ) {
            Text(text = "Save")
        }
    }
}