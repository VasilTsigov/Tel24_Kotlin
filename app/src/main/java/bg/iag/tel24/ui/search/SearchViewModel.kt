package bg.iag.tel24.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import bg.iag.tel24.data.model.TreeNode
import bg.iag.tel24.data.repository.EmployeeRepository
import bg.iag.tel24.data.repository.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = EmployeeRepository(application)

    private val _results = MutableLiveData<List<TreeNode>>()
    private val _loading = MutableLiveData<Boolean>()
    private val _message = MutableLiveData<String?>()

    val results: LiveData<List<TreeNode>> = _results
    val loading: LiveData<Boolean>        = _loading
    val message: LiveData<String?>        = _message

    private var searchJob: Job? = null
    private val nameRegex = Regex("^[a-zA-Za-яА-Я]+\\s[a-zA-Za-яА-Я]+$")
    private val gsmRegex  = Regex("^\\d{5,}$")

    fun search(query: String) {
        searchJob?.cancel()
        val q = query.trim()
        if (q.length < 3) {
            _results.value = emptyList()
            _message.value = null
            return
        }

        searchJob = viewModelScope.launch {
            delay(500) // debounce
            _loading.value = true
            _message.value = null

            val result = when {
                nameRegex.matches(q) -> {
                    val parts = q.split(" ")
                    repo.searchByName(parts[0], parts[1])
                }
                gsmRegex.matches(q)  -> repo.searchByGSM(q)
                else -> {
                    _results.value = emptyList()
                    _loading.value = false
                    return@launch
                }
            }

            when (result) {
                is Result.Success -> _results.value = result.data
                is Result.Error   -> {
                    @Suppress("UNCHECKED_CAST")
                    val cached = result.cached as? List<TreeNode>
                    if (cached != null) {
                        _results.value = cached
                        _message.value = "Офлайн – кеширани резултати"
                    } else {
                        _results.value = emptyList()
                        _message.value = "Неуспешно търсене. Проверете връзката."
                    }
                }
            }

            _loading.value = false
        }
    }
}
