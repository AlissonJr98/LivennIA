package com.livennia.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NormalizarServidorTest {
    @Test
    fun normalizarServidor_aceitaSemProtocolo() {
        assertEquals("http://192.168.0.15:8000/", normalizarServidor("192.168.0.15:8000"))
    }

    @Test
    fun normalizarServidor_aceitaComHttp() {
        assertEquals("http://192.168.0.15:8000/", normalizarServidor("http://192.168.0.15:8000"))
    }

    @Test
    fun normalizarServidor_aceitaComHttps() {
        assertEquals("https://api.exemplo.com/", normalizarServidor("https://api.exemplo.com"))
    }

    @Test
    fun normalizarServidor_adicionaBarraFinal() {
        assertEquals("http://localhost:8000/", normalizarServidor("http://localhost:8000"))
    }

    @Test
    fun normalizarServidor_rejeitaVazio() {
        assertNull(normalizarServidor(""))
        assertNull(normalizarServidor("   "))
    }

    @Test
    fun normalizarServidor_rejeitaInvalido() {
        assertNull(normalizarServidor("not a url :::"))
    }
}