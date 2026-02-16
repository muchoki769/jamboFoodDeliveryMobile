package com.jambofooddelivery.Ui.ViewModels.auth

import com.jambofooddelivery.repositories.AuthRepository
import com.jambofooddelivery.Ui.ViewModels.BaseViewModel
import com.jambofooddelivery.utils.Result
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class ForgotPasswordState(
    val isLoading: Boolean = false,
    val email: String = "",
    val successMessage: String? = null,
    val errorMessage: String? = null
)

sealed class ForgotPasswordEvent {
    object ResetEmailSent : ForgotPasswordEvent()
    data class ShowError(val message: String) : ForgotPasswordEvent()
}

class ForgotPasswordViewModel : BaseViewModel<ForgotPasswordState, ForgotPasswordEvent>(ForgotPasswordState()), KoinComponent {

    private val authRepository: AuthRepository by inject()

    fun onEmailChanged(email: String) {
        setState { it.copy(email = email, errorMessage = null) }
    }

    fun submit() {
        launch {
            setState { it.copy(isLoading = true) }
            when (val result = authRepository.forgotPassword(state.value.email)) {
                is Result.Success -> {
                    setState { it.copy(isLoading = false, successMessage = "Password reset link sent to your email.") }
                    emitEvent(ForgotPasswordEvent.ResetEmailSent)
                }
                is Result.Error -> {
                    setState { it.copy(isLoading = false, errorMessage = result.message) }
                    emitEvent(ForgotPasswordEvent.ShowError(result.message))
                }
                else -> {}
            }
        }
    }
}
