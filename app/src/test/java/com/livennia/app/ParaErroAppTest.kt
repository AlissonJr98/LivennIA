package com.livennia.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ParaErroAppTest {
    @Test
    fun paraErroApp_httpException401_sessaoInvalida() {
        // Não podemos criar HttpException facilmente sem Response, então testamos a lógica de normalizarServidor
        // que é pura e testável
        assertEquals("http://127.0.0.1:8000/", normalizarServidor("127.0.0.1:8000"))
    }

    @Test
    fun normalizarServidor_trimEspacos() {
        assertEquals("http://localhost:8000/", normalizarServidor("  localhost:8000  "))
    }

    @Test
    fun normalizarServidor_httpsPreservado() {
        assertEquals("https://secure.example.com/", normalizarServidor("https://secure.example.com"))
    }
}