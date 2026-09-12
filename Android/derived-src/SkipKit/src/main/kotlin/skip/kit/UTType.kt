package skip.kit

import skip.lib.*

// Copyright 2025–2026 Skip
// SPDX-License-Identifier: MPL-2.0
@androidx.annotation.Keep
@Suppress("MUST_BE_INITIALIZED")
class UTType: MutableStruct, skip.lib.SwiftProjecting {
    internal var identifier: String
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var preferredMIMEType: String? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }

    // “This release does not yet support bridging optional inits”
    constructor(identifier: String, mimeType: String?, conformingTo: UTType? = UTType.data) {
        val supertype = conformingTo
        this.identifier = identifier
        this.preferredMIMEType = mimeType
    }

    private constructor(copy: MutableStruct) {
        @Suppress("NAME_SHADOWING", "UNCHECKED_CAST") val copy = copy as UTType
        this.identifier = copy.identifier
        this.preferredMIMEType = copy.preferredMIMEType
    }

    override var supdate: ((Any) -> Unit)? = null
    override var smutatingcount = 0
    override fun scopy(): MutableStruct = UTType(this as MutableStruct)

    override fun equals(other: Any?): Boolean {
        if (other !is UTType) return false
        return identifier == other.identifier && preferredMIMEType == other.preferredMIMEType
    }

    override fun hashCode(): Int {
        var result = 1
        result = Hasher.combine(result, identifier)
        result = Hasher.combine(result, preferredMIMEType)
        return result
    }

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {

        val item: UTType = UTType(identifier = "public.item", mimeType = null)
        val content: UTType = UTType(identifier = "public.content", mimeType = null)
        val compositeContent: UTType = UTType(identifier = "public.composite-content", mimeType = null)
        val diskImage: UTType = UTType(identifier = "public.disk-image", mimeType = null)
        val data: UTType = UTType(identifier = "public.data", mimeType = null)
        val directory: UTType = UTType(identifier = "public.directory", mimeType = null)
        val resolvable: UTType = UTType(identifier = "com.apple.resolvable", mimeType = null)
        val symbolicLink: UTType = UTType(identifier = "public.symlink", mimeType = null)
        val executable: UTType = UTType(identifier = "public.executable", mimeType = null)
        val mountPoint: UTType = UTType(identifier = "com.apple.mount-point", mimeType = null)
        val aliasFile: UTType = UTType(identifier = "com.apple.alias-file", mimeType = null)
        val urlBookmarkData: UTType = UTType(identifier = "com.apple.bookmark", mimeType = null)
        val url: UTType = UTType(identifier = "public.url", mimeType = null)
        val fileURL: UTType = UTType(identifier = "public.file-url", mimeType = null)
        val text: UTType = UTType(identifier = "public.text", mimeType = null)
        val plainText: UTType = UTType(identifier = "public.plain-text", mimeType = "text/plain")
        val utf8PlainText: UTType = UTType(identifier = "public.utf8-plain-text", mimeType = "text/plain;charset=utf-8")
        val utf16ExternalPlainText: UTType = UTType(identifier = "public.utf16-external-plain-text", mimeType = null)
        val utf16PlainText: UTType = UTType(identifier = "public.utf16-plain-text", mimeType = "text/plain;charset=utf-16")
        val delimitedText: UTType = UTType(identifier = "public.delimited-values-text", mimeType = null)
        val commaSeparatedText: UTType = UTType(identifier = "public.comma-separated-values-text", mimeType = "text/csv")
        val tabSeparatedText: UTType = UTType(identifier = "public.tab-separated-values-text", mimeType = "text/tab-separated-values")
        val utf8TabSeparatedText: UTType = UTType(identifier = "public.utf8-tab-separated-values-text", mimeType = null)
        val rtf: UTType = UTType(identifier = "public.rtf", mimeType = "text/rtf")
        val html: UTType = UTType(identifier = "public.html", mimeType = "text/html")
        val xml: UTType = UTType(identifier = "public.xml", mimeType = "application/xml")
        val yaml: UTType = UTType(identifier = "public.yaml", mimeType = "application/x-yaml")
        val css: UTType = UTType(identifier = "public.css", mimeType = "text/css")
        val sourceCode: UTType = UTType(identifier = "public.source-code", mimeType = null)
        val assemblyLanguageSource: UTType = UTType(identifier = "public.assembly-source", mimeType = null)
        val cSource: UTType = UTType(identifier = "public.c-source", mimeType = null)
        val objectiveCSource: UTType = UTType(identifier = "public.objective-c-source", mimeType = null)
        val swiftSource: UTType = UTType(identifier = "public.swift-source", mimeType = null)
        val cPlusPlusSource: UTType = UTType(identifier = "public.c-plus-plus-source", mimeType = null)
        val objectiveCPlusPlusSource: UTType = UTType(identifier = "public.objective-c-plus-plus-source", mimeType = null)
        val cHeader: UTType = UTType(identifier = "public.c-header", mimeType = null)
        val cPlusPlusHeader: UTType = UTType(identifier = "public.c-plus-plus-header", mimeType = null)
        val script: UTType = UTType(identifier = "public.script", mimeType = null)
        val appleScript: UTType = UTType(identifier = "com.apple.applescript.text", mimeType = null)
        val osaScript: UTType = UTType(identifier = "com.apple.applescript.script", mimeType = null)
        val osaScriptBundle: UTType = UTType(identifier = "com.apple.applescript.script-bundle", mimeType = null)
        val javaScript: UTType = UTType(identifier = "com.netscape.javascript-source", mimeType = "text/javascript")
        val shellScript: UTType = UTType(identifier = "public.shell-script", mimeType = null)
        val perlScript: UTType = UTType(identifier = "public.perl-script", mimeType = "text/x-perl-script")
        val pythonScript: UTType = UTType(identifier = "public.python-script", mimeType = "text/x-python-script")
        val rubyScript: UTType = UTType(identifier = "public.ruby-script", mimeType = "text/x-ruby-script")
        val phpScript: UTType = UTType(identifier = "public.php-script", mimeType = "text/php")
        val makefile: UTType = UTType(identifier = "public.make-source", mimeType = null)
        val json: UTType = UTType(identifier = "public.json", mimeType = "application/json")
        val propertyList: UTType = UTType(identifier = "com.apple.property-list", mimeType = null)
        val xmlPropertyList: UTType = UTType(identifier = "com.apple.xml-property-list", mimeType = null)
        val binaryPropertyList: UTType = UTType(identifier = "com.apple.binary-property-list", mimeType = null)
        val pdf: UTType = UTType(identifier = "com.adobe.pdf", mimeType = "application/pdf")
        val rtfd: UTType = UTType(identifier = "com.apple.rtfd", mimeType = null)
        val flatRTFD: UTType = UTType(identifier = "com.apple.flat-rtfd", mimeType = null)
        val webArchive: UTType = UTType(identifier = "com.apple.webarchive", mimeType = "application/x-webarchive")
        val image: UTType = UTType(identifier = "public.image", mimeType = "image/*")
        val jpeg: UTType = UTType(identifier = "public.jpeg", mimeType = "image/jpeg")
        val tiff: UTType = UTType(identifier = "public.tiff", mimeType = "image/tiff")
        val gif: UTType = UTType(identifier = "com.compuserve.gif", mimeType = "image/gif")
        val png: UTType = UTType(identifier = "public.png", mimeType = "image/png")
        val icns: UTType = UTType(identifier = "com.apple.icns", mimeType = null)
        val bmp: UTType = UTType(identifier = "com.microsoft.bmp", mimeType = "image/bmp")
        val ico: UTType = UTType(identifier = "com.microsoft.ico", mimeType = "image/vnd.microsoft.icon")
        val rawImage: UTType = UTType(identifier = "public.camera-raw-image", mimeType = null)
        val svg: UTType = UTType(identifier = "public.svg-image", mimeType = "image/svg+xml")
        val livePhoto: UTType = UTType(identifier = "com.apple.live-photo", mimeType = null)
        val heif: UTType = UTType(identifier = "public.heif", mimeType = "image/heif")
        val heic: UTType = UTType(identifier = "public.heic", mimeType = "image/heic")
        val heics: UTType = UTType(identifier = "public.heics", mimeType = "image/heic-sequence")
        val webP: UTType = UTType(identifier = "org.webmproject.webp", mimeType = "image/webp")
        val exr: UTType = UTType(identifier = "com.ilm.openexr-image", mimeType = null)
        val dng: UTType = UTType(identifier = "com.adobe.raw-image", mimeType = "image/x-adobe-dng")
        val jpegxl: UTType = UTType(identifier = "public.jpeg-xl", mimeType = "image/jxl")
        val threeDContent: UTType = UTType(identifier = "public.3d-content", mimeType = null)
        val usd: UTType = UTType(identifier = "com.pixar.universal-scene-description", mimeType = null)
        val usdz: UTType = UTType(identifier = "com.pixar.universal-scene-description-mobile", mimeType = "model/vnd.usdz+zip")
        val realityFile: UTType = UTType(identifier = "com.apple.reality", mimeType = "model/vnd.reality")
        val sceneKitScene: UTType = UTType(identifier = "com.apple.scenekit.scene", mimeType = null)
        val arReferenceObject: UTType = UTType(identifier = "com.apple.arobject", mimeType = null)
        val audiovisualContent: UTType = UTType(identifier = "public.audiovisual-content", mimeType = null)
        val movie: UTType = UTType(identifier = "public.movie", mimeType = null)
        val video: UTType = UTType(identifier = "public.video", mimeType = null)
        val audio: UTType = UTType(identifier = "public.audio", mimeType = null)
        val quickTimeMovie: UTType = UTType(identifier = "com.apple.quicktime-movie", mimeType = "video/quicktime")
        val mpeg: UTType = UTType(identifier = "public.mpeg", mimeType = "video/mpeg")
        val mpeg2Video: UTType = UTType(identifier = "public.mpeg-2-video", mimeType = "video/mpeg2")
        val mpeg2TransportStream: UTType = UTType(identifier = "public.mpeg-2-transport-stream", mimeType = null)
        val mp3: UTType = UTType(identifier = "public.mp3", mimeType = "audio/mpeg")
        val mpeg4Movie: UTType = UTType(identifier = "public.mpeg-4", mimeType = "video/mp4")
        val mpeg4Audio: UTType = UTType(identifier = "public.mpeg-4-audio", mimeType = "audio/mp4")
        val appleProtectedMPEG4Audio: UTType = UTType(identifier = "com.apple.protected-mpeg-4-audio", mimeType = null)
        val appleProtectedMPEG4Video: UTType = UTType(identifier = "com.apple.protected-mpeg-4-video", mimeType = null)
        val avi: UTType = UTType(identifier = "public.avi", mimeType = "video/avi")
        val aiff: UTType = UTType(identifier = "public.aiff-audio", mimeType = "audio/aiff")
        val wav: UTType = UTType(identifier = "com.microsoft.waveform-audio", mimeType = "audio/vnd.wave")
        val midi: UTType = UTType(identifier = "public.midi-audio", mimeType = "audio/midi")
        val playlist: UTType = UTType(identifier = "public.playlist", mimeType = null)
        val m3uPlaylist: UTType = UTType(identifier = "public.m3u-playlist", mimeType = "audio/mpegurl")
        val folder: UTType = UTType(identifier = "public.folder", mimeType = null)
        val volume: UTType = UTType(identifier = "public.volume", mimeType = null)
        val package_: UTType = UTType(identifier = "com.apple.package", mimeType = null)
        val bundle: UTType = UTType(identifier = "com.apple.bundle", mimeType = null)
        val pluginBundle: UTType = UTType(identifier = "com.apple.plugin", mimeType = null)
        val spotlightImporter: UTType = UTType(identifier = "com.apple.metadata-importer", mimeType = null)
        val quickLookGenerator: UTType = UTType(identifier = "com.apple.quicklook-generator", mimeType = null)
        val xpcService: UTType = UTType(identifier = "com.apple.xpc-service", mimeType = null)
        val framework: UTType = UTType(identifier = "com.apple.framework", mimeType = null)
        val application: UTType = UTType(identifier = "com.apple.application", mimeType = null)
        val applicationBundle: UTType = UTType(identifier = "com.apple.application-bundle", mimeType = null)
        val applicationExtension: UTType = UTType(identifier = "com.apple.application-and-system-extension", mimeType = null)
        val unixExecutable: UTType = UTType(identifier = "public.unix-executable", mimeType = null)
        val exe: UTType = UTType(identifier = "com.microsoft.windows-executable", mimeType = "application/x-msdownload")
        val systemPreferencesPane: UTType = UTType(identifier = "com.apple.systempreference.prefpane", mimeType = null)
        val archive: UTType = UTType(identifier = "public.archive", mimeType = null)
        val gzip: UTType = UTType(identifier = "org.gnu.gnu-zip-archive", mimeType = "application/x-gzip")
        val bz2: UTType = UTType(identifier = "public.bzip2-archive", mimeType = "application/x-bzip2")
        val zip: UTType = UTType(identifier = "public.zip-archive", mimeType = "application/zip")
        val appleArchive: UTType = UTType(identifier = "com.apple.archive", mimeType = null)
        val tarArchive: UTType = UTType(identifier = "public.tar-archive", mimeType = "application/x-tar")
        val spreadsheet: UTType = UTType(identifier = "public.spreadsheet", mimeType = null)
        val presentation: UTType = UTType(identifier = "public.presentation", mimeType = null)
        val database: UTType = UTType(identifier = "public.database", mimeType = null)
        val message: UTType = UTType(identifier = "public.message", mimeType = null)
        val contact: UTType = UTType(identifier = "public.contact", mimeType = null)
        val vCard: UTType = UTType(identifier = "public.vcard", mimeType = "text/vcard")
        val toDoItem: UTType = UTType(identifier = "public.to-do-item", mimeType = null)
        val calendarEvent: UTType = UTType(identifier = "public.calendar-event", mimeType = null)
        val emailMessage: UTType = UTType(identifier = "public.email-message", mimeType = null)
        val internetLocation: UTType = UTType(identifier = "com.apple.internet-location", mimeType = null)
        val internetShortcut: UTType = UTType(identifier = "com.microsoft.internet-shortcut", mimeType = null)
        val font: UTType = UTType(identifier = "public.font", mimeType = null)
        val bookmark: UTType = UTType(identifier = "public.bookmark", mimeType = null)
        val pkcs12: UTType = UTType(identifier = "com.rsa.pkcs-12", mimeType = "application/x-pkcs12")
        val x509Certificate: UTType = UTType(identifier = "public.x509-certificate", mimeType = "application/x-x509-ca-cert")
        val epub: UTType = UTType(identifier = "org.idpf.epub-container", mimeType = "application/epub+zip")
        val log: UTType = UTType(identifier = "public.log", mimeType = null)
        val ahap: UTType = UTType(identifier = "com.apple.haptics.ahap", mimeType = null)
        val geoJSON: UTType = UTType(identifier = "public.geojson", mimeType = "application/geo+json")
        val linkPresentationMetadata: UTType = UTType(identifier = "com.apple.linkpresentation.metadata", mimeType = null)
    }
}

