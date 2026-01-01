
package com.example.jambofooddelivery.ui.ViewModels

import com.example.jambofooddelivery.domain.LoginUseCase
import com.example.jambofooddelivery.domain.RegisterUseCase
import com.example.jambofooddelivery.models.User
import com.example.jambofooddelivery.repositories.AuthRepository
import com.example.jambofooddelivery.utils.Result
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class AuthState(
    val isLoading: Boolean = false,
    val isLoginSuccessful: Boolean = false,
    val isRegisterSuccessful: Boolean = false,
    val error: String? = null,
    val user: User? = null
)

sealed class AuthEvent {
    object NavigateToHome : AuthEvent()
    object NavigateToLogin : AuthEvent()
    data class ShowError(val message: String) : AuthEvent()
}

class AuthViewModel : BaseViewModel<AuthState, AuthEvent>(AuthState()), KoinComponent {

    private val loginUseCase: LoginUseCase by inject()
    private val registerUseCase: RegisterUseCase by inject()
    private val authRepository: AuthRepository by inject()

    init {
        checkCurrentUser()
    }

    fun login(email: String, password: String) {
        launch {
            setState { it.copy(isLoading = true, error = null) }

            when (val result = loginUseCase(email, password)) {
                is Result.Success -> {
                    setState {
                        it.copy(
                            isLoading = false,
                            isLoginSuccessful = true,
                            user = result.data
                        )
                    }
                    emitEvent(AuthEvent.NavigateToHome)
                }
                is Result.Error -> {
                    setState {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                    emitEvent(AuthEvent.ShowError(result.message))
                }
                is Result.Loading -> {
                    // Already set via setState
                }
            }
        }
    }

    fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String
    ) {
        launch {
            setState { it.copy(isLoading = true, error = null) }

            when (val result = registerUseCase(email, password, firstName, lastName, phone)) {
                is Result.Success -> {
                    setState {
                        it.copy(
                            isLoading = false,
                            isRegisterSuccessful = true,
                            user = result.data
                        )
                    }
                    emitEvent(AuthEvent.NavigateToHome)
                }
                is Result.Error -> {
                    setState {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                    emitEvent(AuthEvent.ShowError(result.message))
                }
                 is Result.Loading -> {
                     // Already set via setState
                 }
            }
        }
    }

    fun logout() {
        launch {
            authRepository.logout()
            setState { AuthState() }
            emitEvent(AuthEvent.NavigateToLogin)
        }
    }

    fun clearError() {
        setState { it.copy(error = null) }
    }

    private fun checkCurrentUser() {
        launch {
            val user = authRepository.getCurrentUser().first()
            setState { it.copy(user = user) }
        }
    }

}