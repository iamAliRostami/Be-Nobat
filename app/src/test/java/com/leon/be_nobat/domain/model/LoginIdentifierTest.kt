package com.leon.be_nobat.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginIdentifierTest {
    @Test fun `accepts common valid email formats`() {
        assertTrue(LoginIdentifier.parse("user.name+tag@example.co.uk").isSuccess)
    }

    @Test fun `rejects malformed iranian mobile`() {
        assertTrue(LoginIdentifier.parse("091234567").isFailure)
    }

    @Test fun `accepts mobile without zero prefix`() {
        val identifier = LoginIdentifier.parse("9121234567").getOrThrow()
        assertEquals("+989121234567", identifier.value)
    }
}
