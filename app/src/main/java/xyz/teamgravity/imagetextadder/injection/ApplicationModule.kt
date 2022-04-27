package xyz.teamgravity.imagetextadder.injection

import android.app.Application
import android.content.ContentResolver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import xyz.teamgravity.imagetextadder.data.local.ImageFile
import xyz.teamgravity.imagetextadder.data.repository.ImageRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Provides
    @Singleton
    fun provideContentResolver(application: Application): ContentResolver = application.contentResolver

    @Provides
    @Singleton
    fun provideImageFile(contentResolver: ContentResolver): ImageFile = ImageFile(contentResolver)

    @Provides
    @Singleton
    fun provideImageRepository(imageFile: ImageFile): ImageRepository = ImageRepository(imageFile)
}