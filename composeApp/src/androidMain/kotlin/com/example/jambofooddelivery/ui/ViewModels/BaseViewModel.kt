package com.example.jambofooddelivery.ui.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

abstract class BaseViewModel<State : Any, Event : Any>(initialState: State) : ViewModel() {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _events = MutableStateFlow<Event?>(null)
    val events = _events.asStateFlow()

    protected fun setState(update: (State) -> State) {
        _state.value = update(_state.value)
    }

    protected fun emitEvent(event: Event) {
        _events.value = event
    }

    fun clearEvent() {
        _events.value = null
    }

    protected fun launch(block: suspend CoroutineScope.() -> Unit) = viewModelScope.launch(block = block)
}

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
    object Empty : UiState<Nothing>()
}


//package com.example.jambofooddelivery.ui.ViewModels
//
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.launch
//
//abstract class BaseViewModel<State : Any, Event : Any>(initialState: State) {
//    private val _state = MutableStateFlow(initialState)
//    val state: StateFlow<State> = _state.asStateFlow()
//
//    private val _events = MutableStateFlow<Event?>(null)
//    val events = _events.asStateFlow()
//
//    private val viewModelScope = CoroutineScope(Dispatchers.Main)
//    private var jobs: MutableSet<Job> = mutableSetOf()
//
//    protected fun setState(update: (State) -> State) {
//        _state.value = update(_state.value)
//    }
//
//    protected fun emitEvent(event: Event) {
//        _events.value = event
//    }
//
//    protected fun clearEvent() {
//        _events.value = null
//    }
//
//    protected fun launch(block: suspend CoroutineScope.() -> Unit): Job {
//        val job = viewModelScope.launch(block = block)
//        jobs.add(job)
//        job.invokeOnCompletion { jobs.remove(job) }
//        return job
//    }
//
//    open fun clear() {
//        jobs.forEach { it.cancel() }
//        jobs.clear()
//    }
//
//}
//
//sealed class UiState<out T> {
//    object Loading : UiState<Nothing>()
//    data class Success<T>(val data: T) : UiState<T>()
//    data class Error(val message: String) : UiState<Nothing>()
//    object Empty : UiState<Nothing>()
//}