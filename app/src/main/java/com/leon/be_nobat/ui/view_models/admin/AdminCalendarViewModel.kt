package com.leon.be_nobat.ui.view_models.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leon.be_nobat.data.remote.ApiPage
import com.leon.be_nobat.domain.model.ManagementAppointment
import com.leon.be_nobat.domain.usecase.AdminListAppointmentsUseCase
import com.leon.be_nobat.domain.usecase.AdminSetAppointmentStatusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminCalendarUiState(
    val loading: Boolean = false,
    val items: List<ManagementAppointment> = emptyList(),
    val total: Int = 0,
    val statusFilter: String? = null,
    val dateFilter: String = "",
    val searchQuery: String = "",
    val error: String? = null,
)

class AdminCalendarViewModel(
    private val listAppointments: AdminListAppointmentsUseCase,
    private val setStatus: AdminSetAppointmentStatusUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(AdminCalendarUiState(loading = true))
    val state: StateFlow<AdminCalendarUiState> = _state.asStateFlow()

    init { load() }

    fun load() = viewModelScope.launch {
        val s = _state.value
        _state.value = s.copy(loading = true, error = null)
        listAppointments(
            date = s.dateFilter.ifBlank { null },
            status = s.statusFilter,
        ).onSuccess { page: ApiPage<ManagementAppointment> ->
            _state.value = _state.value.copy(loading = false, items = page.items, total = page.total, error = null)
        }.onFailure { e ->
            _state.value = _state.value.copy(loading = false, error = e.message)
        }
    }

    fun setStatusFilter(status: String?) { _state.value = _state.value.copy(statusFilter = status); load() }
    fun setDateFilter(date: String) { _state.value = _state.value.copy(dateFilter = date); load() }
    fun setSearchQuery(q: String) { _state.value = _state.value.copy(searchQuery = q) }

    fun changeStatus(id: String, newStatus: String) = viewModelScope.launch {
        setStatus(id, newStatus).onSuccess { updated ->
            val items = _state.value.items.map { if (it.id == id) updated else it }
            _state.value = _state.value.copy(items = items)
        }.onFailure { e -> _state.value = _state.value.copy(error = e.message) }
    }
}
