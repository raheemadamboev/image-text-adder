package xyz.teamgravity.imagetextadder.data.repository

import android.content.IntentSender
import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import xyz.teamgravity.imagetextadder.data.local.ImageFile
import xyz.teamgravity.imagetextadder.data.model.ImageModel

class ImageRepository(
    private val file: ImageFile
) {

    ///////////////////////////////////////////////////////////////////////////
    // Insert
    ///////////////////////////////////////////////////////////////////////////

    suspend fun insertImage(image: ImageModel, uri: Uri, bitmap: Bitmap) {
        file.insertImage(image, uri, bitmap)
    }

    suspend fun insertImage(image: ImageModel, bitmap: Bitmap) {
        file.insertImage(image, bitmap)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Update
    ///////////////////////////////////////////////////////////////////////////

    suspend fun updateImage(image: ImageModel, uri: Uri, bitmap: Bitmap): IntentSender? {
        return file.updateImage(image, uri, bitmap)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Delete
    ///////////////////////////////////////////////////////////////////////////

    suspend fun deleteImage(image: ImageModel): IntentSender? {
        return file.deleteImage(image)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Get
    ///////////////////////////////////////////////////////////////////////////

    fun getImages(): Flow<List<ImageModel>> {
        return file.getImages()
    }
}