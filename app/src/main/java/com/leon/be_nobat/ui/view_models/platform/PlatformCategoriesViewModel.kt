package com.leon.be_nobat.ui.view_models.platform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leon.be_nobat.data.remote.ApiPage
import com.leon.be_nobat.domain.model.ManagementCategory
import com.leon.be_nobat.domain.usecase.PlatformCreateCategoryUseCase
import com.leon.be_nobat.domain.usecase.PlatformDeleteCategoryUseCase
import com.leon.be_nobat.domain.usecase.PlatformListCategoriesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlatformCategoriesUiState(
    val loading: Boolean = false,
    val items: List<ManagementCategory> = emptyList(),
    val total: Int = 0,
    val error: String? = null,
    val message: String? = null,
)

class PlatformCategoriesViewModel(
    private val list: PlatformListCategoriesUseCase,
    private val create: PlatformCreateCategoryUseCase,
    private val deleteUseCase: PlatformDeleteCategoryUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(PlatformCategoriesUiState(loading = true))
    val state: StateFlow<PlatformCategoriesUiState> = _state.asStateFlow()

    init { load() }

    fun load() = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null)
        list()
            .onSuccess { page: ApiPage<ManagementCategory> ->
                _state.value = _state.value.copy(loading = false, items = page.items, total = page.total)
            }
            .onFailure { e -> _state.value = _state.value.copy(loading = false, error = e.message) }
    }

    fun delete(id: String) = viewModelScope.launch {
        deleteUseCase(id).onSuccess {
            _state.value = _state.value.copy(items = _state.value.items.filterNot { it.id == id }, message = "deleted")
        }.onFailure { e -> _state.value = _state.value.copy(error = e.message) }
    }
}
