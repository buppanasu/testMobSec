import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SearchScreenFunction(){
    //SearchScreen()
    val viewModel = viewModel<SearchScreen>()
    val searchText by viewModel.searchText.collectAsState()
    val persons by viewModel.persons.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)){
        TextField(
            value = searchText,
            onValueChange = viewModel::onSearchTextChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = "Search") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(modifier = Modifier
            .fillMaxWidth()
            .weight(1f)){
            items(persons){
                    person ->
                //This is the things that appear, so what we want to do now is
                //once we click a person, it will go to the detailed profile of the person
                Text(
                    text = "${person.firstName} ${person.lastName}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
            }
        }

    }
}
class SearchScreen: ViewModel(){




    //This will be an empty string by default
    private val _searchText = MutableStateFlow("")
    //public exposed version of searchtext
    val searchText = _searchText.asStateFlow()

    //used to show progress bar or hide it
    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    //initially this is an empty list
    private val _persons = MutableStateFlow(allPersons)

    //exposed version, we want to trigger our search when the search text changes
    //so we need to make this dependent on our searchtextstateflow and we want to call combine
    val persons = searchText.combine(_persons){
        //this block will be called if either search text or person state changes
            text, persons ->
        if(text.isBlank()){
            persons
        }
        else{
            persons.filter{
                it.doesMatchSearchQuery(text)
            }
        }
    }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            _persons.value
        )
    fun onSearchTextChange(text: String){
        _searchText.value = text
    }
}

data class Person(
    val firstName: String,
    val lastName: String
){

    fun doesMatchSearchQuery(query: String): Boolean{
        val matchingCombinations = listOf(
            "$firstName$lastName",
            "$firstName $lastName",
            "${firstName.first()} ${lastName.first()}",
        )

        //to return whether we want to include that person or not
        //we want to find whether theres any combination that contains our search query
        return matchingCombinations.any{
            it.contains(query, ignoreCase = true)
        }
    }
}

//predefined persons
private val allPersons = listOf(
    Person(
        firstName = "Jeff",
        lastName = "Bezos"
    ),
    Person(
        firstName = "Michael",
        lastName = "Jackson"
    ),
    Person(
        firstName = "Robert",
        lastName = "Kirkman"
    ),
    Person(
        firstName = "Bob",
        lastName = "Odenkirk"
    ),
)

//what do we need before we can implement actual search logic?
//function that defines if a certain person matches the search query. we want
//define the function in Person class as its its business logic.

