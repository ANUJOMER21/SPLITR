package com.omer.expensetracker.presentation.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.ui.graphics.vector.ImageVector

/** Maps a [com.omer.expensetracker.domain.model.Category.iconKey] to the icon rendered for it.
 * Keys are stored as plain strings in the DB so the icon set can grow without a migration. */
object CategoryIconProvider {

    private val icons: Map<String, ImageVector> = mapOf(
        "food" to Icons.Filled.Fastfood,
        "transport" to Icons.Filled.DirectionsCar,
        "bills" to Icons.Filled.Receipt,
        "shopping" to Icons.Filled.ShoppingCart,
        "entertainment" to Icons.Filled.Movie,
        "health" to Icons.Filled.LocalHospital,
        "groceries" to Icons.Filled.LocalGroceryStore,
        "savings" to Icons.Filled.Savings,
        "other" to Icons.Filled.Category,
        "home" to Icons.Filled.Home,
        "travel" to Icons.Filled.Flight,
        "education" to Icons.Filled.School,
        "pets" to Icons.Filled.Pets,
        "fitness" to Icons.Filled.FitnessCenter,
        "coffee" to Icons.Filled.LocalCafe,
        "phone" to Icons.Filled.PhoneAndroid,
        "gaming" to Icons.Filled.SportsEsports
    )

    val pickableKeys: List<String> = icons.keys.toList()

    fun iconFor(iconKey: String): ImageVector = icons[iconKey] ?: Icons.Filled.Category
}
