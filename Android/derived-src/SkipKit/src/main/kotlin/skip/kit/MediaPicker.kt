package skip.kit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue
import skip.lib.*

// Copyright 2025–2026 Skip
// SPDX-License-Identifier: MPL-2.0
import skip.foundation.*
import skip.ui.*

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.startActivity
import skip.model.*

@androidx.annotation.Keep
enum class MediaPickerType: skip.lib.SwiftProjecting {
    camera,
    library;

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {
    }
}


internal fun Context.asActivity(): Activity {
    val matchtarget_0 = this as? Activity
    if (matchtarget_0 != null) {
        val activity = matchtarget_0
        return activity.sref()
    } else {
        val matchtarget_1 = this as? android.content.ContextWrapper
        if (matchtarget_1 != null) {
            val wrapper = matchtarget_1
            return wrapper.baseContext.asActivity()
        } else {
            fatalError("could not extract activity from: ${this}")
        }
    }
}

/// Enables a media picker interface for the camera or photo library can be activated through the `isPresented` binding, and which returns the selected image through the `selectedImageURL` binding.
///
/// On iOS, this camera selector will be presented in a `fullScreenCover` view, whereas the media library browser will be presented in a `sheet`.
/// On Android, the camera and library browser will be activated through Intents after querying for the necessary permissions.
fun View.withMediaPicker(type: MediaPickerType, isPresented: Binding<Boolean>, selectedImageURL: Binding<URL?>): View {
    return ComposeBuilder l@{ composectx: ComposeContext ->
        when (type) {
            MediaPickerType.library -> {
                val pickImageLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
                    // uri e.g.: content://media/picker/0/com.android.providers.media.photopicker/media/1000000025
                    isPresented.wrappedValue = false // clear the presented bit
                    logger.log("pickImageLauncher: ${uri}")
                    if (uri != null) {
                        selectedImageURL.wrappedValue = URL(platformValue = java.net.URI.create(uri.toString()))
                    }
                }

                return@l onChange(of = isPresented.wrappedValue) { presented ->
                    if (presented == true) {
                        pickImageLauncher.launch("image/*")
                    }
                }.Compose(composectx)
            }
            MediaPickerType.camera -> {
                var imageURLString by rememberSaveable { mutableStateOf<String?>(null) }

                // alternatively, we could use TakePicturePreview, which returns a Bitmap
                val takePictureLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) { success ->
                    // uri e.g.: content://media/picker/0/com.android.providers.media.photopicker/media/1000000025
                    isPresented.wrappedValue = false // clear the presented bit
                    logger.log("takePictureLauncher: success: ${success} from ${imageURLString}")
                    if (success == true) {
                        imageURLString.sref()?.let { imageURLString ->
                            selectedImageURL.wrappedValue = (try { URL(string = imageURLString) } catch (_: NullReturnException) { null })
                        }
                    }
                }

                // FIXME: 05-20 20:29:41.435  8964  8964 E AndroidRuntime: java.lang.SecurityException: Permission Denial: starting Intent { act=android.media.action.IMAGE_CAPTURE flg=0x3 cmp=com.android.camera2/com.android.camera.CaptureActivity clip={text/uri-list hasLabel(0) {}} (has extras) } from ProcessRecord{c5fb1f 8964:skip.photo.chat/u0a190} (pid=8964, uid=10190) with revoked permission android.permission.CAMERA

                val context = LocalContext.current.sref()

                val PERM_REQUEST_CAMERA = 642

                return@l onChange(of = isPresented.wrappedValue) { presented ->
                    if (presented == true) {
                        var perms = listOf(Manifest.permission.CAMERA).toTypedArray()
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            logger.log("takePictureLauncher: requesting Manifest.permission.CAMERA permission")
                            ActivityCompat.requestPermissions(context.asActivity(), perms, PERM_REQUEST_CAMERA)
                            isPresented.wrappedValue = false
                        } else {
                            val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
                            val ext = ".jpg"
                            val tmpFile = java.io.File.createTempFile("SkipKit_${UUID().uuidString}", ext, storageDir)
                            logger.log("takePictureLauncher: create tmpFile: ${tmpFile}")

                            imageURLString = androidx.core.content.FileProvider.getUriForFile(context.asActivity(), context.getPackageName() + ".fileprovider", tmpFile).kotlin().toString()
                            logger.log("takePictureLauncher: takePictureLauncher.launch: ${imageURLString}")

                            takePictureLauncher.launch(android.net.Uri.parse(imageURLString))
                        }
                    }
                }.Compose(composectx)
            }
        }
        ComposeResult.ok
    }
}

