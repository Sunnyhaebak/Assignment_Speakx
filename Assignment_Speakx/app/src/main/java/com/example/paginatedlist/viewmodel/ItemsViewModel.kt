import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paginatedlist.api.ApiService
import com.example.paginatedlist.model.Item
import kotlinx.coroutines.launch

class ItemsViewModel : ViewModel() {
    private val apiService = ApiService()
    
    private val _state = MutableLiveData<ItemsState>()
    val state: LiveData<ItemsState> = _state

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        _state.value = ItemsState()
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = apiService.fetchItems(id = 1000, direction = "down")
                _state.value = ItemsState(
                    items = response.data,
                    hasMoreDown = response.hasMore
                )
            } catch (e: Exception) {
                _state.value = _state.value?.copy(error = e.message)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMore(direction: String) {
        val currentState = _state.value ?: return
        
        if ((direction == "up" && currentState.isLoadingUp) ||
            (direction == "down" && currentState.isLoadingDown)) {
            return
        }

        val id = if (direction == "up") {
            currentState.items.firstOrNull()?.id ?: return
        } else {
            currentState.items.lastOrNull()?.id ?: return
        }

        viewModelScope.launch {
            try {
                _state.value = currentState.copy(
                    isLoadingUp = direction == "up",
                    isLoadingDown = direction == "down"
                )

                val response = apiService.fetchItems(id = id, direction = direction)
                
                val newItems = if (direction == "up") {
                    response.data + currentState.items
                } else {
                    currentState.items + response.data
                }

                _state.value = currentState.copy(
                    items = newItems,
                    hasMoreUp = if (direction == "up") response.hasMore else currentState.hasMoreUp,
                    hasMoreDown = if (direction == "down") response.hasMore else currentState.hasMoreDown,
                    isLoadingUp = false,
                    isLoadingDown = false
                )
            } catch (e: Exception) {
                _state.value = currentState.copy(
                    isLoadingUp = false,
                    isLoadingDown = false,
                    error = e.message
                )
            }
        }
    }
}

data class ItemsState(
    val items: List<Item> = emptyList(),
    val hasMoreUp: Boolean = true,
    val hasMoreDown: Boolean = true,
    val isLoadingUp: Boolean = false,
    val isLoadingDown: Boolean = false,
    val error: String? = null
) 