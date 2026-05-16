package skip.kit

import skip.lib.*
import skip.lib.Array

// Copyright 2025–2026 Skip
// SPDX-License-Identifier: MPL-2.0
import skip.foundation.*

// MARK: - SPDX SBOM Model

/// A parsed SPDX (Software Package Data Exchange) Software Bill of Materials document.
///
/// This is a Codable representation of the SPDX 2.3 JSON format produced by tools like
/// `skip sbom create` (for the iOS/SwiftPM dependency tree) and the `spdx-gradle-plugin`
/// (for the Android/Gradle dependency tree).
///
/// See: https://spdx.github.io/spdx-spec/v2.3/
@androidx.annotation.Keep
@Suppress("MUST_BE_INITIALIZED")
internal class SBOMDocument: Codable, MutableStruct {
    /// The SPDX version this document conforms to (e.g., `SPDX-2.3`).
    internal var spdxVersion: String? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    /// The SPDX identifier for this document (typically `SPDXRef-DOCUMENT`).
    internal var SPDXID: String? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    /// The data license for the SPDX document content (typically `CC0-1.0`).
    internal var dataLicense: String? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    /// A human-readable name for the document.
    internal var name: String? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    /// A unique URI namespace identifying this document.
    internal var documentNamespace: String? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    /// Information about how and when the SBOM was created.
    internal var creationInfo: SBOMCreationInfo? = null
        get() = field.sref({ this.creationInfo = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    /// The packages described by this SBOM.
    internal var packages: Array<SBOMPackage>
        get() = field.sref({ this.packages = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    /// Relationships between SPDX elements (e.g., DEPENDS_ON).
    internal var relationships: Array<SBOMRelationship>? = null
        get() = field.sref({ this.relationships = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    /// Custom (non-SPDX-listed) license definitions used by packages in this document.
    internal var hasExtractedLicensingInfos: Array<SBOMExtractedLicense>? = null
        get() = field.sref({ this.hasExtractedLicensingInfos = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }

    internal constructor(spdxVersion: String? = null, SPDXID: String? = null, dataLicense: String? = null, name: String? = null, documentNamespace: String? = null, creationInfo: SBOMCreationInfo? = null, packages: Array<SBOMPackage> = arrayOf(), relationships: Array<SBOMRelationship>? = null, hasExtractedLicensingInfos: Array<SBOMExtractedLicense>? = null) {
        this.spdxVersion = spdxVersion
        this.SPDXID = SPDXID
        this.dataLicense = dataLicense
        this.name = name
        this.documentNamespace = documentNamespace
        this.creationInfo = creationInfo
        this.packages = packages
        this.relationships = relationships
        this.hasExtractedLicensingInfos = hasExtractedLicensingInfos
    }

    /// All packages in the document excluding any "root" application packages (those with
    /// `primaryPackagePurpose == "APPLICATION"` or those that match the document name),
    /// sorted alphabetically by name (case-insensitive). This is the "flat" list of every
    /// dependency users typically want to see in a Bill of Materials view.
    internal val dependencyPackages: Array<SBOMPackage>
        get() {
            val docName = this.name ?: ""
            val filtered = packages.filter l@{ pkg ->
                if (pkg.primaryPackagePurpose == "APPLICATION") {
                    return@l false
                }
                // Filter out the root project package, which the gradle plugin emits with the
                // document name and a sourceInfo of "git+<no-scm-uri>...".
                pkg.name?.let { pkgName ->
                    if (pkgName == docName) {
                        return@l false
                    }
                }
                return@l true
            }
            return sortedByName(filtered)
        }

    /// The SPDX identifier of the root package described by this document, found via the
    /// `SPDXRef-DOCUMENT DESCRIBES <root>` relationship. Falls back to the first package
    /// with `primaryPackagePurpose == "APPLICATION"` if no `DESCRIBES` relationship exists.
    internal val rootPackageSPDXID: String?
        get() {
            relationships.sref()?.let { rels ->
                for (rel in rels.sref()) {
                    if (rel.relationshipType == "DESCRIBES" && rel.spdxElementId == "SPDXRef-DOCUMENT") {
                        rel.relatedSpdxElement?.let { related ->
                            return related
                        }
                    }
                }
            }
            for (pkg in packages.sref()) {
                if (pkg.primaryPackagePurpose == "APPLICATION") {
                    return pkg.SPDXID
                }
            }
            return null
        }

    /// Looks up a package by its SPDX identifier.
    internal fun package_(forSPDXID: String): SBOMPackage? {
        val spdxId = forSPDXID
        for (pkg in packages.sref()) {
            if (pkg.SPDXID == spdxId) {
                return pkg.sref()
            }
        }
        return null
    }

    /// Returns the packages directly depended on by the package with the given SPDX
    /// identifier (i.e., `<spdxId> DEPENDS_ON X`), sorted alphabetically by name.
    /// If the document has no relationships, returns an empty array.
    internal fun directDependencies(ofSPDXID: String): Array<SBOMPackage> {
        val spdxId = ofSPDXID
        val rels_0 = relationships.sref()
        if (rels_0 == null) {
            return arrayOf()
        }
        var result: Array<SBOMPackage> = arrayOf()
        for (rel in rels_0.sref()) {
            if (rel.relationshipType != "DEPENDS_ON") {
                continue
            }
            if (rel.spdxElementId != spdxId) {
                continue
            }
            val target_0 = rel.relatedSpdxElement
            if (target_0 == null) {
                continue
            }
            package_(forSPDXID = target_0)?.let { pkg ->
                result.append(pkg)
            }
        }
        return sortedByName(result)
    }

    /// Returns the direct dependencies of the given package, sorted alphabetically by name.
    internal fun directDependencies(of: SBOMPackage): Array<SBOMPackage> {
        val package_ = of
        val id_0 = package_.SPDXID
        if (id_0 == null) {
            return arrayOf()
        }
        return directDependencies(ofSPDXID = id_0)
    }

    /// The top-level dependency packages: the packages directly depended on by the document's
    /// root package via the `DEPENDS_ON` relationship, sorted alphabetically by name.
    ///
    /// If the document does not contain a `DESCRIBES` relationship or any `DEPENDS_ON`
    /// relationships from the root, this falls back to `dependencyPackages` so the
    /// hierarchy view still has something useful to display.
    internal val topLevelPackages: Array<SBOMPackage>
        get() {
            val rootId_0 = rootPackageSPDXID
            if (rootId_0 == null) {
                return dependencyPackages
            }
            val direct = directDependencies(ofSPDXID = rootId_0)
            if (direct.isEmpty) {
                return dependencyPackages
            }
            return direct
        }

    /// Looks up an extracted license by its `LicenseRef-…` identifier.
    internal fun extractedLicense(forId: String): SBOMExtractedLicense? {
        val licenseId = forId
        val infos_0 = hasExtractedLicensingInfos.sref()
        if (infos_0 == null) {
            return null
        }
        for (info in infos_0.sref()) {
            if (info.licenseId == licenseId) {
                return info.sref()
            }
        }
        return null
    }

    /// Sorts an array of packages alphabetically by `name`, case-insensitively. Packages
    /// without a name are pushed to the end of the list.
    private fun sortedByName(pkgs: Array<SBOMPackage>): Array<SBOMPackage> {
        return pkgs.sorted l@{ lhs, rhs ->
            val l = (lhs.name ?: "~").lowercased()
            val r = (rhs.name ?: "~").lowercased()
            return@l l < r
        }
    }

    private constructor(copy: MutableStruct) {
        @Suppress("NAME_SHADOWING", "UNCHECKED_CAST") val copy = copy as SBOMDocument
        this.spdxVersion = copy.spdxVersion
        this.SPDXID = copy.SPDXID
        this.dataLicense = copy.dataLicense
        this.name = copy.name
        this.documentNamespace = copy.documentNamespace
        this.creationInfo = copy.creationInfo
        this.packages = copy.packages
        this.relationships = copy.relationships
        this.hasExtractedLicensingInfos = copy.hasExtractedLicensingInfos
    }

    override var supdate: ((Any) -> Unit)? = null
    override var smutatingcount = 0
    override fun scopy(): MutableStruct = SBOMDocument(this as MutableStruct)

    override fun equals(other: Any?): Boolean {
        if (other !is SBOMDocument) return false
        return spdxVersion == other.spdxVersion && SPDXID == other.SPDXID && dataLicense == other.dataLicense && name == other.name && documentNamespace == other.documentNamespace && creationInfo == other.creationInfo && packages == other.packages && relationships == other.relationships && hasExtractedLicensingInfos == other.hasExtractedLicensingInfos
    }

    override fun hashCode(): Int {
        var result = 1
        result = Hasher.combine(result, spdxVersion)
        result = Hasher.combine(result, SPDXID)
        result = Hasher.combine(result, dataLicense)
        result = Hasher.combine(result, name)
        result = Hasher.combine(result, documentNamespace)
        result = Hasher.combine(result, creationInfo)
        result = Hasher.combine(result, packages)
        result = Hasher.combine(result, relationships)
        result = Hasher.combine(result, hasExtractedLicensingInfos)
        return result
    }

    private enum class CodingKeys(override val rawValue: String, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): CodingKey, RawRepresentable<String> {
        spdxVersion("spdxVersion"),
        SPDXID("SPDXID"),
        dataLicense("dataLicense"),
        name_("name"),
        documentNamespace("documentNamespace"),
        creationInfo("creationInfo"),
        packages("packages"),
        relationships("relationships"),
        hasExtractedLicensingInfos("hasExtractedLicensingInfos");

        @androidx.annotation.Keep
        companion object {
            fun init(rawValue: String): CodingKeys? {
                return when (rawValue) {
                    "spdxVersion" -> CodingKeys.spdxVersion
                    "SPDXID" -> CodingKeys.SPDXID
                    "dataLicense" -> CodingKeys.dataLicense
                    "name" -> CodingKeys.name_
                    "documentNamespace" -> CodingKeys.documentNamespace
                    "creationInfo" -> CodingKeys.creationInfo
                    "packages" -> CodingKeys.packages
                    "relationships" -> CodingKeys.relationships
                    "hasExtractedLicensingInfos" -> CodingKeys.hasExtractedLicensingInfos
                    else -> null
                }
            }
        }
    }

    override fun encode(to: Encoder) {
        val container = to.container(keyedBy = CodingKeys::class)
        container.encodeIfPresent(spdxVersion, forKey = CodingKeys.spdxVersion)
        container.encodeIfPresent(SPDXID, forKey = CodingKeys.SPDXID)
        container.encodeIfPresent(dataLicense, forKey = CodingKeys.dataLicense)
        container.encodeIfPresent(name, forKey = CodingKeys.name_)
        container.encodeIfPresent(documentNamespace, forKey = CodingKeys.documentNamespace)
        container.encodeIfPresent(creationInfo, forKey = CodingKeys.creationInfo)
        container.encode(packages, forKey = CodingKeys.packages)
        container.encodeIfPresent(relationships, forKey = CodingKeys.relationships)
        container.encodeIfPresent(hasExtractedLicensingInfos, forKey = CodingKeys.hasExtractedLicensingInfos)
    }

    constructor(from: Decoder) {
        val container = from.container(keyedBy = CodingKeys::class)
        this.spdxVersion = container.decodeIfPresent(String::class, forKey = CodingKeys.spdxVersion)
        this.SPDXID = container.decodeIfPresent(String::class, forKey = CodingKeys.SPDXID)
        this.dataLicense = container.decodeIfPresent(String::class, forKey = CodingKeys.dataLicense)
        this.name = container.decodeIfPresent(String::class, forKey = CodingKeys.name_)
        this.documentNamespace = container.decodeIfPresent(String::class, forKey = CodingKeys.documentNamespace)
        this.creationInfo = container.decodeIfPresent(SBOMCreationInfo::class, forKey = CodingKeys.creationInfo)
        this.packages = container.decode(Array::class, elementType = SBOMPackage::class, forKey = CodingKeys.packages)
        this.relationships = container.decodeIfPresent(Array::class, elementType = SBOMRelationship::class, forKey = CodingKeys.relationships)
        this.hasExtractedLicensingInfos = container.decodeIfPresent(Array::class, elementType = SBOMExtractedLicense::class, forKey = CodingKeys.hasExtractedLicensingInfos)
    }

    @androidx.annotation.Keep
    companion object: DecodableCompanion<SBOMDocument> {

        /// The default resource name for the SBOM appropriate for the current platform
        /// (`sbom-darwin-ios.spdx` on Apple platforms, `sbom-linux-android.spdx` on Android).
        internal val defaultResourceName: String
            get() = sbomLinuxAndroidResourceName

        /// Loads the SBOM document for the current platform from the given bundle, if present.
        ///
        /// On Apple platforms this looks for `sbom-darwin-ios.spdx.json`. On Android, it looks
        /// for `sbom-linux-android.spdx.json`.
        ///
        /// - Parameter bundle: The bundle containing the SBOM resource.
        /// - Returns: The parsed `SBOMDocument`, or `nil` if no SBOM resource is present in the bundle.
        /// - Throws: A decoding error if the resource exists but cannot be parsed as SPDX JSON.
        internal fun load(from: Bundle): SBOMDocument? {
            val bundle = from
            val url_0 = bundle.url(forResource = defaultResourceName, withExtension = sbomResourceExtension)
            if (url_0 == null) {
                return null
            }
            return load(from = url_0)
        }

        /// Loads and parses an SPDX JSON SBOM document from the given file URL.
        internal fun load(from: URL): SBOMDocument {
            val url = from
            val data = Data(contentsOf = url)
            return parse(data = data)
        }

        /// Parses an SPDX JSON SBOM document from raw `Data`.
        internal fun parse(data: Data): SBOMDocument {
            val decoder = JSONDecoder()
            return decoder.decode(SBOMDocument::class, from = data)
        }

        /// Returns the raw bytes of the SBOM resource for the current platform from the given
        /// bundle, if present. Useful for sharing the file via the system share sheet without
        /// re-encoding it.
        internal fun rawData(from: Bundle): Data? {
            val bundle = from
            val url_1 = bundle.url(forResource = defaultResourceName, withExtension = sbomResourceExtension)
            if (url_1 == null) {
                return null
            }
            return try { Data(contentsOf = url_1) } catch (_: Throwable) { null }
        }

        /// Returns the URL of the SBOM resource for the current platform from the given
        /// bundle, if present.
        internal fun resourceURL(in_: Bundle): URL? {
            val bundle = in_
            return bundle.url(forResource = defaultResourceName, withExtension = sbomResourceExtension)
        }

        /// Returns `true` if the given bundle contains an SBOM resource for the current platform.
        internal fun bundleContainsSBOM(bundle: Bundle): Boolean = resourceURL(in_ = bundle) != null

        override fun init(from: Decoder): SBOMDocument = SBOMDocument(from = from)

        private fun CodingKeys(rawValue: String): CodingKeys? = CodingKeys.init(rawValue = rawValue)
    }
}

/// Metadata about how the SBOM was generated.
@androidx.annotation.Keep
internal class SBOMCreationInfo: Codable, MutableStruct {
    /// ISO 8601 timestamp of when the document was created.
    internal var created: String? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    /// The tools and/or organizations that created this document.
    internal var creators: Array<String>? = null
        get() = field.sref({ this.creators = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    /// The version of the SPDX license list used.
    internal var licenseListVersion: String? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }

    internal constructor(created: String? = null, creators: Array<String>? = null, licenseListVersion: String? = null) {
        this.created = created
        this.creators = creators
        this.licenseListVersion = licenseListVersion
    }

    private constructor(copy: MutableStruct) {
        @Suppress("NAME_SHADOWING", "UNCHECKED_CAST") val copy = copy as SBOMCreationInfo
        this.created = copy.created
        this.creators = copy.creators
        this.licenseListVersion = copy.licenseListVersion
    }

    override var supdate: ((Any) -> Unit)? = null
    override var smutatingcount = 0
    override fun scopy(): MutableStruct = SBOMCreationInfo(this as MutableStruct)

    override fun equals(other: Any?): Boolean {
        if (other !is SBOMCreationInfo) return false
        return created == other.created && creators == other.creators && licenseListVersion == other.licenseListVersion
    }

    override fun hashCode(): Int {
        var result = 1
        result = Hasher.combine(result, created)
        result = Hasher.combine(result, creators)
        result = Hasher.combine(result, licenseListVersion)
        return result
    }

    private enum class CodingKeys(override val rawValue: String, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): CodingKey, RawRepresentable<String> {
        created("created"),
        creators("creators"),
        licenseListVersion("licenseListVersion");

        @androidx.annotation.Keep
        companion object {
            fun init(rawValue: String): CodingKeys? {
                return when (rawValue) {
                    "created" -> CodingKeys.created
                    "creators" -> CodingKeys.creators
                    "licenseListVersion" -> CodingKeys.licenseListVersion
                    else -> null
                }
            }
        }
    }

    override fun encode(to: Encoder) {
        val container = to.container(keyedBy = CodingKeys::class)
        container.encodeIfPresent(created, forKey = CodingKeys.created)
        container.encodeIfPresent(creators, forKey = CodingKeys.creators)
        container.encodeIfPresent(licenseListVersion, forKey = CodingKeys.licenseListVersion)
    }

    constructor(from: Decoder) {
        val container = from.container(keyedBy = CodingKeys::class)
        this.created = container.decodeIfPresent(String::class, forKey = CodingKeys.created)
        this.creators = container.decodeIfPresent(Array::class, elementType = String::class, forKey = CodingKeys.creators)
        this.licenseListVersion = container.decodeIfPresent(String::class, forKey = CodingKeys.licenseListVersion)
    }

    @androidx.annotation.Keep
    companion object: DecodableCompanion<SBOMCreationInfo> {
        override fun init(from: Decoder): SBOMCreationInfo = SBOMCreationInfo(from = from)

        private fun CodingKeys(rawValue: String): CodingKeys? = CodingKeys.init(rawValue = rawValue)
    }
}

/// A single package (dependency) tracked in the SBOM.
@androidx.annotation.Keep
internal class SBOMPackage: Codable, Identifiable<String>, MutableStruct {
    /// The SPDX identifier for this package.
    internal var SPDXID: String? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    /// Human-readable package name.
    internal var name: String? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    /// Version string for the package.
    internal var versionInfo: String? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    /// Supplier (organization or person) that distributes the package.
    internal var supplier: String? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    /// Originator (the party that originally created the package).
    internal var originator: String? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    /// URL or other locator describing where the package can be downloaded.
    internal var downloadLocation: String? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    /// Free-form description of the package.
    internal var description: String? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    /// A short summary of the package.
    internal var summary: String? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    /// The package homepage.
    internal var homepage: String? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    /// The license that the SBOM author concluded applies (after analysis).
    internal var licenseConcluded: String? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    /// The license that the package author declared in the package.
    internal var licenseDeclared: String? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    /// Additional comments about the license.
    internal var licenseComments: String? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    /// Copyright notices declared by the package.
    internal var copyrightText: String? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    /// What this package is for (e.g., `LIBRARY`, `APPLICATION`).
    internal var primaryPackagePurpose: String? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    /// Source-info string (often from the gradle plugin).
    internal var sourceInfo: String? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    /// Whether the file contents of the package were analyzed.
    internal var filesAnalyzed: Boolean? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    /// Cryptographic checksums for the package archive.
    internal var checksums: Array<SBOMChecksum>? = null
        get() = field.sref({ this.checksums = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    /// External references such as `purl` (package URL) and SwiftPM repository URLs.
    internal var externalRefs: Array<SBOMExternalRef>? = null
        get() = field.sref({ this.externalRefs = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    /// License information extracted from the package's files.
    internal var licenseInfoFromFiles: Array<String>? = null
        get() = field.sref({ this.licenseInfoFromFiles = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }

    override val id: String
        get() = SPDXID ?: (name ?: "")

    internal constructor(SPDXID: String? = null, name: String? = null, versionInfo: String? = null, supplier: String? = null, originator: String? = null, downloadLocation: String? = null, description: String? = null, summary: String? = null, homepage: String? = null, licenseConcluded: String? = null, licenseDeclared: String? = null, licenseComments: String? = null, copyrightText: String? = null, primaryPackagePurpose: String? = null, sourceInfo: String? = null, filesAnalyzed: Boolean? = null, checksums: Array<SBOMChecksum>? = null, externalRefs: Array<SBOMExternalRef>? = null, licenseInfoFromFiles: Array<String>? = null) {
        this.SPDXID = SPDXID
        this.name = name
        this.versionInfo = versionInfo
        this.supplier = supplier
        this.originator = originator
        this.downloadLocation = downloadLocation
        this.description = description
        this.summary = summary
        this.homepage = homepage
        this.licenseConcluded = licenseConcluded
        this.licenseDeclared = licenseDeclared
        this.licenseComments = licenseComments
        this.copyrightText = copyrightText
        this.primaryPackagePurpose = primaryPackagePurpose
        this.sourceInfo = sourceInfo
        this.filesAnalyzed = filesAnalyzed
        this.checksums = checksums
        this.externalRefs = externalRefs
        this.licenseInfoFromFiles = licenseInfoFromFiles
    }

    private constructor(copy: MutableStruct) {
        @Suppress("NAME_SHADOWING", "UNCHECKED_CAST") val copy = copy as SBOMPackage
        this.SPDXID = copy.SPDXID
        this.name = copy.name
        this.versionInfo = copy.versionInfo
        this.supplier = copy.supplier
        this.originator = copy.originator
        this.downloadLocation = copy.downloadLocation
        this.description = copy.description
        this.summary = copy.summary
        this.homepage = copy.homepage
        this.licenseConcluded = copy.licenseConcluded
        this.licenseDeclared = copy.licenseDeclared
        this.licenseComments = copy.licenseComments
        this.copyrightText = copy.copyrightText
        this.primaryPackagePurpose = copy.primaryPackagePurpose
        this.sourceInfo = copy.sourceInfo
        this.filesAnalyzed = copy.filesAnalyzed
        this.checksums = copy.checksums
        this.externalRefs = copy.externalRefs
        this.licenseInfoFromFiles = copy.licenseInfoFromFiles
    }

    override var supdate: ((Any) -> Unit)? = null
    override var smutatingcount = 0
    override fun scopy(): MutableStruct = SBOMPackage(this as MutableStruct)

    override fun equals(other: Any?): Boolean {
        if (other !is SBOMPackage) return false
        return SPDXID == other.SPDXID && name == other.name && versionInfo == other.versionInfo && supplier == other.supplier && originator == other.originator && downloadLocation == other.downloadLocation && description == other.description && summary == other.summary && homepage == other.homepage && licenseConcluded == other.licenseConcluded && licenseDeclared == other.licenseDeclared && licenseComments == other.licenseComments && copyrightText == other.copyrightText && primaryPackagePurpose == other.primaryPackagePurpose && sourceInfo == other.sourceInfo && filesAnalyzed == other.filesAnalyzed && checksums == other.checksums && externalRefs == other.externalRefs && licenseInfoFromFiles == other.licenseInfoFromFiles
    }

    override fun hashCode(): Int {
        var result = 1
        result = Hasher.combine(result, SPDXID)
        result = Hasher.combine(result, name)
        result = Hasher.combine(result, versionInfo)
        result = Hasher.combine(result, supplier)
        result = Hasher.combine(result, originator)
        result = Hasher.combine(result, downloadLocation)
        result = Hasher.combine(result, description)
        result = Hasher.combine(result, summary)
        result = Hasher.combine(result, homepage)
        result = Hasher.combine(result, licenseConcluded)
        result = Hasher.combine(result, licenseDeclared)
        result = Hasher.combine(result, licenseComments)
        result = Hasher.combine(result, copyrightText)
        result = Hasher.combine(result, primaryPackagePurpose)
        result = Hasher.combine(result, sourceInfo)
        result = Hasher.combine(result, filesAnalyzed)
        result = Hasher.combine(result, checksums)
        result = Hasher.combine(result, externalRefs)
        result = Hasher.combine(result, licenseInfoFromFiles)
        return result
    }

    private enum class CodingKeys(override val rawValue: String, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): CodingKey, RawRepresentable<String> {
        SPDXID("SPDXID"),
        name_("name"),
        versionInfo("versionInfo"),
        supplier("supplier"),
        originator("originator"),
        downloadLocation("downloadLocation"),
        description_("description"),
        summary("summary"),
        homepage("homepage"),
        licenseConcluded("licenseConcluded"),
        licenseDeclared("licenseDeclared"),
        licenseComments("licenseComments"),
        copyrightText("copyrightText"),
        primaryPackagePurpose("primaryPackagePurpose"),
        sourceInfo("sourceInfo"),
        filesAnalyzed("filesAnalyzed"),
        checksums("checksums"),
        externalRefs("externalRefs"),
        licenseInfoFromFiles("licenseInfoFromFiles");

        @androidx.annotation.Keep
        companion object {
            fun init(rawValue: String): CodingKeys? {
                return when (rawValue) {
                    "SPDXID" -> CodingKeys.SPDXID
                    "name" -> CodingKeys.name_
                    "versionInfo" -> CodingKeys.versionInfo
                    "supplier" -> CodingKeys.supplier
                    "originator" -> CodingKeys.originator
                    "downloadLocation" -> CodingKeys.downloadLocation
                    "description" -> CodingKeys.description_
                    "summary" -> CodingKeys.summary
                    "homepage" -> CodingKeys.homepage
                    "licenseConcluded" -> CodingKeys.licenseConcluded
                    "licenseDeclared" -> CodingKeys.licenseDeclared
                    "licenseComments" -> CodingKeys.licenseComments
                    "copyrightText" -> CodingKeys.copyrightText
                    "primaryPackagePurpose" -> CodingKeys.primaryPackagePurpose
                    "sourceInfo" -> CodingKeys.sourceInfo
                    "filesAnalyzed" -> CodingKeys.filesAnalyzed
                    "checksums" -> CodingKeys.checksums
                    "externalRefs" -> CodingKeys.externalRefs
                    "licenseInfoFromFiles" -> CodingKeys.licenseInfoFromFiles
                    else -> null
                }
            }
        }
    }

    override fun encode(to: Encoder) {
        val container = to.container(keyedBy = CodingKeys::class)
        container.encodeIfPresent(SPDXID, forKey = CodingKeys.SPDXID)
        container.encodeIfPresent(name, forKey = CodingKeys.name_)
        container.encodeIfPresent(versionInfo, forKey = CodingKeys.versionInfo)
        container.encodeIfPresent(supplier, forKey = CodingKeys.supplier)
        container.encodeIfPresent(originator, forKey = CodingKeys.originator)
        container.encodeIfPresent(downloadLocation, forKey = CodingKeys.downloadLocation)
        container.encodeIfPresent(description, forKey = CodingKeys.description_)
        container.encodeIfPresent(summary, forKey = CodingKeys.summary)
        container.encodeIfPresent(homepage, forKey = CodingKeys.homepage)
        container.encodeIfPresent(licenseConcluded, forKey = CodingKeys.licenseConcluded)
        container.encodeIfPresent(licenseDeclared, forKey = CodingKeys.licenseDeclared)
        container.encodeIfPresent(licenseComments, forKey = CodingKeys.licenseComments)
        container.encodeIfPresent(copyrightText, forKey = CodingKeys.copyrightText)
        container.encodeIfPresent(primaryPackagePurpose, forKey = CodingKeys.primaryPackagePurpose)
        container.encodeIfPresent(sourceInfo, forKey = CodingKeys.sourceInfo)
        container.encodeIfPresent(filesAnalyzed, forKey = CodingKeys.filesAnalyzed)
        container.encodeIfPresent(checksums, forKey = CodingKeys.checksums)
        container.encodeIfPresent(externalRefs, forKey = CodingKeys.externalRefs)
        container.encodeIfPresent(licenseInfoFromFiles, forKey = CodingKeys.licenseInfoFromFiles)
    }

    constructor(from: Decoder) {
        val container = from.container(keyedBy = CodingKeys::class)
        this.SPDXID = container.decodeIfPresent(String::class, forKey = CodingKeys.SPDXID)
        this.name = container.decodeIfPresent(String::class, forKey = CodingKeys.name_)
        this.versionInfo = container.decodeIfPresent(String::class, forKey = CodingKeys.versionInfo)
        this.supplier = container.decodeIfPresent(String::class, forKey = CodingKeys.supplier)
        this.originator = container.decodeIfPresent(String::class, forKey = CodingKeys.originator)
        this.downloadLocation = container.decodeIfPresent(String::class, forKey = CodingKeys.downloadLocation)
        this.description = container.decodeIfPresent(String::class, forKey = CodingKeys.description_)
        this.summary = container.decodeIfPresent(String::class, forKey = CodingKeys.summary)
        this.homepage = container.decodeIfPresent(String::class, forKey = CodingKeys.homepage)
        this.licenseConcluded = container.decodeIfPresent(String::class, forKey = CodingKeys.licenseConcluded)
        this.licenseDeclared = container.decodeIfPresent(String::class, forKey = CodingKeys.licenseDeclared)
        this.licenseComments = container.decodeIfPresent(String::class, forKey = CodingKeys.licenseComments)
        this.copyrightText = container.decodeIfPresent(String::class, forKey = CodingKeys.copyrightText)
        this.primaryPackagePurpose = container.decodeIfPresent(String::class, forKey = CodingKeys.primaryPackagePurpose)
        this.sourceInfo = container.decodeIfPresent(String::class, forKey = CodingKeys.sourceInfo)
        this.filesAnalyzed = container.decodeIfPresent(Boolean::class, forKey = CodingKeys.filesAnalyzed)
        this.checksums = container.decodeIfPresent(Array::class, elementType = SBOMChecksum::class, forKey = CodingKeys.checksums)
        this.externalRefs = container.decodeIfPresent(Array::class, elementType = SBOMExternalRef::class, forKey = CodingKeys.externalRefs)
        this.licenseInfoFromFiles = container.decodeIfPresent(Array::class, elementType = String::class, forKey = CodingKeys.licenseInfoFromFiles)
    }

    @androidx.annotation.Keep
    companion object: DecodableCompanion<SBOMPackage> {
        override fun init(from: Decoder): SBOMPackage = SBOMPackage(from = from)

        private fun CodingKeys(rawValue: String): CodingKeys? = CodingKeys.init(rawValue = rawValue)
    }
}

/// A cryptographic checksum entry on a package.
@androidx.annotation.Keep
internal class SBOMChecksum: Codable, MutableStruct {
    /// Hash algorithm name (e.g., `SHA1`, `SHA256`).
    internal var algorithm: String? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    /// The hex-encoded checksum value.
    internal var checksumValue: String? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }

    internal constructor(algorithm: String? = null, checksumValue: String? = null) {
        this.algorithm = algorithm
        this.checksumValue = checksumValue
    }

    private constructor(copy: MutableStruct) {
        @Suppress("NAME_SHADOWING", "UNCHECKED_CAST") val copy = copy as SBOMChecksum
        this.algorithm = copy.algorithm
        this.checksumValue = copy.checksumValue
    }

    override var supdate: ((Any) -> Unit)? = null
    override var smutatingcount = 0
    override fun scopy(): MutableStruct = SBOMChecksum(this as MutableStruct)

    override fun equals(other: Any?): Boolean {
        if (other !is SBOMChecksum) return false
        return algorithm == other.algorithm && checksumValue == other.checksumValue
    }

    override fun hashCode(): Int {
        var result = 1
        result = Hasher.combine(result, algorithm)
        result = Hasher.combine(result, checksumValue)
        return result
    }

    private enum class CodingKeys(override val rawValue: String, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): CodingKey, RawRepresentable<String> {
        algorithm("algorithm"),
        checksumValue("checksumValue");

        @androidx.annotation.Keep
        companion object {
            fun init(rawValue: String): CodingKeys? {
                return when (rawValue) {
                    "algorithm" -> CodingKeys.algorithm
                    "checksumValue" -> CodingKeys.checksumValue
                    else -> null
                }
            }
        }
    }

    override fun encode(to: Encoder) {
        val container = to.container(keyedBy = CodingKeys::class)
        container.encodeIfPresent(algorithm, forKey = CodingKeys.algorithm)
        container.encodeIfPresent(checksumValue, forKey = CodingKeys.checksumValue)
    }

    constructor(from: Decoder) {
        val container = from.container(keyedBy = CodingKeys::class)
        this.algorithm = container.decodeIfPresent(String::class, forKey = CodingKeys.algorithm)
        this.checksumValue = container.decodeIfPresent(String::class, forKey = CodingKeys.checksumValue)
    }

    @androidx.annotation.Keep
    companion object: DecodableCompanion<SBOMChecksum> {
        override fun init(from: Decoder): SBOMChecksum = SBOMChecksum(from = from)

        private fun CodingKeys(rawValue: String): CodingKeys? = CodingKeys.init(rawValue = rawValue)
    }
}

/// An external reference attached to a package, such as a Package URL (`purl`) or SwiftPM repository.
@androidx.annotation.Keep
internal class SBOMExternalRef: Codable, MutableStruct {
    /// Category (e.g., `PACKAGE-MANAGER`, `SECURITY`).
    internal var referenceCategory: String? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    /// The locator string (the meaning depends on `referenceType`).
    internal var referenceLocator: String? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    /// The type of reference (e.g., `purl`, `swiftpm`).
    internal var referenceType: String? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }

    internal constructor(referenceCategory: String? = null, referenceLocator: String? = null, referenceType: String? = null) {
        this.referenceCategory = referenceCategory
        this.referenceLocator = referenceLocator
        this.referenceType = referenceType
    }

    private constructor(copy: MutableStruct) {
        @Suppress("NAME_SHADOWING", "UNCHECKED_CAST") val copy = copy as SBOMExternalRef
        this.referenceCategory = copy.referenceCategory
        this.referenceLocator = copy.referenceLocator
        this.referenceType = copy.referenceType
    }

    override var supdate: ((Any) -> Unit)? = null
    override var smutatingcount = 0
    override fun scopy(): MutableStruct = SBOMExternalRef(this as MutableStruct)

    override fun equals(other: Any?): Boolean {
        if (other !is SBOMExternalRef) return false
        return referenceCategory == other.referenceCategory && referenceLocator == other.referenceLocator && referenceType == other.referenceType
    }

    override fun hashCode(): Int {
        var result = 1
        result = Hasher.combine(result, referenceCategory)
        result = Hasher.combine(result, referenceLocator)
        result = Hasher.combine(result, referenceType)
        return result
    }

    private enum class CodingKeys(override val rawValue: String, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): CodingKey, RawRepresentable<String> {
        referenceCategory("referenceCategory"),
        referenceLocator("referenceLocator"),
        referenceType("referenceType");

        @androidx.annotation.Keep
        companion object {
            fun init(rawValue: String): CodingKeys? {
                return when (rawValue) {
                    "referenceCategory" -> CodingKeys.referenceCategory
                    "referenceLocator" -> CodingKeys.referenceLocator
                    "referenceType" -> CodingKeys.referenceType
                    else -> null
                }
            }
        }
    }

    override fun encode(to: Encoder) {
        val container = to.container(keyedBy = CodingKeys::class)
        container.encodeIfPresent(referenceCategory, forKey = CodingKeys.referenceCategory)
        container.encodeIfPresent(referenceLocator, forKey = CodingKeys.referenceLocator)
        container.encodeIfPresent(referenceType, forKey = CodingKeys.referenceType)
    }

    constructor(from: Decoder) {
        val container = from.container(keyedBy = CodingKeys::class)
        this.referenceCategory = container.decodeIfPresent(String::class, forKey = CodingKeys.referenceCategory)
        this.referenceLocator = container.decodeIfPresent(String::class, forKey = CodingKeys.referenceLocator)
        this.referenceType = container.decodeIfPresent(String::class, forKey = CodingKeys.referenceType)
    }

    @androidx.annotation.Keep
    companion object: DecodableCompanion<SBOMExternalRef> {
        override fun init(from: Decoder): SBOMExternalRef = SBOMExternalRef(from = from)

        private fun CodingKeys(rawValue: String): CodingKeys? = CodingKeys.init(rawValue = rawValue)
    }
}

/// A relationship between two SPDX elements (e.g., `A DEPENDS_ON B`).
@androidx.annotation.Keep
internal class SBOMRelationship: Codable, MutableStruct {
    internal var spdxElementId: String? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var relatedSpdxElement: String? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var relationshipType: String? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }

    internal constructor(spdxElementId: String? = null, relatedSpdxElement: String? = null, relationshipType: String? = null) {
        this.spdxElementId = spdxElementId
        this.relatedSpdxElement = relatedSpdxElement
        this.relationshipType = relationshipType
    }

    private constructor(copy: MutableStruct) {
        @Suppress("NAME_SHADOWING", "UNCHECKED_CAST") val copy = copy as SBOMRelationship
        this.spdxElementId = copy.spdxElementId
        this.relatedSpdxElement = copy.relatedSpdxElement
        this.relationshipType = copy.relationshipType
    }

    override var supdate: ((Any) -> Unit)? = null
    override var smutatingcount = 0
    override fun scopy(): MutableStruct = SBOMRelationship(this as MutableStruct)

    override fun equals(other: Any?): Boolean {
        if (other !is SBOMRelationship) return false
        return spdxElementId == other.spdxElementId && relatedSpdxElement == other.relatedSpdxElement && relationshipType == other.relationshipType
    }

    override fun hashCode(): Int {
        var result = 1
        result = Hasher.combine(result, spdxElementId)
        result = Hasher.combine(result, relatedSpdxElement)
        result = Hasher.combine(result, relationshipType)
        return result
    }

    private enum class CodingKeys(override val rawValue: String, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): CodingKey, RawRepresentable<String> {
        spdxElementId("spdxElementId"),
        relatedSpdxElement("relatedSpdxElement"),
        relationshipType("relationshipType");

        @androidx.annotation.Keep
        companion object {
            fun init(rawValue: String): CodingKeys? {
                return when (rawValue) {
                    "spdxElementId" -> CodingKeys.spdxElementId
                    "relatedSpdxElement" -> CodingKeys.relatedSpdxElement
                    "relationshipType" -> CodingKeys.relationshipType
                    else -> null
                }
            }
        }
    }

    override fun encode(to: Encoder) {
        val container = to.container(keyedBy = CodingKeys::class)
        container.encodeIfPresent(spdxElementId, forKey = CodingKeys.spdxElementId)
        container.encodeIfPresent(relatedSpdxElement, forKey = CodingKeys.relatedSpdxElement)
        container.encodeIfPresent(relationshipType, forKey = CodingKeys.relationshipType)
    }

    constructor(from: Decoder) {
        val container = from.container(keyedBy = CodingKeys::class)
        this.spdxElementId = container.decodeIfPresent(String::class, forKey = CodingKeys.spdxElementId)
        this.relatedSpdxElement = container.decodeIfPresent(String::class, forKey = CodingKeys.relatedSpdxElement)
        this.relationshipType = container.decodeIfPresent(String::class, forKey = CodingKeys.relationshipType)
    }

    @androidx.annotation.Keep
    companion object: DecodableCompanion<SBOMRelationship> {
        override fun init(from: Decoder): SBOMRelationship = SBOMRelationship(from = from)

        private fun CodingKeys(rawValue: String): CodingKeys? = CodingKeys.init(rawValue = rawValue)
    }
}

/// A custom license definition for licenses that are not on the SPDX License List.
/// Identified by `LicenseRef-…` rather than a standard SPDX identifier.
@androidx.annotation.Keep
internal class SBOMExtractedLicense: Codable, MutableStruct {
    /// The `LicenseRef-…` identifier used to refer to this license.
    internal var licenseId: String? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    /// The full text of the license.
    internal var extractedText: String? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    /// A human-readable name for the license.
    internal var name: String? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    /// URLs where the license text or more information can be found.
    internal var seeAlsos: Array<String>? = null
        get() = field.sref({ this.seeAlsos = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }

    internal constructor(licenseId: String? = null, extractedText: String? = null, name: String? = null, seeAlsos: Array<String>? = null) {
        this.licenseId = licenseId
        this.extractedText = extractedText
        this.name = name
        this.seeAlsos = seeAlsos
    }

    private constructor(copy: MutableStruct) {
        @Suppress("NAME_SHADOWING", "UNCHECKED_CAST") val copy = copy as SBOMExtractedLicense
        this.licenseId = copy.licenseId
        this.extractedText = copy.extractedText
        this.name = copy.name
        this.seeAlsos = copy.seeAlsos
    }

    override var supdate: ((Any) -> Unit)? = null
    override var smutatingcount = 0
    override fun scopy(): MutableStruct = SBOMExtractedLicense(this as MutableStruct)

    override fun equals(other: Any?): Boolean {
        if (other !is SBOMExtractedLicense) return false
        return licenseId == other.licenseId && extractedText == other.extractedText && name == other.name && seeAlsos == other.seeAlsos
    }

    override fun hashCode(): Int {
        var result = 1
        result = Hasher.combine(result, licenseId)
        result = Hasher.combine(result, extractedText)
        result = Hasher.combine(result, name)
        result = Hasher.combine(result, seeAlsos)
        return result
    }

    private enum class CodingKeys(override val rawValue: String, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): CodingKey, RawRepresentable<String> {
        licenseId("licenseId"),
        extractedText("extractedText"),
        name_("name"),
        seeAlsos("seeAlsos");

        @androidx.annotation.Keep
        companion object {
            fun init(rawValue: String): CodingKeys? {
                return when (rawValue) {
                    "licenseId" -> CodingKeys.licenseId
                    "extractedText" -> CodingKeys.extractedText
                    "name" -> CodingKeys.name_
                    "seeAlsos" -> CodingKeys.seeAlsos
                    else -> null
                }
            }
        }
    }

    override fun encode(to: Encoder) {
        val container = to.container(keyedBy = CodingKeys::class)
        container.encodeIfPresent(licenseId, forKey = CodingKeys.licenseId)
        container.encodeIfPresent(extractedText, forKey = CodingKeys.extractedText)
        container.encodeIfPresent(name, forKey = CodingKeys.name_)
        container.encodeIfPresent(seeAlsos, forKey = CodingKeys.seeAlsos)
    }

    constructor(from: Decoder) {
        val container = from.container(keyedBy = CodingKeys::class)
        this.licenseId = container.decodeIfPresent(String::class, forKey = CodingKeys.licenseId)
        this.extractedText = container.decodeIfPresent(String::class, forKey = CodingKeys.extractedText)
        this.name = container.decodeIfPresent(String::class, forKey = CodingKeys.name_)
        this.seeAlsos = container.decodeIfPresent(Array::class, elementType = String::class, forKey = CodingKeys.seeAlsos)
    }

    @androidx.annotation.Keep
    companion object: DecodableCompanion<SBOMExtractedLicense> {
        override fun init(from: Decoder): SBOMExtractedLicense = SBOMExtractedLicense(from = from)

        private fun CodingKeys(rawValue: String): CodingKeys? = CodingKeys.init(rawValue = rawValue)
    }
}

// MARK: - Loading

/// The standard resource name (without extension) for the iOS/Darwin SPDX SBOM file.
internal val sbomDarwinResourceName = "sbom-darwin-ios.spdx"
/// The standard resource name (without extension) for the Android/Linux SPDX SBOM file.
internal val sbomLinuxAndroidResourceName = "sbom-linux-android.spdx"
/// The file extension for SPDX SBOM files.
internal val sbomResourceExtension = "json"

/// Controls how `SBOMView` presents the bundled software dependencies.
@androidx.annotation.Keep
enum class SBOMDisplayMode(override val rawValue: String, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): RawRepresentable<String>, skip.lib.SwiftProjecting {
    /// Show only the top-level dependencies (those directly depended on by the
    /// document's root package via the `DEPENDS_ON` relationship). The detail view
    /// for each package then lists its own direct dependencies, allowing the user
    /// to navigate the dependency tree.
    hierarchy("hierarchy"),
    /// Show every dependency package in the SBOM as a single flat, alphabetised list.
    flat("flat");

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {
        fun init(rawValue: String): SBOMDisplayMode? {
            return when (rawValue) {
                "hierarchy" -> SBOMDisplayMode.hierarchy
                "flat" -> SBOMDisplayMode.flat
                else -> null
            }
        }
    }
}

fun SBOMDisplayMode(rawValue: String): SBOMDisplayMode? = SBOMDisplayMode.init(rawValue = rawValue)

// MARK: - License helpers

/// Helpers for working with SPDX license identifiers.
internal enum class SPDXLicense {
    ;

    @androidx.annotation.Keep
    companion object {
        /// `NOASSERTION` is the SPDX sentinel meaning "no information was provided".
        internal val noAssertion = "NOASSERTION"
        /// `NONE` is the SPDX sentinel meaning "the field has explicitly no value".
        internal val none = "NONE"

        /// Returns `true` if the given license string is missing or one of the SPDX
        /// "no information" sentinels (`NOASSERTION`, `NONE`).
        internal fun isUnknown(license: String?): Boolean {
            if (license == null) {
                return true
            }
            if (license.isEmpty) {
                return true
            }
            if (license == noAssertion) {
                return true
            }
            if (license == none) {
                return true
            }
            return false
        }

        /// Returns the SPDX license identifier suitable for linking to spdx.org/licenses/,
        /// stripping any compound expressions like `WITH` clauses or `OR`/`AND` operators.
        /// Returns `nil` if no usable identifier can be extracted, or if the identifier is
        /// a `LicenseRef-…` (which is a custom license, not on the SPDX list).
        internal fun canonicalIdentifier(license: String?): String? {
            val raw_0 = license
            if (raw_0 == null) {
                return null
            }
            val trimmed = raw_0.trimmingCharacters(in_ = CharacterSet.whitespacesAndNewlines)
            if (isUnknown(trimmed)) {
                return null
            }
            // Strip enclosing parentheses
            var s = trimmed
            while (s.hasPrefix("(") && s.hasSuffix(")")) {
                s = String(s.dropFirst().dropLast()).trimmingCharacters(in_ = CharacterSet.whitespacesAndNewlines)
            }
            // Take the first identifier from a compound expression like "MIT OR Apache-2.0"
            // or "LGPL-3.0-only WITH LGPL-3.0-linking-exception".
            val separators = arrayOf(" WITH ", " OR ", " AND ", " or ", " and ", " with ")
            var head = s
            for (sep in separators.sref()) {
                head.range(of = sep)?.let { range ->
                    head = String(head[head.startIndex..<range.lowerBound])
                }
            }
            head = head.trimmingCharacters(in_ = CharacterSet.whitespacesAndNewlines)
            if (head.isEmpty) {
                return null
            }
            // LicenseRef-... is not on spdx.org/licenses
            if (head.hasPrefix("LicenseRef-")) {
                return null
            }
            return head
        }

        /// Returns the URL on spdx.org/licenses/ for the given SPDX license identifier,
        /// or `nil` if no canonical SPDX identifier could be extracted.
        internal fun licensePageURL(for_: String?): URL? {
            val license = for_
            val id_1 = canonicalIdentifier(license)
            if (id_1 == null) {
                return null
            }
            return (try { URL(string = "https://spdx.org/licenses/${id_1}.html") } catch (_: NullReturnException) { null })
        }
    }
}
 // !SKIP_BRIDGE // !SKIP_BRIDGE
