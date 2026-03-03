package bg.iag.tel24.ui.tree

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import bg.iag.tel24.data.model.TreeNode
import bg.iag.tel24.data.repository.EmployeeRepository
import bg.iag.tel24.data.repository.Result
import android.util.Log
import kotlinx.coroutines.launch

enum class DataSource { IAG, RDG, DP }

class TreeViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = EmployeeRepository(application)

    private val _items     = MutableLiveData<List<TreeNode>>()
    private val _loading   = MutableLiveData<Boolean>()
    private val _message   = MutableLiveData<String?>()

    val items:   LiveData<List<TreeNode>> = _items
    val loading: LiveData<Boolean>        = _loading
    val message: LiveData<String?>        = _message

    fun load(source: DataSource) {
        viewModelScope.launch {
            _loading.value = true
            _message.value = null

            val result = when (source) {
                DataSource.IAG -> repo.getIag()
                DataSource.RDG -> repo.getRdg()
                DataSource.DP  -> repo.getDp()
            }

            when (result) {
                is Result.Success -> {
                    Log.d("Tel24", "[$source] Success: ${result.data.size} root nodes")
                    result.data.take(3).forEach { Log.d("Tel24", "  node: ${it.text}, children=${it.children?.size}") }
                    _items.value = result.data
                }
                is Result.Error   -> {
                    Log.e("Tel24", "[$source] Error: ${result.exception.message}")
                    @Suppress("UNCHECKED_CAST")
                    val cached = result.cached as? List<TreeNode>
                    if (cached != null) {
                        _items.value   = cached
                        _message.value = "Офлайн – кеширани данни"
                    } else {
                        _message.value = "Грешка при зареждане. Проверете връзката."
                    }
                }
            }

            _loading.value = false
        }
    }
}
