package xyz.teamgravity.imagetextadder.presentation.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import dagger.hilt.android.AndroidEntryPoint
import xyz.teamgravity.imagetextadder.R
import xyz.teamgravity.imagetextadder.core.extension.gone
import xyz.teamgravity.imagetextadder.core.extension.invisible
import xyz.teamgravity.imagetextadder.core.extension.visible
import xyz.teamgravity.imagetextadder.core.util.Helper
import xyz.teamgravity.imagetextadder.databinding.FragmentImageBinding
import xyz.teamgravity.imagetextadder.presentation.viewmodel.ImageViewModel
import java.text.SimpleDateFormat
import javax.inject.Inject

@AndroidEntryPoint
class Image : Fragment() {

    private var _binding: FragmentImageBinding? = null
    private val binding get() = _binding!!

    private val viewmodel by viewModels<ImageViewModel>()
    private val args by navArgs<ImageArgs>()

    @Inject
    lateinit var formatter: SimpleDateFormat

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateUI()
        button()
    }

    private fun updateUI() {
        toolbar()
        updateImage()
    }

    private fun button() {
        onEdit()
        onInfo()
        onDelete()
    }

    private fun toolbar() {
        val appCompatActivity = activity as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.toolbar)
        appCompatActivity.supportActionBar?.title = ""
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        appCompatActivity.supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun updateImage() {
        binding.apply {
            Glide.with(requireContext())
                .load(args.image.uri)
                .signature(ObjectKey(args.image.date))
                .into(imageI)
        }
    }

    private fun showHideEditor() {
        if (binding.titleField.visibility == View.VISIBLE) {
            hideEditor()
        } else {
            hideInfo()
            showEditor()
        }
    }

    private fun showEditor() {
        binding.apply {
            titleField.visible()
            titleField.requestFocus()
            showKeyboard()

            subtitleField.visible()
        }
    }

    private fun hideEditor() {
        binding.apply {
            titleField.invisible()
            subtitleField.invisible()
            hideKeyboard()
        }
    }

    private fun showHideInfo() {
        binding.apply {
            if (imageInfoContainer.visibility == View.VISIBLE) {
                hideInfo()
            } else {
                hideEditor()
                updateInfo()
                showInfo()
            }
        }
    }

    private fun updateInfo() {
        binding.apply {
            dateT.text = formatter.format(args.image.date)
            pathT.text = args.image.path
            sizeT.text = getString(R.string.your_kb, Helper.kbFromBytes(args.image.size))

            if (args.image.width == null || args.image.height == null) {
                dimensionsT.gone()
            } else {
                dimensionsT.text = getString(R.string.your_dimension, args.image.width, args.image.height)
                dimensionsT.visible()
            }
        }
    }

    private fun showInfo() {
        binding.imageInfoContainer.visible()
    }

    private fun hideInfo() {
        binding.imageInfoContainer.invisible()
    }

    private fun showKeyboard() {
        val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.titleField, InputMethodManager.SHOW_FORCED)
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    private fun onEdit() {
        binding.editI.setOnClickListener {
            showHideEditor()
        }
    }

    private fun onInfo() {
        binding.infoI.setOnClickListener {
            showHideInfo()
        }
    }

    private fun onDelete() {
        binding.deleteI.setOnClickListener {

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}