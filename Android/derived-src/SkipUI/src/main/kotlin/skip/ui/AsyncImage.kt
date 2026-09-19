package skip.ui

import skip.lib.*
import skip.lib.Set

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0
import skip.foundation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import android.webkit.MimeTypeMap
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.rememberAsyncImagePainter
import coil3.compose.rememberConstraintsSizeResolver
import coil3.request.ImageRequest
import coil3.fetch.Fetcher
import coil3.fetch.FetchResult
import coil3.ImageLoader
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.PlatformContext
import coil3.asImage
import kotlin.math.roundToInt
import okio.buffer
import okio.source

/// Composes AsyncImage placeholder views (empty / error / nil URL) with optional ``EnvironmentValues/_aspectRatio`` from the environment.
@Composable
private fun composePlaceholder(view: View, context: ComposeContext) {
    val matchtarget_0 = EnvironmentValues.shared._aspectRatio
    if (matchtarget_0 != null) {
        val (aspectRatio, contentMode) = matchtarget_0
        view.aspectRatio(aspectRatio, contentMode = contentMode).Compose(context = context)
    } else {
        view.Compose(context = context)
    }
}

@androidx.annotation.Keep
@Suppress("MUST_BE_INITIALIZED")
class AsyncImageBridgedContentArguments: MutableStruct, skip.lib.SwiftProjecting {
    var image: Image? = null
        get() = field.sref({ this.image = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    var error: Error? = null
        get() = field.sref({ this.error = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    var isEmpty: Boolean
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }

    constructor(image: Image? = null, error: Error? = null, isEmpty: Boolean) {
        this.image = image
        this.error = error
        this.isEmpty = isEmpty
    }

    override var supdate: ((Any) -> Unit)? = null
    override var smutatingcount = 0
    override fun scopy(): MutableStruct = AsyncImageBridgedContentArguments(image, error, isEmpty)

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {
    }
}

@androidx.annotation.Keep
class AsyncImage: View, Renderable, skip.lib.SwiftProjecting {
    internal val url: URL?
    internal val scale: Double
    internal val content: (AsyncImagePhase) -> View

    constructor(url: URL?, scale: Double = 1.0) {
        this.url = url.sref()
        this.scale = scale
        this.content = l@{ phase ->
            when (phase) {
                is AsyncImagePhase.EmptyCase -> {
                    val matchtarget_1 = phase.image
                    if (matchtarget_1 != null) {
                        val image = matchtarget_1
                        return@l ZStack { ->
                            ComposeBuilder { composectx: ComposeContext ->
                                Color.clear.Compose(composectx) // Showing a placeholder here prevents layout shift if the image is 0x0 on the first frame
                                image.Compose(composectx)
                                ComposeResult.ok
                            }
                        }
                    } else {
                        return@l Companion.defaultPlaceholder()
                    }
                }
                is AsyncImagePhase.FailureCase -> return@l Companion.defaultPlaceholder()
                is AsyncImagePhase.SuccessCase -> {
                    val image = phase.associated0
                    return@l image
                }
            }
        }
    }

    constructor(url: URL?, scale: Double = 1.0, content: (Image) -> View, placeholder: () -> View) {
        this.url = url.sref()
        this.scale = scale
        this.content = l@{ phase ->
            when (phase) {
                is AsyncImagePhase.EmptyCase -> {
                    val matchtarget_2 = phase.image
                    if (matchtarget_2 != null) {
                        val image = matchtarget_2
                        return@l ZStack { ->
                            ComposeBuilder { composectx: ComposeContext ->
                                Color.clear.Compose(composectx) // Showing a placeholder here prevents layout shift if the image is 0x0 on the first frame
                                content(image).Compose(composectx)
                                ComposeResult.ok
                            }
                        }
                    } else {
                        return@l placeholder()
                    }
                }
                is AsyncImagePhase.FailureCase -> return@l placeholder()
                is AsyncImagePhase.SuccessCase -> {
                    val image = phase.associated0
                    return@l content(image)
                }
            }
        }
    }

    constructor(url: URL?, scale: Double = 1.0, transaction: Any? = null, content: (AsyncImagePhase) -> View) {
        this.url = url.sref()
        this.scale = scale
        this.content = content
    }

    // Note that we reverse the `url` and `scale` parameter order just to create a unique JVM signature
    constructor(scale: Double, url: URL?, bridgedContent: ((AsyncImageBridgedContentArguments) -> View)?) {
        this.url = url.sref()
        this.scale = scale
        this.content = l@{ phase ->
            if (bridgedContent != null) {
                when (phase) {
                    is AsyncImagePhase.EmptyCase -> return@l bridgedContent(AsyncImageBridgedContentArguments(image = phase.image, error = null, isEmpty = true))
                    is AsyncImagePhase.FailureCase -> {
                        val error = phase.associated0
                        return@l bridgedContent(AsyncImageBridgedContentArguments(image = null, error = error, isEmpty = false))
                    }
                    is AsyncImagePhase.SuccessCase -> {
                        val image = phase.associated0
                        return@l bridgedContent(AsyncImageBridgedContentArguments(image = image, error = null, isEmpty = false))
                    }
                }
            } else {
                when (phase) {
                    is AsyncImagePhase.EmptyCase -> {
                        val matchtarget_3 = phase.image
                        if (matchtarget_3 != null) {
                            val image = matchtarget_3
                            return@l ZStack { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    Color.clear.Compose(composectx) // Showing a placeholder here prevents layout shift if the image is 0x0 on the first frame
                                    image.Compose(composectx)
                                    ComposeResult.ok
                                }
                            }
                        } else {
                            return@l Companion.defaultPlaceholder()
                        }
                    }
                    is AsyncImagePhase.FailureCase -> {
                        val error = phase.associated0
                        return@l Companion.defaultPlaceholder()
                    }
                    is AsyncImagePhase.SuccessCase -> {
                        val image = phase.associated0
                        return@l image
                    }
                }
            }
        }
    }

    @Composable
    override fun Render(context: ComposeContext) {
        if (url == null) {
            val placeholderView = this.content(AsyncImagePhase.empty(null))
            composePlaceholder(placeholderView, context = context)
            return
        }

        val urlString = url.absoluteString
        // Coil does not automatically handle embedded jar URLs like
        // jar:file:/data/app/…/base.apk!/showcase/module/Resources/swift-logo.png or
        // asset:/showcase/module/Resources/swift-logo.png, so
        // we add a custom fetchers that will handle loading the URL.
        // Otherwise use Coil's default URL string handling
        val requestSource: Any = (if (AssetURLFetcher.handlesURL(url)) url else urlString).sref()
        val androidContext = LocalContext.current.sref()

        if (EnvironmentValues.shared._subcomposeAsyncImage) {
            val dm = androidContext.resources.displayMetrics.sref()
            val maxPx = max(Int(dm.widthPixels), Int(dm.heightPixels))
            val cacheKey = "${urlString}#${maxPx}x${maxPx}"

            val model = remember(urlString, maxPx) l@{ ->
                // Coil refuses to use its memory cache for .size(Size.ORIGINAL) requests!
                // We're using maxPx as an arbitrary bound to force it to cache properly
                // Coil memory-cache size validation is in MemoryCacheService.isCacheValueValidForSize:
                // See compose-source/io-coil-kt-coil3/coil-core-android/commonMain/coil3/memory/MemoryCacheService.kt:127.
                return@l ImageRequest.Builder(androidContext)
                    .fetcherFactory(AssetURLFetcher.Factory())
                    .decoderFactory(coil3.svg.SvgDecoder.Factory())
                    .decoderFactory(PdfDecoder.Factory())
                    .data(requestSource)
                    .size(coil3.size.Size(width = maxPx, height = maxPx))
                    .memoryCacheKey(cacheKey)
                    .diskCacheKey(cacheKey)
                    .build()
            }

            SubcomposeAsyncImage(model = model, contentDescription = null, loading = { _ ->
                val placeholderView = content(AsyncImagePhase.empty(null))
                composePlaceholder(placeholderView, context = context)
            }, success = { state ->
                val image = Image(painter = this.painter, scale = scale)
                val content = content(AsyncImagePhase.success(image))
                content.Compose(context = context)
            }, error = { state ->
                val placeholderView = content(AsyncImagePhase.failure(ErrorException(cause = state.result.throwable)))
                composePlaceholder(placeholderView, context = context)
            })
            return
        }

        val sizeResolver = rememberConstraintsSizeResolver()
        val cacheKey = "${urlString}#layout"
        val model = remember(urlString, sizeResolver) l@{ ->
            return@l ImageRequest.Builder(androidContext)
                .fetcherFactory(AssetURLFetcher.Factory())
                .decoderFactory(coil3.svg.SvgDecoder.Factory())
                .decoderFactory(PdfDecoder.Factory())
                .data(requestSource)
                .size(sizeResolver)
                .memoryCacheKey(cacheKey)
                .diskCacheKey(cacheKey)
                .build()
        }
        val painter = rememberAsyncImagePainter(model = model, contentScale = ContentScale.Fit)
        val asyncImageState = painter.state.collectAsState()

        val innerContext = context.content()
        Box(modifier = context.modifier.then(sizeResolver), contentAlignment = androidx.compose.ui.Alignment.Center) { ->
            val state = asyncImageState.value.sref()
            if (state == AsyncImagePainter.State.Empty) {
                // In this case, Coil doesn't yet know the true state
                // If the image is cached in memory, we can render it right away
                // If not, we'd want to show a placeholder
                val coilImage = Image(painter = painter, scale = scale)
                val placeholderView = content(AsyncImagePhase.empty(coilImage))
                composePlaceholder(placeholderView, context = innerContext)
            } else if (state is AsyncImagePainter.State.Loading) {
                val placeholderView = content(AsyncImagePhase.empty(null))
                composePlaceholder(placeholderView, context = innerContext)
            } else if (state is AsyncImagePainter.State.Success) {
                val image = Image(painter = painter, scale = scale)
                val successContent = content(AsyncImagePhase.success(image))
                successContent.Compose(context = innerContext)
            } else if (state is AsyncImagePainter.State.Error) {
                val errorState = (state as AsyncImagePainter.State.Error).sref()
                val placeholderView = content(AsyncImagePhase.failure(ErrorException(cause = errorState.result.throwable)))
                composePlaceholder(placeholderView, context = innerContext)
            }
        }
    }

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {

        private fun defaultPlaceholder(): View {
            return ComposeBuilder { composectx: ComposeContext -> Color.placeholder.Compose(composectx) }
        }
    }
}

sealed class AsyncImagePhase {
    // This is either Coil's `Loading` state with a nil image, or `Empty` state with a painter-backed image
    // In Coil's `Empty` state, Coil doesn't yet know whether the image is in memory cache or not
    class EmptyCase(val associated0: Image?): AsyncImagePhase() {
    }
    class SuccessCase(val associated0: Image): AsyncImagePhase() {
    }
    class FailureCase(val associated0: Error): AsyncImagePhase() {
    }

    val image: Image?
        get() {
            when (this) {
                is AsyncImagePhase.SuccessCase -> {
                    val image = this.associated0
                    return image
                }
                is AsyncImagePhase.EmptyCase -> {
                    val image = this.associated0
                    return image
                }
                else -> return null
            }
        }

    val error: Error?
        get() {
            when (this) {
                is AsyncImagePhase.FailureCase -> {
                    val error = this.associated0
                    return error
                }
                else -> return null
            }
        }

    @androidx.annotation.Keep
    companion object {
        fun empty(associated0: Image?): AsyncImagePhase = EmptyCase(associated0)
        fun success(associated0: Image): AsyncImagePhase = SuccessCase(associated0)
        fun failure(associated0: Error): AsyncImagePhase = FailureCase(associated0)
    }
}

/// A Coil fetcher that handles `skip.foundation.URL` instances for known custom URL schemes.
internal class AssetURLFetcher: Fetcher {
    private val url: URL
    private val options: coil3.request.Options

    internal constructor(url: URL, options: coil3.request.Options) {
        this.url = url.sref()
        this.options = options.sref()
    }

    override suspend fun fetch(): FetchResult = Async.run l@{
        val ctx = options.context.sref()
        val stream = url.kotlin().toURL().openConnection().getInputStream().source().buffer()
        val source = coil3.decode.ImageSource(source = stream, fileSystem = okio.FileSystem.SYSTEM)
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url.absoluteString))
        val dataSource: coil3.decode.DataSource = coil3.decode.DataSource.DISK.sref()
        return@l coil3.fetch.SourceFetchResult(source = source, mimeType = mimeType, dataSource = dataSource)
    }

    internal class Factory: Fetcher.Factory<URL> {
        override fun create(data: URL, options: coil3.request.Options, imageLoader: ImageLoader): Fetcher? {
            if ((!AssetURLFetcher.handlesURL(data))) {
                return null
            }
            return AssetURLFetcher(url = data, options = options)
        }
    }

    @androidx.annotation.Keep
    companion object {
        internal val handledURLSchemes: Set<String> = setOf("asset", "jar", "jarfile", "jar:file")

        internal fun handlesURL(url: URL): Boolean {
            val scheme_0 = url.scheme
            if (scheme_0 == null) {
                return false
            }
            return handledURLSchemes.contains(scheme_0)
        }
    }
}

internal open class PdfDecoder: coil3.decode.Decoder {
    internal val sourceResult: coil3.fetch.SourceFetchResult
    internal val options: coil3.request.Options

    internal class Factory: coil3.decode.Decoder.Factory {
        override fun create(result: coil3.fetch.SourceFetchResult, options: coil3.request.Options, imageLoader: coil3.ImageLoader): coil3.decode.Decoder? {
            //logger.debug("PdfDecoder.Factory.create result=\(result) options=\(options) imageLoader=\(imageLoader)")
            return PdfDecoder(sourceResult = result, options = options)
        }
    }

    internal constructor(sourceResult: coil3.fetch.SourceFetchResult, options: coil3.request.Options) {
        this.sourceResult = sourceResult.sref()
        this.options = options.sref()
    }

    override suspend fun decode(): coil3.decode.DecodeResult? = Async.run l@{
        val deferactions_0: MutableList<() -> Unit> = mutableListOf()
        try {
            val src: coil3.decode.ImageSource = sourceResult.source.sref()
            val source: okio.BufferedSource = src.source()

            // make sure it is a PDF image by scanning for "%PDF-" (25 50 44 46 2D)
            val peek = source.peek()
            // logger.debug("PdfDecoder.decode peek \(peek.readByte()) \(peek.readByte()) \(peek.readByte()) \(peek.readByte()) \(peek.readByte())")

            if (peek.readByte() != Byte(0x25)) {
                return@l null // %
            } // %
            if (peek.readByte() != Byte(0x50)) {
                return@l null // P
            } // P
            if (peek.readByte() != Byte(0x44)) {
                return@l null // D
            } // D
            if (peek.readByte() != Byte(0x46)) {
                return@l null // F
            } // F
            if (peek.readByte() != Byte(0x2D)) {
                return@l null // -
            } // -

            // Unfortunately, PdfRenderer requires a ParcelFileDescriptor, which can only be created from an actual file, and not the JarInputStream from which we load assets from the .apk; so we need to write the PDF out to a temporary file in order to be able to render the PDF to a Bitmap that Coil can use
            // Fortunately, even through we are loading from a buffer, Coil's ImageSource.file() function will: “Return a Path that resolves to a file containing this ImageSource's data. If this image source is backed by a BufferedSource, a temporary file containing this ImageSource's data will be created.”
            val imageFile = src.file().toFile()

            val parcelFileDescriptor = android.os.ParcelFileDescriptor.open(imageFile, android.os.ParcelFileDescriptor.MODE_READ_ONLY)
            deferactions_0.add {
                parcelFileDescriptor.close()
            }

            val pdfRenderer = android.graphics.pdf.PdfRenderer(parcelFileDescriptor)
            deferactions_0.add {
                pdfRenderer.close()
            }

            val page = pdfRenderer.openPage(0)
            deferactions_0.add {
                page.close()
            }

            val density = options.context.resources.displayMetrics.density.sref()

            val srcWidth = Double(page.width * density)
            val srcHeight = Double(page.height * density)

            val optionsWidth = (options.size.width as? coil3.size.Dimension.Pixels)?.px?.toDouble()
            val optionsHeight = (options.size.height as? coil3.size.Dimension.Pixels)?.px?.toDouble()

            val dstWidth: Double = optionsWidth ?: srcWidth
            val dstHeight: Double = optionsHeight ?: srcHeight

            val scale = coil3.decode.DecodeUtils.computeSizeMultiplier(srcWidth = srcWidth, srcHeight = srcHeight, dstWidth = dstWidth, dstHeight = dstHeight, scale = options.scale)

            val width = (scale * srcWidth).roundToInt()
            val height = (scale * srcHeight).roundToInt()

            logger.debug("PdfDecoder.decode result=${sourceResult} options=${options} imageFile=${imageFile} width=${width} height=${height}")
            val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            val drawable = android.graphics.drawable.BitmapDrawable(options.context.resources, bitmap)
            return@l coil3.decode.DecodeResult(image = drawable.asImage(), isSampled = true)
        } finally {
            deferactions_0.asReversed().forEach { it.invoke() }
        }
    }
}
