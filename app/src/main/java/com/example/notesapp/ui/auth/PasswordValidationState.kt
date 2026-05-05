package com.example.notesapp.ui.auth

data class PasswordValidationState(
    val hasMinLength: Boolean = false,
    val hasLowerCase: Boolean = false,
    val hasUpperCase: Boolean = false,
    val hasNumber: Boolean = false,
    val hasSpecialChar: Boolean = false
) {
    val fulfilledCriteriaCount: Int
        get() = listOf(hasLowerCase, hasUpperCase, hasNumber, hasSpecialChar).count { it }

    val hasThreeOfFour: Boolean
        get() = fulfilledCriteriaCount >= 3

    val isValid: Boolean
        get() = hasMinLength && hasThreeOfFour
}
