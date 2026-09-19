package skip.kit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import skip.lib.*
import skip.lib.Array

// Copyright 2025–2026 Skip
// SPDX-License-Identifier: MPL-2.0
import skip.foundation.*
import skip.ui.*

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import skip.model.*

// MARK: - MailComposerOptions

/// Options for composing an email message.
@androidx.annotation.Keep
@Suppress("MUST_BE_INITIALIZED")
class MailComposerOptions: MutableStruct, skip.lib.SwiftProjecting {
    /// The primary recipient email addresses.
    var recipients: Array<String>
        get() = field.sref({ this.recipients = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    /// Carbon copy recipients.
    var ccRecipients: Array<String>
        get() = field.sref({ this.ccRecipients = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    /// Blind carbon copy recipients.
    var bccRecipients: Array<String>
        get() = field.sref({ this.bccRecipients = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    /// The email subject line.
    var subject: String? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    /// The email body text.
    var body: String? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    /// Whether the body is HTML formatted.
    var isHTML: Boolean
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    /// File attachments. Each attachment specifies a URL, MIME type, and filename.
    var attachments: Array<MailAttachment>
        get() = field.sref({ this.attachments = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }

    constructor(recipients: Array<String> = arrayOf(), ccRecipients: Array<String> = arrayOf(), bccRecipients: Array<String> = arrayOf(), subject: String? = null, body: String? = null, isHTML: Boolean = false, attachments: Array<MailAttachment> = arrayOf()) {
        this.recipients = recipients
        this.ccRecipients = ccRecipients
        this.bccRecipients = bccRecipients
        this.subject = subject
        this.body = body
        this.isHTML = isHTML
        this.attachments = attachments
    }

    private constructor(copy: MutableStruct) {
        @Suppress("NAME_SHADOWING", "UNCHECKED_CAST") val copy = copy as MailComposerOptions
        this.recipients = copy.recipients
        this.ccRecipients = copy.ccRecipients
        this.bccRecipients = copy.bccRecipients
        this.subject = copy.subject
        this.body = copy.body
        this.isHTML = copy.isHTML
        this.attachments = copy.attachments
    }

    override var supdate: ((Any) -> Unit)? = null
    override var smutatingcount = 0
    override fun scopy(): MutableStruct = MailComposerOptions(this as MutableStruct)

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {
    }
}

// MARK: - MailAttachment

/// A file attachment for an email.
@androidx.annotation.Keep
@Suppress("MUST_BE_INITIALIZED")
class MailAttachment: MutableStruct, skip.lib.SwiftProjecting {
    /// The file URL of the attachment.
    var url: URL
        get() = field.sref({ this.url = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    /// The MIME type (e.g. `"image/png"`, `"application/pdf"`).
    var mimeType: String
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    /// The filename to display to the recipient.
    var filename: String
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }

    constructor(url: URL, mimeType: String, filename: String) {
        this.url = url
        this.mimeType = mimeType
        this.filename = filename
    }

    private constructor(copy: MutableStruct) {
        @Suppress("NAME_SHADOWING", "UNCHECKED_CAST") val copy = copy as MailAttachment
        this.url = copy.url
        this.mimeType = copy.mimeType
        this.filename = copy.filename
    }

    override var supdate: ((Any) -> Unit)? = null
    override var smutatingcount = 0
    override fun scopy(): MutableStruct = MailAttachment(this as MutableStruct)

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {
    }
}

// MARK: - MailComposerResult

/// The result of a mail composition attempt.
@androidx.annotation.Keep
enum class MailComposerResult(override val rawValue: String, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): RawRepresentable<String>, skip.lib.SwiftProjecting {
    /// The email was sent successfully.
    sent("sent"),
    /// The email was saved as a draft.
    saved("saved"),
    /// The user cancelled the composition.
    cancelled("cancelled"),
    /// The composition failed.
    failed("failed"),
    /// The result is unknown (Android always returns this since the intent doesn't report back).
    unknown("unknown");

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {
        fun init(rawValue: String): MailComposerResult? {
            return when (rawValue) {
                "sent" -> MailComposerResult.sent
                "saved" -> MailComposerResult.saved
                "cancelled" -> MailComposerResult.cancelled
                "failed" -> MailComposerResult.failed
                "unknown" -> MailComposerResult.unknown
                else -> null
            }
        }
    }
}

fun MailComposerResult(rawValue: String): MailComposerResult? = MailComposerResult.init(rawValue = rawValue)

// MARK: - MailComposer Availability

/// Utility for checking mail composition availability.
enum class MailComposer {
    ;

    @androidx.annotation.Keep
    companion object {
        /// Whether the device can send email.
        ///
        /// On iOS, this checks `MFMailComposeViewController.canSendMail()`.
        /// On Android, this checks whether an app can handle `ACTION_SENDTO` with a `mailto:` URI.
        fun canSendMail(): Boolean {
            val context = ProcessInfo.processInfo.androidContext.sref()
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.setData(Uri.parse("mailto:"))
            return intent.resolveActivity(context.getPackageManager()) != null
        }
    }
}

/// Present an email composition interface.
///
/// On iOS, this presents an `MFMailComposeViewController` in a sheet.
/// On Android, this launches an `ACTION_SENDTO` intent to the user's email app.
///
/// - Parameters:
///   - isPresented: A binding that controls whether the composer is shown.
///   - options: The email composition options (recipients, subject, body, attachments).
///   - onComplete: Called when the composition finishes, with the result status.
fun View.withMailComposer(isPresented: Binding<Boolean>, options: MailComposerOptions = MailComposerOptions(), onComplete: ((MailComposerResult) -> Unit)? = null): View {
    return ComposeBuilder l@{ composectx: ComposeContext ->
        val context = LocalContext.current.sref()

        return@l onChange(of = isPresented.wrappedValue) { oldValue, presented ->
            if (presented == true) {
                isPresented.wrappedValue = false
                launchMailIntent(context = context, options = options)
                onComplete?.invoke(MailComposerResult.unknown)
            }
        }.Compose(composectx) // !SKIP
        ComposeResult.ok
    }
}

// MARK: - Android Intent

private fun launchMailIntent(context: Context, options: MailComposerOptions) {
    if (options.attachments.isEmpty) {
        // Simple mailto: intent for text-only emails
        var uriString = "mailto:"
        if (!options.recipients.isEmpty) {
            uriString += options.recipients.joined(separator = ",")
        }
        var params: Array<String> = arrayOf()
        options.subject?.let { subject ->
            params.append("subject=" + Uri.encode(subject))
        }
        options.body?.let { body ->
            params.append("body=" + Uri.encode(body))
        }
        if (!options.ccRecipients.isEmpty) {
            params.append("cc=" + options.ccRecipients.joined(separator = ","))
        }
        if (!options.bccRecipients.isEmpty) {
            params.append("bcc=" + options.bccRecipients.joined(separator = ","))
        }
        if (!params.isEmpty) {
            uriString += "?" + params.joined(separator = "&")
        }

        val intent = Intent(Intent.ACTION_SENDTO)
        intent.setData(Uri.parse(uriString))
        context.startActivity(intent)
    } else {
        // ACTION_SEND or ACTION_SEND_MULTIPLE for attachments
        val intent: Intent
        if (options.attachments.count == 1) {
            intent = Intent(Intent.ACTION_SEND)
            intent.setType(options.attachments[0].mimeType)
            val fileUri = Uri.parse(options.attachments[0].url.absoluteString)
            intent.putExtra(Intent.EXTRA_STREAM, fileUri)
        } else {
            intent = Intent(Intent.ACTION_SEND_MULTIPLE)
            intent.setType("message/rfc822")
            val uris = ArrayList<Uri>()
            for (attachment in options.attachments.sref()) {
                val fileUri = Uri.parse(attachment.url.absoluteString)
                uris.add(fileUri)
            }
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        }

        if (!options.recipients.isEmpty) {
            intent.putExtra(Intent.EXTRA_EMAIL, options.recipients.toList().toTypedArray())
        }
        if (!options.ccRecipients.isEmpty) {
            intent.putExtra(Intent.EXTRA_CC, options.ccRecipients.toList().toTypedArray())
        }
        if (!options.bccRecipients.isEmpty) {
            intent.putExtra(Intent.EXTRA_BCC, options.bccRecipients.toList().toTypedArray())
        }
        options.subject?.let { subject ->
            intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        options.body?.let { body ->
            if (options.isHTML) {
                intent.putExtra(Intent.EXTRA_TEXT, android.text.Html.fromHtml(body, android.text.Html.FROM_HTML_MODE_COMPACT))
            } else {
                intent.putExtra(Intent.EXTRA_TEXT, body)
            }
        }

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(intent, "Send Email"))
    }
}

// MARK: - iOS MFMailComposeViewController


