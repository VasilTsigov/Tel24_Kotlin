package bg.iag.tel24.data.repository

import android.content.Context
import bg.iag.tel24.data.local.AppDatabase
import bg.iag.tel24.data.local.CachedData
import bg.iag.tel24.data.model.TreeNode
import bg.iag.tel24.data.network.RetrofitClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception, val cached: Any? = null) : Result<Nothing>()
}

class EmployeeRepository(context: Context) {

    private val dao  = AppDatabase.getInstance(context).cacheDao()
    private val api  = RetrofitClient.api
    private val gson = Gson()
    private val listType = object : TypeToken<List<TreeNode>>() {}.type

    // ─── Tree data ───────────────────────────────────────────────────────────

    suspend fun getIag() = fetchTree("iag_data") { api.getIag().root?.let { listOf(it) } }
    suspend fun getRdg() = fetchTree("rdg_data") { api.getRdg().root?.let { listOf(it) } }
    suspend fun getDp()  = fetchTree("dp_data")  { api.getDp().root?.let  { listOf(it) } }

    private suspend fun fetchTree(
        key: String,
        fetch: suspend () -> List<TreeNode>?
    ): Result<List<TreeNode>> = withContext(Dispatchers.IO) {
        try {
            val items = fetch() ?: emptyList()
            if (items.isNotEmpty()) dao.put(CachedData(key, gson.toJson(items)))
            Result.Success(items)
        } catch (e: Exception) {
            val cached = dao.get(key)?.let { gson.fromJson<List<TreeNode>>(it.json, listType) }
            Result.Error(e, cached)
        }
    }

    // ─── Search ──────────────────────────────────────────────────────────────

    suspend fun searchByName(first: String, last: String) =
        fetchSearch("search_${first}_${last}") { api.searchByName(first, last).data?.items }

    suspend fun searchByGSM(gsm: String) =
        fetchSearch("search_gsm_$gsm") { api.searchByGSM(gsm).data?.items }

    private suspend fun fetchSearch(
        key: String,
        fetch: suspend () -> List<TreeNode>?
    ): Result<List<TreeNode>> = withContext(Dispatchers.IO) {
        try {
            val items = fetch() ?: emptyList()
            if (items.isNotEmpty()) dao.put(CachedData(key, gson.toJson(items)))
            Result.Success(items)
        } catch (e: Exception) {
            val cached = dao.get(key)?.let { gson.fromJson<List<TreeNode>>(it.json, listType) }
            Result.Error(e, cached)
        }
    }
}
