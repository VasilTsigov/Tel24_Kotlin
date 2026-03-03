package bg.iag.tel24.data.model

import com.google.gson.annotations.SerializedName

// ─── API responses ───────────────────────────────────────────────────────────

data class ApiResponse(
    @SerializedName("items") val root: TreeNode? = null
)

data class SearchApiResponse(
    @SerializedName("data") val data: SearchData? = null
)

data class SearchData(
    @SerializedName("items") val items: List<TreeNode>? = null
)

// ─── Tree node (department or employee) ─────────────────────────────────────

data class TreeNode(
    @SerializedName("id")       val id:       Int?    = null,
    @SerializedName("text")     val text:     String? = null,
    @SerializedName("leaf")     val leaf:     Boolean = false,
    @SerializedName("gsm")      val gsm:      String? = null,
    @SerializedName("email")    val email:    String? = null,
    @SerializedName("pod")      val pod:      String? = null,
    @SerializedName("pict")     val pict:     String? = null,
    @SerializedName("glavpod")  val glavpod:  Int?    = null,
    @SerializedName("dlag")     val dlag:     String? = null,
    @SerializedName("items")    val children: List<TreeNode>? = null
) {
    val imageUrl: String?
        get() = if (!pict.isNullOrBlank() && glavpod != null)
            "https://vasil.iag.bg/upload/$glavpod/$pict"
        else null

    val isEmployee: Boolean get() = leaf || children.isNullOrEmpty()
}
