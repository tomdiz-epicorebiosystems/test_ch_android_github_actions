package com.epicorebiosystems.rehydrate.appInjectionSupport

data class EpicoreCHViewState(
    val isUARTModuleRunning: Boolean = false,
    val refreshToggle: Boolean = false
) {

    fun copyWithRefresh(): EpicoreCHViewState {
        return copy(refreshToggle = !refreshToggle)
    }
}
