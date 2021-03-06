package xyz.teamgravity.imagetextadder.injection

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.FragmentScoped
import xyz.teamgravity.imagetextadder.presentation.adapter.ImageAdapter
import java.text.SimpleDateFormat
import java.util.*

@Module
@InstallIn(FragmentComponent::class)
object FragmentModule {

    @Provides
    @FragmentScoped
    fun provideImageAdapter(): ImageAdapter = ImageAdapter()

    @Provides
    @FragmentScoped
    fun provideDateFormatter(): SimpleDateFormat = SimpleDateFormat("MMM d, yyyy  •  HH:mm", Locale.getDefault())
}