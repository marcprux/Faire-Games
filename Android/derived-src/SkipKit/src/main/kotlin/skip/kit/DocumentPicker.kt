package skip.kit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import skip.lib.*
import skip.lib.Array

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

/// Allow to present a Document picker interface activated by the `isPresented` binding. It will return the selected file URL through the `selectedDocumentURL` binding.
///
/// On iOS uses the `fileImporter` with allowed content types of `text`, `pdf` and `images`.
/// On Android it will user the intet action  ACTION_OPEN_DOCUMENT  to present the system picker for `pdf` and `images`.
/// It optionally also returns the real `filename` and `mimeType` through the corresponding bindings, since on this platform the document pickers returns an obfuscated url. Also, on Android, in order for the url to be accessible outside the scope of this call a copy of the file is made in the cache directory, and the copied file url is returned
/// - Parameters:
///   - isPresented: binding for presentation
///   - selectedDocumentURL: the URL of the selected file
///   - filename: the filename of the selected file
///   - mimeType: the mimeType of the selected file
fun View.withDocumentPicker(isPresented: Binding<Boolean>, allowedContentTypes: Array<UTType>, selectedDocumentURL: Binding<URL?>, selectedFilename: Binding<String?>, selectedFileMimeType: Binding<String?>): View {
    return ComposeBuilder l@{ composectx: ComposeContext ->
        val context = LocalContext.current.sref()

        val pickDocumentLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
            isPresented.wrappedValue = false
            logger.log(message = "selected document uri: ${uri}")
            if (uri != null) {
                val resolver = context.contentResolver.sref()

                resolver.query(uri, null, null, null, null)?.let { query ->
                    val nameIndex = query.getColumnIndexOrThrow(android.provider.OpenableColumns.DISPLAY_NAME)
                    val mimetypeIndex = query.getColumnIndexOrThrow(android.provider.DocumentsContract.Document.COLUMN_MIME_TYPE)
                    query.moveToFirst()
                    val name = query.getString(nameIndex)
                    val type = query.getString(mimetypeIndex)

                    selectedFilename.wrappedValue = name
                    selectedFileMimeType.wrappedValue = type

                    // To be able to access the file from another part of the app it needs to be copied in tha cached directory:
                    val matchtarget_0 = context.cacheDir
                    if (matchtarget_0 != null) {
                        val storageDir = matchtarget_0
                        val matchtarget_1 = (try { URL(string = storageDir.path) } catch (_: NullReturnException) { null })
                        if (matchtarget_1 != null) {
                            val url = matchtarget_1
                            val filemanager = FileManager.default
                            val destinationFileURL = url.appendingPathComponent(selectedFilename.wrappedValue!!)

                            if (filemanager.fileExists(atPath = destinationFileURL.path)) {
                                try { filemanager.removeItem(at = destinationFileURL) } catch (_: Throwable) { null }
                            }

                            val inputStream = resolver.openInputStream(uri)!!
                            val outputFile = java.io.File(destinationFileURL.path)
                            val outputStream = java.io.FileOutputStream(outputFile)
                            inputStream.copyTo(outputStream)

                            outputStream.close()
                            inputStream.close()

                            selectedDocumentURL.wrappedValue = destinationFileURL
                        } else {
                            selectedDocumentURL.wrappedValue = URL(platformValue = java.net.URI.create(uri.toString()))
                        }
                    } else {
                        selectedDocumentURL.wrappedValue = URL(platformValue = java.net.URI.create(uri.toString()))
                    }
                }
            }
        }

        return@l onChange(of = isPresented.wrappedValue) { oldValue, presented ->
            if (presented == true) {
                val parsedMimeTypes: Array<String> = allowedContentTypes.map({ it -> it.preferredMIMEType ?: "" })
                var types = kotlin.arrayOf("")
                for (type in parsedMimeTypes.sref()) {
                    types += type
                }
                val mimeTypes = types.sref() //kotlin.arrayOf("application/pdf", "image/*")
                pickDocumentLauncher.launch(mimeTypes)
            }
        }.Compose(composectx)
 // !SKIP
        ComposeResult.ok
    }
}
