package com.compose.trackit

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.compose.trackit.ui.theme.TrackItTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.compose.trackit.resources.Res
import com.compose.trackit.resources.ic_account_box
import com.compose.trackit.resources.ic_favorite
import com.compose.trackit.resources.ic_home

@Composable
fun App() {
  TrackItTheme {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    Scaffold(
      modifier = Modifier.fillMaxSize(),
      bottomBar = {
        NavigationBar {
          AppDestinations.entries.forEach { destination ->
            NavigationBarItem(
              selected = destination == currentDestination,
              onClick = { currentDestination = destination },
              icon = {
                Icon(
                  painter = painterResource(destination.icon),
                  contentDescription = destination.label
                )
              },
              label = { Text(destination.label) }
            )
          }
        }
      }
    ) { innerPadding ->
      Greeting(
        name = "Android",
        modifier = Modifier.padding(innerPadding)
      )
    }
  }
}

enum class AppDestinations(
  val label: String,
  val icon: DrawableResource,
) {
  HOME("Home", Res.drawable.ic_home),
  FAVORITES("Favorites", Res.drawable.ic_favorite),
  PROFILE("Profile", Res.drawable.ic_account_box),
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(
    text = "Hello $name!",
    modifier = modifier
  )
}

@Preview
@Composable
fun AppPreview() {
  App()
}
