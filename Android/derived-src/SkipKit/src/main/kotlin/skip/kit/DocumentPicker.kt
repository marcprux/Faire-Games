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

/// Allows presenting a document picker interface activated by the `isPresented` binding.
///
/// On iOS, this uses `fileImporter` with the supplied content types. On Android, this uses the
/// `ACTION_OPEN_DOCUMENT` intent. Android returns an obfuscated URL, so the selected document is
/// copied into the app cache and the copied file URL is returned together with its filename and MIME type.
///
/// - Parameters:
///   - isPresented: Binding for presentation.
///   - allowedContentTypes: The content types that can be selected.
///   - selectedDocumentURL: The URL of the selected file.
///   - selectedFilename: The filename of the selected file.
///   - selectedFileMimeType: The MIME type of the selected file.
fun View.withDocumentPicker(isPresented: Binding<Boolean>, allowedContentTypes: Array<UTType>, selectedDocumentURL: Binding<URL?>, selectedFilename: Binding<String?>, selectedFileMimeType: Binding<String?>): View {
    return ComposeBuilder { composectx: ComposeContext ->
        this.withDocumentPicker(isPresented = isPresented, allowedContentTypes = allowedContentTypes, allowsMultipleSelection = false, selectedDocumentURLs = Binding(get = l@{ ->
            selectedDocumentURL.wrappedValue.sref()?.let { value ->
                return@l arrayOf(value)
            }
            return@l arrayOf()
        }, set = { it -> selectedDocumentURL.wrappedValue = it.first }), selectedFilenames = Binding(get = l@{ ->
            selectedFilename.wrappedValue.sref()?.let { value ->
                return@l arrayOf(value)
            }
            return@l arrayOf()
        }, set = { it -> selectedFilename.wrappedValue = it.first }), selectedFileMimeTypes = Binding(get = l@{ ->
            selectedFileMimeType.wrappedValue.sref()?.let { value ->
                return@l arrayOf(value)
            }
            return@l arrayOf()
        }, set = { it -> selectedFileMimeType.wrappedValue = it.first })).Compose(composectx)
    }
}

/// Allows presenting a document picker interface activated by the `isPresented` binding.
///
/// On iOS, this uses `fileImporter` with the supplied content types. On Android, this uses the
/// `ACTION_OPEN_DOCUMENT` intent. Android returns obfuscated URLs, so selected documents are
/// copied into the app cache and the copied file URLs are returned together with their filenames and MIME types.
///
/// - Parameters:
///   - isPresented: Binding for presentation.
///   - allowedContentTypes: The content types that can be selected.
///   - allowsMultipleSelection: Whether multiple documents can be selected.
///   - selectedDocumentURLs: The URLs of the selected files.
///   - selectedFilenames: The filenames of the selected files.
///   - selectedFileMimeTypes: The MIME types of the selected files.
fun View.withDocumentPicker(isPresented: Binding<Boolean>, allowedContentTypes: Array<UTType>, allowsMultipleSelection: Boolean, selectedDocumentURLs: Binding<Array<URL>>, selectedFilenames: Binding<Array<String>>, selectedFileMimeTypes: Binding<Array<String>>): View {
    return ComposeBuilder l@{ composectx: ComposeContext ->

        val context = LocalContext.current.sref()

        val pickDocumentLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
            isPresented.wrappedValue = false
            logger.log(message = "selected document uri: ${uri}")
            if (uri != null) {
                val result = resolvePickedDocument(uri = uri, context = context)
                selectedFilenames.wrappedValue = arrayOf(result.filename)
                selectedFileMimeTypes.wrappedValue = arrayOf(result.mimeType ?: "")
                val matchtarget_0 = result.url
                if (matchtarget_0 != null) {
                    val url = matchtarget_0
                    selectedDocumentURLs.wrappedValue = arrayOf(url)
                } else {
                    selectedDocumentURLs.wrappedValue = arrayOf()
                }
            }
        }

        val pickDocumentsLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            isPresented.wrappedValue = false
            var urls = Array<URL>()
            var filenames = Array<String>()
            var mimeTypes = Array<String>()

            for (uri in uris.sref()) {
                val result = resolvePickedDocument(uri = uri, context = context, uniqueDestinationName = true)
                result.url?.let { url ->
                    urls.append(url)
                    filenames.append(result.filename)
                    mimeTypes.append(result.mimeType ?: "")
                }
            }

            selectedDocumentURLs.wrappedValue = urls
            selectedFilenames.wrappedValue = filenames
            selectedFileMimeTypes.wrappedValue = mimeTypes
        }

        return@l onChange(of = isPresented.wrappedValue) { oldValue, presented ->
            if (presented == true) {
                val parsedMimeTypes: Array<String> = allowedContentTypes.map { it -> it.preferredMIMEType ?: "" }
                var types = kotlin.arrayOf("*/*")
                for (type in parsedMimeTypes.sref()) {
                    if (type.isEmpty == false) {
                        types += type
                    }
                }
                val mimeTypes = types.sref()
                isPresented.wrappedValue = false
                if (allowsMultipleSelection) {
                    pickDocumentsLauncher.launch(mimeTypes)
                } else {
                    pickDocumentLauncher.launch(mimeTypes)
                }
            }
        }.Compose(composectx)
 // !SKIP
        ComposeResult.ok
    }
}

/// Allows presenting a document exporter interface activated by the `isPresented` binding.
///
/// On iOS, this uses `fileExporter` to present the system export dialog. On Android, this uses the
/// `ACTION_CREATE_DOCUMENT` intent and copies the file to the selected location.
///
/// - Parameters:
///   - isPresented: Binding for presentation.
///   - contentType: The content type of the exported file.
///   - documentURL: The URL of the file to export.
///   - onCompletion: Called when the export finishes or fails.
fun View.withDocumentExporter(isPresented: Binding<Boolean>, contentType: UTType, documentURL: URL?, onCompletion: ((Result<URL, Error>) -> Unit)? = null): View {
    return ComposeBuilder l@{ composectx: ComposeContext ->

        val context = LocalContext.current.sref()
        val mimeType = contentType.preferredMIMEType ?: "*/*"

        val exportDocumentLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.CreateDocument(mimeType)) l@{ uri ->
            isPresented.wrappedValue = false
            if ((uri == null) || (documentURL == null)) {
                return@l
            }

            try {
                val outputStream_0 = context.contentResolver.openOutputStream(uri)
                if (outputStream_0 == null) {
                    throw CocoaError.error(CocoaError.fileWriteUnknown) as Throwable
                }

                val inputStream = java.io.FileInputStream(java.io.File(documentURL.path))
                inputStream.copyTo(outputStream_0)
                inputStream.close()
                outputStream_0.close()
                onCompletion?.invoke(Result.success(URL(platformValue = java.net.URI.create(uri.toString()))))
            } catch (error: Throwable) {
                @Suppress("NAME_SHADOWING") val error = error.aserror()
                onCompletion?.invoke(Result.failure(error))
            }
        }

        return@l onChange(of = isPresented.wrappedValue) { oldValue, presented ->
            if (presented == true) {
                isPresented.wrappedValue = false
                exportDocumentLauncher.launch(documentURL?.lastPathComponent ?: "Document")
            }
        }.Compose(composectx)
 // !SKIP
        ComposeResult.ok
    }
}

private fun resolvePickedDocument(uri: android.net.Uri, context: Context, uniqueDestinationName: Boolean = false): Tuple3<URL?, String, String?> {

    val resolver = context.contentResolver.sref()
    var resolvedName: String? = null
    var resolvedMime: String? = null

    resolver.query(uri, null, null, null, null)?.let { query ->
        if (query.moveToFirst()) {
            // Downloads provider omits these columns; tolerate -1.
            val nameIndex = query.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0) {
                resolvedName = query.getString(nameIndex)
            }
            val mimeIndex = query.getColumnIndex(android.provider.DocumentsContract.Document.COLUMN_MIME_TYPE)
            if (mimeIndex >= 0) {
                resolvedMime = query.getString(mimeIndex)
            }
        }
        query.close()
    }

    if (resolvedMime == null) {
        resolvedMime = resolver.getType(uri)
    }

    val safeName: String = resolvedName ?: "import-${java.util.UUID.randomUUID().toString()}"
    val destinationName: String = if (uniqueDestinationName) "${java.util.UUID.randomUUID().toString()}-${safeName}" else safeName

    // java.io.File path avoids Skip URL.appendingPathComponent NPE.
    val matchtarget_1 = context.cacheDir
    if (matchtarget_1 != null) {
        val cacheDir = matchtarget_1
        val destinationFile = java.io.File(cacheDir, destinationName)
        if (destinationFile.exists()) {
            destinationFile.delete()
        }
        val matchtarget_2 = resolver.openInputStream(uri)
        if (matchtarget_2 != null) {
            val inputStream = matchtarget_2
            val outputStream = java.io.FileOutputStream(destinationFile)
            inputStream.copyTo(outputStream)
            outputStream.close()
            inputStream.close()
            // File.toURI() percent-encodes; raw path would crash java.net.URI.
            return Tuple3(URL(platformValue = destinationFile.toURI()), safeName, resolvedMime)
        } else {
            return Tuple3(URL(platformValue = java.net.URI.create(uri.toString())), safeName, resolvedMime)
        }
    } else {
        return Tuple3(URL(platformValue = java.net.URI.create(uri.toString())), safeName, resolvedMime)
    }
}

