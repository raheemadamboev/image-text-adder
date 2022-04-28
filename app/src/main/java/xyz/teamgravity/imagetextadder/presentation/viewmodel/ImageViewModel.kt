package xyz.teamgravity.imagetextadder.presentation.viewmodel

import android.content.IntentSender
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import xyz.teamgravity.imagetextadder.data.model.ImageModel
import xyz.teamgravity.imagetextadder.data.repository.ImageRepository
import javax.inject.Inject

@HiltViewModel
class ImageViewModel @Inject constructor(
    private val repository: ImageRepository,
) : ViewModel() {

    private val _event = Channel<ImageEvent> { }
    val event: Flow<ImageEvent> = _event.receiveAsFlow()

    fun saveImage(image: ImageModel, uri: Uri?, bitmap: Bitmap) {
        viewModelScope.launch {
            if (uri == null) repository.insertImage(image, bitmap) else repository.insertImage(image, uri, bitmap)
            _event.send(ImageEvent.ImageSaved)
        }
    }

    fun updateImage(image: ImageModel, bitmap: Bitmap) {
        viewModelScope.launch {
            val sender = repository.updateImage(image, bitmap)

            if (sender == null) _event.send(ImageEvent.ImageUpdated)
            else _event.send(ImageEvent.ScopedPermissionNeeded(sender = sender, request = ImageRequest.UPDATE))
        }
    }

    fun deleteImage(image: ImageModel) {
        viewModelScope.launch {
            val sender = repository.deleteImage(image)

            if (sender == null) _event.send(ImageEvent.ImageDeleted)
            else _event.send(ImageEvent.ScopedPermissionNeeded(sender = sender, request = ImageRequest.DELETE))
        }
    }

    sealed class ImageEvent {
        object ImageSaved : ImageEvent()
        object ImageUpdated : ImageEvent()
        object ImageDeleted : ImageEvent()
        data class ScopedPermissionNeeded(val sender: IntentSender, val request: ImageRequest) : ImageEvent()
    }

    enum class ImageRequest {
        UPDATE,
        DELETE
    }
}