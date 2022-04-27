package xyz.teamgravity.imagetextadder.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import xyz.teamgravity.imagetextadder.data.model.ImageModel
import xyz.teamgravity.imagetextadder.databinding.CardImageBinding

class ImageAdapter : ListAdapter<ImageModel, ImageAdapter.ImageViewHolder>(ImageDiff) {

    var listener: ImageListener? = null

    inner class ImageViewHolder(private val binding: CardImageBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener?.onImageClick(getItem(position))
                }
            }
        }

        fun bind(model: ImageModel) {
            binding.apply {
                Glide.with(root.context)
                    .load(model.uri)
                    .signature(ObjectKey(model.date))
                    .into(root)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(CardImageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private object ImageDiff : DiffUtil.ItemCallback<ImageModel>() {
        override fun areItemsTheSame(oldItem: ImageModel, newItem: ImageModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ImageModel, newItem: ImageModel): Boolean {
            return oldItem == newItem
        }

    }

    interface ImageListener {
        fun onImageClick(image: ImageModel)
    }
}