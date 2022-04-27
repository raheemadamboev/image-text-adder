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

    suspend fun insertImage(uri: Uri, bitmap: Bitmap, format: Bitmap.CompressFormat) {
        file.insertImage(uri, bitmap, format)
    }

    suspend fun insertImage(bitmap: Bitmap, format: Bitmap.CompressFormat) {
        file.insertImage(bitmap, format)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Update
    ///////////////////////////////////////////////////////////////////////////

    suspend fun updateImage(uri: Uri, bitmap: Bitmap, format: Bitmap.CompressFormat): IntentSender? {
        return file.updateImage(uri, bitmap, format)
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