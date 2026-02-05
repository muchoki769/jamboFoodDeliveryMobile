package com.jambofooddelivery.domain

import com.jambofooddelivery.models.User
import com.jambofooddelivery.repositories.AuthRepository
import com.jambofooddelivery.utils.Result

class LoginUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        return authRepository.login(email, password)
    }
}

class RegisterUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String
    ): Result<User> {
        return authRepository.register(email, password, firstName, lastName, phone)
    }
}