package xyz.teamgravity.imagetextadder.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import xyz.teamgravity.imagetextadder.data.model.ImageModel
import xyz.teamgravity.imagetextadder.data.repository.ImageRepository
import javax.inject.Inject

@HiltViewModel
class ImageListViewModel @Inject constructor(
    private val repository: ImageRepository
) : ViewModel() {

    private val _state = MutableStateFlow(emptyList<ImageModel>())
    val state: StateFlow<List<ImageModel>> = _state.asStateFlow()

    fun onGetImages() {
        viewModelScope.launch {
            _state.emit(repository.getImages().first())
        }
    }
}