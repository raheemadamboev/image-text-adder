package xyz.teamgravity.imagetextadder.data.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ImageModel(
    val id: Long,
    val uri: Uri,
    val path: String,
    val name: String,
    val size: String,
    val width: String?,
    val height: String?,
    val date: String
) : Parcelable
