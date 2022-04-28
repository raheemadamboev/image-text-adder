package xyz.teamgravity.imagetextadder.presentation.fragment

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.text.TextPaint
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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
        observe()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_image, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save_copy_menu -> {
                saveImage()
                true
            }

            R.id.save_menu -> {
                updateImage()
                true
            }

            R.id.save_as_menu -> {

                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateUI() {
        toolbar()
        displayImage()
    }

    private fun button() {
        onEdit()
        onInfo()
        onDelete()
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewmodel.event.collectLatest { event ->
                event(event)
            }
        }
    }

    private fun toolbar() {
        setHasOptionsMenu(true)
        val appCompatActivity = activity as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.toolbar)
        appCompatActivity.supportActionBar?.title = ""
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        appCompatActivity.supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun displayImage() {
        binding.apply {
            Glide.with(requireContext())
                .load(args.image.uri)
                .signature(ObjectKey(args.image.date))
                .into(imageI)
        }
    }

    private fun event(event: ImageViewModel.ImageEvent) {
        when (event) {
            ImageViewModel.ImageEvent.ImageSaved,
            ImageViewModel.ImageEvent.ImageUpdated,
            ImageViewModel.ImageEvent.ImageDeleted -> handleImageChange()

            is ImageViewModel.ImageEvent.ScopedPermissionNeeded -> {
                when (event.request) {
                    ImageViewModel.ImageRequest.UPDATE -> updateScopedPermissionLauncher.launch(
                        IntentSenderRequest.Builder(event.sender).build()
                    )
                    ImageViewModel.ImageRequest.DELETE -> deleteScopedPermissionLauncher.launch(
                        IntentSenderRequest.Builder(event.sender).build()
                    )
                }
            }
        }
    }

    private fun handleImageChange() {
        Toast.makeText(requireContext(), getString(R.string.images_changed), Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
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

    private fun saveImage(uri: Uri? = null) {
        hideEditor()
        viewmodel.saveImage(args.image, uri, createBitmap())
    }

    private fun updateImage() {
        hideEditor()
        viewmodel.updateImage(args.image, createBitmap())
    }

    private fun createBitmap(): Bitmap {
        with(binding) {
            val bitmap = getBitmapFromView(imageI)
            addTextToBitmap(bitmap, titleField.text.toString(), subtitleField.text.toString())
            return bitmap
        }
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
        return bitmap
    }

    private fun addTextToBitmap(bitmap: Bitmap, title: String, subtitle: String) {
        with(binding) {
            val canvas = Canvas(bitmap)

            val textPaint = TextPaint(titleField.paint).apply {
                color = Color.WHITE
                textAlign = Paint.Align.CENTER
            }

            val outlinePaint = Paint().apply {
                isAntiAlias = true
                textSize = titleField.textSize
                color = Color.BLACK
                typeface = titleField.typeface
                style = Paint.Style.STROKE
                textAlign = Paint.Align.CENTER
                strokeWidth = 10F
            }

            val xPos = bitmap.width / 2F
            var yPos = titleField.pivotY + titleField.height

            canvas.drawText(title, xPos, yPos, outlinePaint)
            canvas.drawText(title, xPos, yPos, textPaint)

            yPos = imageI.height.toFloat() - subtitleField.height

            canvas.drawText(subtitle, xPos, yPos, outlinePaint)
            canvas.drawText(subtitle, xPos, yPos, textPaint)
        }
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

    private val updateScopedPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) updateImage()
        else Toast.makeText(requireContext(), getString(R.string.permission_fail), Toast.LENGTH_SHORT).show()
    }

    private val deleteScopedPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}