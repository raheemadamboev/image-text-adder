package xyz.teamgravity.imagetextadder.data.local

import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.ContentUris
import android.content.IntentSender
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.contentValuesOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import xyz.teamgravity.imagetextadder.core.constant.Const
import xyz.teamgravity.imagetextadder.data.model.ImageModel
import java.io.File

class ImageFile(
    private val resolver: ContentResolver
) {

    companion object {
        private const val QUALITY = 100
        private const val DIRECTORY_FOLDER = "ImageTextAdder"
    }


    ///////////////////////////////////////////////////////////////////////////
    // Insert
    ///////////////////////////////////////////////////////////////////////////

    suspend fun insertImage(image: ImageModel, uri: Uri, bitmap: Bitmap) {
        withContext(Dispatchers.IO) {
            val format = getFormat(image.uri)

            resolver.openOutputStream(uri, "w").use { stream ->
                bitmap.compress(format, QUALITY, stream)
            }
        }
    }

    suspend fun insertImage(image: ImageModel, bitmap: Bitmap) {
        withContext(Dispatchers.IO) {
            val format = getFormat(image.uri)
            val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val dir = File(Environment.DIRECTORY_PICTURES, DIRECTORY_FOLDER)
            val date = System.currentTimeMillis()
            val extension = getImageExtension(format)

            val newImage = contentValuesOf(
                MediaStore.Images.Media.DISPLAY_NAME to "$date.$extension",
                MediaStore.Images.Media.MIME_TYPE to "image/$extension",
                MediaStore.Images.Media.DATE_ADDED to date,
                MediaStore.Images.Media.DATE_MODIFIED to date,
                MediaStore.Images.Media.SIZE to bitmap.byteCount,
                MediaStore.Images.Media.WIDTH to bitmap.width,
                MediaStore.Images.Media.HEIGHT to bitmap.height,
                MediaStore.Images.Media.RELATIVE_PATH to "$dir${File.separator}",
                MediaStore.Images.Media.IS_PENDING to 1
            )

            val uri = resolver.insert(collection, newImage)

            resolver.openOutputStream(uri!!, "w").use { stream ->
                bitmap.compress(format, QUALITY, stream)
            }

            newImage.clear()
            newImage.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, newImage, null, null)

            Unit
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Update
    ///////////////////////////////////////////////////////////////////////////

    suspend fun updateImage(image: ImageModel, bitmap: Bitmap): IntentSender? {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                insertImage(image, image.uri, bitmap)
                null
                // successful
            } catch (e: SecurityException) {
                val recover = e as? RecoverableSecurityException ?: throw e
                recover.userAction.actionIntent.intentSender
                // means owner of the file is not this app, so user need to permit it to make changes
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Delete
    ///////////////////////////////////////////////////////////////////////////

    suspend fun deleteImage(image: ImageModel): IntentSender? {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                resolver.delete(
                    image.uri,
                    "${MediaStore.Images.Media._ID} = ?",
                    arrayOf(image.id.toString())
                )
                null
            } catch (e: SecurityException) {
                val recover = e as? RecoverableSecurityException ?: throw e
                recover.userAction.actionIntent.intentSender
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Get
    ///////////////////////////////////////////////////////////////////////////

    fun getImages(): Flow<List<ImageModel>> {
        return flow {
            val images = mutableListOf<ImageModel>()

            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.RELATIVE_PATH,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.DATE_ADDED
            )
            val sort = "${MediaStore.Images.Media.DATE_ADDED} DESC"

            resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sort
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                    val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH))
                    val name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))
                    val size = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE))
                    val width = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH))
                    val height = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT))
                    val date = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED))

                    val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                    if (size == null) continue // discard invalid images that might exist on the device

                    images.add(
                        ImageModel(
                            id = id,
                            uri = uri,
                            path = path,
                            name = name,
                            size = size.toLongOrNull() ?: 0,
                            width = width,
                            height = height,
                            date = (date.toLongOrNull() ?: 0) * 1_000
                        )
                    )
                }
            }

            emit(images)
        }.flowOn(Dispatchers.IO)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Utils
    ///////////////////////////////////////////////////////////////////////////

    private fun getFormat(uri: Uri): Bitmap.CompressFormat {
        val type = resolver.getType(uri)
        return getImageFormat(type)
    }

    private fun getImageFormat(type: String?): Bitmap.CompressFormat {
        return when (type) {
            Bitmap.CompressFormat.PNG.name -> Bitmap.CompressFormat.PNG
            Bitmap.CompressFormat.JPEG.name -> Bitmap.CompressFormat.JPEG
            Bitmap.CompressFormat.WEBP.name -> Bitmap.CompressFormat.WEBP
            else -> Bitmap.CompressFormat.JPEG
        }
    }

    private fun getImageExtension(format: Bitmap.CompressFormat): String {
        return when (format) {
            Bitmap.CompressFormat.PNG -> Const.PNG
            Bitmap.CompressFormat.JPEG -> Const.JPG
            else -> Const.JPG
        }
    }
}