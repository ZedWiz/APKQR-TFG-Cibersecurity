package com.example.lectorqr

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface VirusTotalApi {
    @GET("urls/{id}")
    suspend fun getUrlReport(
        @Header("x-apikey") apiKey: String,
        @Path("id") urlId: String
    ): VirusTotalResponse

    @POST("urls")
    @FormUrlEncoded
    suspend fun postUrl(
        @Header("x-apikey") apiKey: String,
        @Field("url") url: String
    ): VirusTotalPostResponse
}

data class VirusTotalResponse(
    val data: UrlData
)

data class UrlData(
    val attributes: UrlAttributes
)

data class UrlAttributes(
    val last_analysis_stats: AnalysisStats
)

data class AnalysisStats(
    val malicious: Int,
    val suspicious: Int,
    val harmless: Int,
    val undetected: Int
)

data class VirusTotalPostResponse(
    val data: PostData
)

data class PostData(
    val id: String,
    val type: String
)
