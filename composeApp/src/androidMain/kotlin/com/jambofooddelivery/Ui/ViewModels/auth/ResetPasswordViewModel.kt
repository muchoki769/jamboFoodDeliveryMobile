package com.jambofooddelivery.Ui.ViewModels.auth

import com.jambofooddelivery.repositories.AuthRepository
import com.jambofooddelivery.Ui.ViewModels.BaseViewModel
import com.jambofooddelivery.utils.Result
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class ResetPasswordState(
    val isLoading: Boolean = false,
    val password: String = "",
    val confirmPassword: String = "",
    val token: String = "",
    val isTokenValid: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

sealed class ResetPasswordEvent {
    object PasswordResetSuccess : ResetPasswordEvent()
    data class ShowError(val message: String) : ResetPasswordEvent()
}

class ResetPasswordViewModel : BaseViewModel<ResetPasswordState, ResetPasswordEvent>(ResetPasswordState()), KoinComponent {

    private val authRepository: AuthRepository by inject()

    fun setToken(token: String) {
        setState { it.copy(token = token) }
        verifyToken(token)
    }

    private fun verifyToken(token: String) {
        launch {
            setState { it.copy(isLoading = true) }
            when (val result = authRepository.verifyResetToken(token)) {
                is Result.Success -> {
                    setState { it.copy(isLoading = false, isTokenValid = true) }
                }
                is Result.Error -> {
                    setState { it.copy(isLoading = false, isTokenValid = false, errorMessage = result.message) }
                }
                else -> {}
            }
        }
    }

    fun onPasswordChanged(password: String) {
        setState { it.copy(password = password, errorMessage = null) }
    }

    fun onConfirmPasswordChanged(password: String) {
        setState { it.copy(confirmPassword = password, errorMessage = null) }
    }

    fun submit() {
        if (state.value.password != state.value.confirmPassword) {
            setState { it.copy(errorMessage = "Passwords do not match") }
            return
        }

        launch {
            setState { it.copy(isLoading = true) }
            when (val result = authRepository.resetPassword(state.value.password, state.value.token)) {
                is Result.Success -> {
                    setState { it.copy(isLoading = false, successMessage = "Password reset successfully.") }
                    emitEvent(ResetPasswordEvent.PasswordResetSuccess)
                }
                is Result.Error -> {
                    setState { it.copy(isLoading = false, errorMessage = result.message) }
                    emitEvent(ResetPasswordEvent.ShowError(result.message))
                }
                else -> {}
            }
        }
    }
}
