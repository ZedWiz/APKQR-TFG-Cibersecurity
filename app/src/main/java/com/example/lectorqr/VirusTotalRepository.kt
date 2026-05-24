package com.example.lectorqr

import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

sealed class ScanResult {
    object Safe : ScanResult()
    object Malicious : ScanResult()
    object NotFound : ScanResult()
    data class Error(val message: String) : ScanResult()
}

class VirusTotalRepository {
    private val api: VirusTotalApi = Retrofit.Builder()
        .baseUrl("https://www.virustotal.com/api/v3/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(VirusTotalApi::class.java)

    // API Key de VirusTotal (v3)
    private val apiKey = "300e2b70f98ab1c11c0cde0421979819c4992aa337fee766ae81919f0ebdece2"

    fun encodeUrl(url: String): String {
        return try {
            // Intenta usar java.util.Base64 (disponible en Java 8+ / Android API 26+)
            java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(url.toByteArray())
        } catch (e: Throwable) {
            // Fallback para Android antiguo
            android.util.Base64.encodeToString(url.toByteArray(), android.util.Base64.NO_WRAP or android.util.Base64.URL_SAFE)
                .replace("=", "")
        }
    }

    suspend fun checkUrl(url: String): ScanResult {
        return try {
            val urlId = encodeUrl(url)
            try {
                val response = api.getUrlReport(apiKey, urlId)
                val stats = response.data.attributes.last_analysis_stats
                
                if (stats.malicious > 0 || stats.suspicious > 0) {
                    ScanResult.Malicious
                } else {
                    ScanResult.Safe
                }
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    // Si la URL no existe en VT, intentamos enviarla para análisis futuro
                    try { api.postUrl(apiKey, url) } catch (ignore: Exception) {}
                    ScanResult.NotFound
                } else {
                    ScanResult.Error("Error API: ${e.code()}")
                }
            }
        } catch (e: Exception) {
            ScanResult.Error(e.message ?: "Error desconocido")
        }
    }
}
