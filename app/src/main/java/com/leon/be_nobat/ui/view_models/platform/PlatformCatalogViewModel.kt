package com.leon.be_nobat.ui.view_models.platform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leon.be_nobat.data.remote.ApiPage
import com.leon.be_nobat.domain.model.ManagementCatalog
import com.leon.be_nobat.domain.usecase.PlatformCreateCatalogUseCase
import com.leon.be_nobat.domain.usecase.PlatformDeleteCatalogUseCase
import com.leon.be_nobat.domain.usecase.PlatformListCatalogUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlatformCatalogUiState(
    val loading: Boolean = false,
    val items: List<ManagementCatalog> = emptyList(),
    val total: Int = 0,
    val error: String? = null,
    val message: String? = null,
)

class PlatformCatalogViewModel(
    private val list: PlatformListCatalogUseCase,
    private val create: PlatformCreateCatalogUseCase,
    private val deleteUseCase: PlatformDeleteCatalogUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(PlatformCatalogUiState(loading = true))
    val state: StateFlow<PlatformCatalogUiState> = _state.asStateFlow()

    init { load() }

    fun load() = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null)
        list()
            .onSuccess { page: ApiPage<ManagementCatalog> ->
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
