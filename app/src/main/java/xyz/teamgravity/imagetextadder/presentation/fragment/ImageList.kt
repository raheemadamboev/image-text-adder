package xyz.teamgravity.imagetextadder.presentation.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import xyz.teamgravity.imagetextadder.R
import xyz.teamgravity.imagetextadder.core.extension.navigateSafely
import xyz.teamgravity.imagetextadder.data.model.ImageModel
import xyz.teamgravity.imagetextadder.databinding.FragmentImageListBinding
import xyz.teamgravity.imagetextadder.presentation.adapter.ImageAdapter
import xyz.teamgravity.imagetextadder.presentation.viewmodel.ImageListViewModel
import javax.inject.Inject

@AndroidEntryPoint
class ImageList : Fragment(), ImageAdapter.ImageListener {

    private var _binding: FragmentImageListBinding? = null
    private val binding get() = _binding!!

    private val viewmodel by viewModels<ImageListViewModel>()

    @Inject
    lateinit var adapter: ImageAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentImageListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkPermission()
        updateUI()
        observe()
    }

    private fun updateUI() {
        toolbar()
        recyclerview()
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewmodel.state.collectLatest { images ->
                adapter.submitList(images)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewmodel.onGetImages()
            }
        }
    }

    private fun toolbar() {
        val appCompatActivity = activity as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.toolbar)
        appCompatActivity.setTitle(R.string.app_name)
    }

    private fun recyclerview() {
        binding.apply {
            recyclerview.setHasFixedSize(true)
            recyclerview.adapter = adapter
            adapter.listener = this@ImageList
        }
    }

    private fun readExternalStorageAllowed(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkPermission() {
        if (!readExternalStorageAllowed()) requestPermission()
    }

    private fun requestPermission() {
        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) viewmodel.onGetImages()
    }

    override fun onImageClick(image: ImageModel) {
        findNavController().navigateSafely(ImageListDirections.actionImageListToImage(image = image))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}