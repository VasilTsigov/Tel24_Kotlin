package bg.iag.tel24.data.network

import bg.iag.tel24.data.model.ApiResponse
import bg.iag.tel24.data.model.SearchApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("tel/v7/iag_empl")
    suspend fun getIag(): ApiResponse

    @GET("tel/v7/rdg_empl")
    suspend fun getRdg(): ApiResponse

    @GET("tel/v7/dp_dgs_empl")
    suspend fun getDp(): ApiResponse

    @GET("all_empl/imeAndFam")
    suspend fun searchByName(
        @Query("strIme") firstName: String,
        @Query("strFam") lastName: String
    ): SearchApiResponse

    @GET("all_empl/byGSM")
    suspend fun searchByGSM(
        @Query("number") gsm: String
    ): SearchApiResponse
}
