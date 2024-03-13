import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.testmobsec.BottomAppBarContent
import com.example.testmobsec.R
import com.example.testmobsec.TopAppBarContent
import com.example.testmobsec.util.Band

@Composable
fun SearchBandScreen(navController: NavController, bandViewModel: BandViewModel = viewModel()) {
    // State for managing search query
    var searchQuery by remember { mutableStateOf("") }
    val allBands by bandViewModel.allBands.collectAsState(initial = emptyList())

    // Fetch all bands when the screen is first composed
    LaunchedEffect(Unit) {
        bandViewModel.fetchAllBands()
    }

    // Filtered list of bands based on search query
    val filteredBands = allBands.filter {
        it.bandName.contains(searchQuery, ignoreCase = true)
    }

    // Assuming you're calling fetchAllBands somewhere to fill allBands StateFlow
    // For instance, bandViewModel.fetchAllBands() could be called on composable's first composition.

    Scaffold(
        topBar = { TopAppBarContent(navController) },
        bottomBar = { BottomAppBarContent(navController) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Enter Band name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                items(filteredBands) { band ->
                    BandItem(band, navController, bandViewModel)
                }
            }
        }
    }
}

@Composable
fun BandItem(band: Band, navController: NavController, bandViewModel: BandViewModel = viewModel()) {
    Card(
        modifier = Modifier
            .padding(8.dp) // Add padding around the card for proper spacing in the grid.
            .fillMaxWidth()
            .height(180.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, // Center content horizontally
            verticalArrangement = Arrangement.Center, // Center content vertically
            modifier = Modifier.fillMaxSize() // Ensure the column takes the full size of the card
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(data = band.imageUrl)
                        .error(R.drawable.ic_launcher_foreground)
                        .build()
                ),
                contentDescription = "Band Image",
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.CenterHorizontally) // Ensure the image is centered
            )
            Spacer(modifier = Modifier.height(8.dp)) // Add space between image and text
            Text(
                text = band.bandName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally) // Ensure the text is centered
                    .padding(horizontal = 16.dp) // Add horizontal padding for the text
            )
            Spacer(modifier = Modifier.height(8.dp)) // Add space between text and button
            Button(
                onClick = {
                    navController.navigate("other_band_screen/${band.bandId}")
                },
                modifier = Modifier.align(Alignment.CenterHorizontally) // Ensure the button is centered
            ) {
                Text("Explore")
            }
        }
    }
}
