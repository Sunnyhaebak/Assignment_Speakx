import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.paginatedlist.adapter.ItemsAdapter
import com.example.paginatedlist.databinding.ActivityMainBinding
import com.example.paginatedlist.viewmodel.ItemsViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: ItemsViewModel by viewModels()
    private val adapter = ItemsAdapter()
    private val scrollThreshold = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            adapter = this@MainActivity.adapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
                    val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                    if (lastVisibleItem >= totalItemCount - scrollThreshold) {
                        viewModel.loadMore("down")
                    }

                    if (firstVisibleItem <= scrollThreshold) {
                        viewModel.loadMore("up")
                    }
                }
            })
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(this) { state ->
            adapter.submitList(state.items)
            
            binding.errorText.apply {
                text = state.error
                visibility = if (state.error != null) View.VISIBLE else View.GONE
            }

            binding.shimmerTop.apply {
                visibility = if (state.isLoadingUp && state.hasMoreUp) View.VISIBLE else View.GONE
                if (state.isLoadingUp) startShimmer() else stopShimmer()
            }

            binding.shimmerBottom.apply {
                visibility = if (state.isLoadingDown && state.hasMoreDown) View.VISIBLE else View.GONE
                if (state.isLoadingDown) startShimmer() else stopShimmer()
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }
} 