package com.livennia.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidacoesTest {
    @Test
    fun emailValido_aceitaEmailCorreto() {
        assertTrue(emailValido("teste@exemplo.com"))
        assertTrue(emailValido("a.b+c@dominio.co.uk"))
    }

    @Test
    fun emailValido_rejeitaInvalidos() {
        assertFalse(emailValido(""))
        assertFalse(emailValido("sem-arroba"))
        assertFalse(emailValido("@dominio.com"))
        assertFalse(emailValido("a@"))
    }

    @Test
    fun normalizarServidor_aceitaFormatos() {
        org.junit.Assert.assertEquals("http://192.168.0.15:8000/", normalizarServidor("192.168.0.15:8000"))
        org.junit.Assert.assertEquals("http://192.168.0.15:8000/", normalizarServidor("http://192.168.0.15:8000"))
        org.junit.Assert.assertEquals("https://api.exemplo.com/", normalizarServidor("https://api.exemplo.com"))
    }

    @Test
    fun normalizarServidor_rejeitaVazio() {
        org.junit.Assert.assertNull(normalizarServidor(""))
        org.junit.Assert.assertNull(normalizarServidor("   "))
    }
}