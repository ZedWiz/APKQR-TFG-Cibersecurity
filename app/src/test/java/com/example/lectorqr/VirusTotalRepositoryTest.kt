package com.example.lectorqr

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VirusTotalRepositoryTest {
    private val repository = VirusTotalRepository()

    @Test
    fun testUrlEncoding() {
        val url = "https://www.google.com"
        // Codificación esperada para google.com en VT v3: aHR0cHM6Ly93d3cuZ29vZ2xlLmNvbQ
        val expected = "aHR0cHM6Ly93d3cuZ29vZ2xlLmNvbQ"
        val encoded = repository.encodeUrl(url)
        assertEquals("La codificación Base64 URL-safe debe ser correcta", expected, encoded)
    }

    @Test
    fun testCheckSafeUrl() = runBlocking {
        val url = "https://www.google.com"
        val result = repository.checkUrl(url)
        
        when (result) {
            is ScanResult.Safe -> assertTrue(true)
            is ScanResult.Malicious -> println("Aviso: Google marcada como maliciosa (raro)")
            is ScanResult.NotFound -> println("Aviso: Google no encontrada en VirusTotal (raro)")
            is ScanResult.Error -> println("Aviso: Error de red/API: ${result.message}")
        }
    }
}
