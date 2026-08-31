package com.copy9029.bangumimanagerreformed.ui.profile.color_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.copy9029.bangumimanagerreformed.data.GlobalSettings
import com.copy9029.bangumimanagerreformed.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ColorSettingsUiState(
    val default01ColorLong: Long = SettingsRepository.DEFAULT_01_COLOR_LONG,
    val default04ColorLong: Long = SettingsRepository.DEFAULT_04_COLOR_LONG,
    val default07ColorLong: Long = SettingsRepository.DEFAULT_07_COLOR_LONG,
    val default10ColorLong: Long = SettingsRepository.DEFAULT_10_COLOR_LONG,
    val colorPickerMonth: Int? = null,
    val colorPickerInitialColorLong: Long? = null,
)

data class ColorSettingsToastMessage(
    val month: Int,
    val argbHex: String,
)

private enum class DefaultColorTarget(val month: Int) {
    JANUARY(1),
    APRIL(4),
    JULY(7),
    OCTOBER(10),
}

@HiltViewModel
class ColorSettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val defaultColorTarget = MutableStateFlow<DefaultColorTarget?>(null)
    private val _toastMessages = MutableSharedFlow<ColorSettingsToastMessage>(
        extraBufferCapacity = 1,
    )
    val toastMessages: SharedFlow<ColorSettingsToastMessage> = _toastMessages

    val uiState: StateFlow<ColorSettingsUiState> = combine(
        settingsRepository.globalSettings,
        defaultColorTarget,
    ) { settings, colorTarget ->
        settings.toColorSettingsUiState(colorTarget)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ColorSettingsUiState(),
    )

    fun onDefault01ColorClick() {
        defaultColorTarget.value = DefaultColorTarget.JANUARY
    }

    fun onDefault04ColorClick() {
        defaultColorTarget.value = DefaultColorTarget.APRIL
    }

    fun onDefault07ColorClick() {
        defaultColorTarget.value = DefaultColorTarget.JULY
    }

    fun onDefault10ColorClick() {
        defaultColorTarget.value = DefaultColorTarget.OCTOBER
    }

    fun onDefaultColorDialogDismiss() {
        defaultColorTarget.value = null
    }

    fun onDefaultColorSelected(value: Long) {
        val target = defaultColorTarget.value ?: return
        defaultColorTarget.value = null

        viewModelScope.launch {
            when (target) {
                DefaultColorTarget.JANUARY -> settingsRepository.setDefault01ColorLong(value)
                DefaultColorTarget.APRIL -> settingsRepository.setDefault04ColorLong(value)
                DefaultColorTarget.JULY -> settingsRepository.setDefault07ColorLong(value)
                DefaultColorTarget.OCTOBER -> settingsRepository.setDefault10ColorLong(value)
            }
            _toastMessages.emit(
                ColorSettingsToastMessage(
                    month = target.month,
                    argbHex = value.toArgbHex(),
                ),
            )
        }
    }
}

private fun GlobalSettings.toColorSettingsUiState(
    defaultColorTarget: DefaultColorTarget?,
): ColorSettingsUiState {
    return ColorSettingsUiState(
        default01ColorLong = default01ColorLong,
        default04ColorLong = default04ColorLong,
        default07ColorLong = default07ColorLong,
        default10ColorLong = default10ColorLong,
        colorPickerMonth = defaultColorTarget?.month,
        colorPickerInitialColorLong = when (defaultColorTarget) {
            DefaultColorTarget.JANUARY -> default01ColorLong
            DefaultColorTarget.APRIL -> default04ColorLong
            DefaultColorTarget.JULY -> default07ColorLong
            DefaultColorTarget.OCTOBER -> default10ColorLong
            null -> null
        },
    )
}

private fun Long.toArgbHex(): String =
    toString(16).takeLast(8).padStart(8, '0').uppercase()
