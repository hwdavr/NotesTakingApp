package com.example.notesapp.ui.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordValidationStateTest {

    @Test
    fun `isValid is true when min length and 3 of 4 criteria are met`() {
        val state = PasswordValidationState(
            hasMinLength = true,
            hasLowerCase = true,
            hasUpperCase = true,
            hasNumber = true,
            hasSpecialChar = false
        )
        assertTrue(state.isValid)
        assertEquals(3, state.fulfilledCriteriaCount)
    }

    @Test
    fun `isValid is false when min length is not met`() {
        val state = PasswordValidationState(
            hasMinLength = false,
            hasLowerCase = true,
            hasUpperCase = true,
            hasNumber = true,
            hasSpecialChar = true
        )
        assertFalse(state.isValid)
    }

    @Test
    fun `isValid is false when less than 3 criteria are met`() {
        val state = PasswordValidationState(
            hasMinLength = true,
            hasLowerCase = true,
            hasUpperCase = true,
            hasNumber = false,
            hasSpecialChar = false
        )
        assertFalse(state.isValid)
        assertEquals(2, state.fulfilledCriteriaCount)
    }

    @Test
    fun `all criteria met`() {
        val state = PasswordValidationState(
            hasMinLength = true,
            hasLowerCase = true,
            hasUpperCase = true,
            hasNumber = true,
            hasSpecialChar = true
        )
        assertTrue(state.isValid)
        assertEquals(4, state.fulfilledCriteriaCount)
    }
}
