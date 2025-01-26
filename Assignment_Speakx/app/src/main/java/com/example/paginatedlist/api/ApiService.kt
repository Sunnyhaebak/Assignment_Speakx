import com.example.paginatedlist.model.ApiResponse
import com.example.paginatedlist.model.Item
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min

class ApiService {
    companion object {
        private const val MIN_ID = 0
        private const val MAX_ID = 2000
        private const val PAGE_SIZE = 20
    }

    suspend fun fetchItems(id: Int, direction: String): ApiResponse {
        // Simulate network delay
        delay(1500)

        val items = mutableListOf<Item>()
        var hasMore = true

        when (direction) {
            "up" -> {
                val startId = id - 1
                val endId = max(startId - PAGE_SIZE + 1, MIN_ID)
                
                for (i in startId downTo endId) {
                    items.add(Item(id = i, title = "Item $i"))
                }
                
                hasMore = endId > MIN_ID
            }
            "down" -> {
                val startId = id + 1
                val endId = min(startId + PAGE_SIZE - 1, MAX_ID)
                
                for (i in startId..endId) {
                    items.add(Item(id = i, title = "Item $i"))
                }
                
                hasMore = endId < MAX_ID
            }
        }

        return ApiResponse(data = items, hasMore = hasMore)
    }
} 