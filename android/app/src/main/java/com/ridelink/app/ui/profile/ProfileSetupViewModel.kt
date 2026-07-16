package com.ridelink.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridelink.app.data.remote.ApiService
import com.ridelink.app.data.remote.UpdateProfileRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileSetupUiState(
    val displayName: String = "",
    val bio: String = "",
    val loading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ProfileSetupViewModel @Inject constructor(private val api: ApiService) : ViewModel() {

    private val _state = MutableStateFlow(ProfileSetupUiState())
    val state: StateFlow<ProfileSetupUiState> = _state.asStateFlow()

    private val _saved = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val saved: SharedFlow<Unit> = _saved

    fun onDisplayNameChange(value: String) {
        _state.value = _state.value.copy(displayName = value, error = null)
    }

    fun onBioChange(value: String) {
        _state.value = _state.value.copy(bio = value)
    }

    fun save() {
        val name = _state.value.displayName.trim()
        if (name.isEmpty()) {
            _state.value = _state.value.copy(error = "Please enter a display name")
            return
        }
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                api.updateProfile(UpdateProfileRequest(name, _state.value.bio.trim().ifEmpty { null }))
                _state.value = _state.value.copy(loading = false)
                _saved.tryEmit(Unit)
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = "Could not save profile")
            }
        }
    }
}
