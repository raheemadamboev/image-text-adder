package xyz.teamgravity.imagetextadder.presentation.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import xyz.teamgravity.imagetextadder.R
import xyz.teamgravity.imagetextadder.databinding.ActivityMainBinding

class Main : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.fragment_container).navigateUp()
    }
}