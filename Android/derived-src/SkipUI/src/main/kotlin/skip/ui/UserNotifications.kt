package skip.ui

import skip.lib.*
import skip.lib.Array
import skip.lib.Set

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0
import skip.foundation.*
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequestBuilder
import java.util.HashSet
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@androidx.annotation.Keep
class UNUserNotificationCenter: skip.lib.SwiftProjecting {

    private constructor() {
    }

    suspend fun notificationSettings(): UNNotificationSettings = Async.run l@{
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return@l UNNotificationSettings(authorizationStatus = UNAuthorizationStatus.authorized)
        }
        val status: UNAuthorizationStatus
        val matchtarget_0 = UIApplication.shared.androidActivity
        if (matchtarget_0 != null) {
            val activity = matchtarget_0
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                status = UNAuthorizationStatus.authorized
            } else if (UserDefaults.standard.bool(forKey = "UNNotificationPermissionDenied")) {
                status = UNAuthorizationStatus.denied
            } else {
                status = UNAuthorizationStatus.notDetermined
            }
        } else if (UserDefaults.standard.bool(forKey = "UNNotificationPermissionDenied")) {
            status = UNAuthorizationStatus.denied
        } else {
            status = UNAuthorizationStatus.notDetermined
        }
        return@l UNNotificationSettings(authorizationStatus = status)
    }
    fun callback_notificationSettings(f_return_callback: (skip.ui.UNNotificationSettings) -> Unit) {
        Task {
            f_return_callback(notificationSettings())
        }
    }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    suspend fun setBadgeCount(count: Int): Unit = Unit

    suspend fun requestAuthorization(options: UNAuthorizationOptions): Boolean = Async.run l@{
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return@l true
        }
        val granted = UIApplication.shared.requestPermission(Manifest.permission.POST_NOTIFICATIONS)
        val defaults = UserDefaults.standard
        if (granted) {
            defaults.removeObject(forKey = "UNNotificationPermissionDenied")
        } else {
            defaults.set(true, forKey = "UNNotificationPermissionDenied")
        }
        return@l granted
    }

    suspend fun requestAuthorization(bridgedOptions: Int): Boolean = Async.run l@{
        return@l requestAuthorization(options = UNAuthorizationOptions(rawValue = bridgedOptions))
    }
    fun callback_requestAuthorization(bridgedOptions: Int, f_return_callback: (Boolean?, Throwable?) -> Unit) {
        Task {
            try {
                f_return_callback(requestAuthorization(bridgedOptions = bridgedOptions), null)
            } catch(t: Throwable) {
                f_return_callback(null, t)
            }
        }
    }

    var delegate: UNUserNotificationCenterDelegate? = null
        get() = field.sref({ this.delegate = it })
        set(newValue) {
            field = newValue.sref()
        }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    val supportsContentExtensions: Boolean
        get() {
            fatalError()
        }

    suspend fun add(request: UNNotificationRequest): Unit = Async.run l@{
        val delegate_0 = delegate.sref()
        if (delegate_0 == null) {
            return@l
        }

        val notification = UNNotification(request = request, date = Date.now)
        val options = delegate_0.userNotificationCenter(this, willPresent = notification)
        if (!options.contains(UNNotificationPresentationOptions.banner) && !options.contains(UNNotificationPresentationOptions.alert)) {
            return@l
        }

        val activity_0 = UIApplication.shared.androidActivity.sref()
        if (activity_0 == null) {
            return@l
        }

        // Build the data which should be displayed in the notification.
        val dataBuilder = Data.Builder()
            .putString("title", request.content.title)
            .putString("body", request.content.body)
            .putInt("id", request.identifier.hashValue)

        request.content.attachments.first(where = { it -> it.type == "public.image" })?.let { imageAttachment ->
            dataBuilder.putString("image_url", imageAttachment.url.absoluteString)
        }

        for ((key, value) in request.content.userInfo.sref()) {
            val matchtarget_1 = value as? String
            if (matchtarget_1 != null) {
                val s = matchtarget_1
                dataBuilder.putString(key.toString(), s)
            } else {
                val matchtarget_2 = value as? Boolean
                if (matchtarget_2 != null) {
                    val b = matchtarget_2
                    dataBuilder.putBoolean(key.toString(), b)
                } else {
                    val matchtarget_3 = value as? Int
                    if (matchtarget_3 != null) {
                        val i = matchtarget_3
                        dataBuilder.putInt(key.toString(), i)
                    } else {
                        val matchtarget_4 = value as? Double
                        if (matchtarget_4 != null) {
                            val d = matchtarget_4
                            dataBuilder.putDouble(key.toString(), d)
                        } else {
                            dataBuilder.putString(key.toString(), value.toString())
                        }
                    }
                }
            }
        }

        // Get the next trigger date (if any).
        val nextDate = ((request.trigger as? UNCalendarNotificationTrigger)?.nextTriggerDate() ?: (request.trigger as? UNTimeIntervalNotificationTrigger)?.nextTriggerDate()).sref()
        val delayMillis = if (nextDate != null) max(0, nextDate!!.currentTimeMillis - System.currentTimeMillis()) else 0

        // Get the work manager.
        val workManager = WorkManager.getInstance(activity_0)

        // Add the notification work request to the work manager.
        val workData = dataBuilder.build()
        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(workData)
            .addTag(request.identifier)
            .build()
        workManager.enqueue(workRequest)

        // Add the notification identifier to the shared preferences to be able to cancel it later.
        nextDate?.currentTimeMillis?.let { triggerMillis ->
            val preferences = activity_0.getSharedPreferences("__skip_usernotifications", Context.MODE_PRIVATE)
            val ids = HashSet<String>(preferences.getStringSet("ids", HashSet<String>()) ?: HashSet<String>())
            ids.add(request.identifier)
            preferences.edit()
                .putStringSet("ids", ids)
                .putLong("date_" + request.identifier, triggerMillis)
                .apply()
        }
    }
    fun callback_add(request: UNNotificationRequest, f_return_callback: (Throwable?) -> Unit) {
        Task {
            try {
                add(request = request)
                f_return_callback(null)
            } catch(t: Throwable) {
                f_return_callback(t)
            }
        }
    }

    suspend fun pendingNotificationRequests(): Array<Any> = Async.run l@{
        return@l getPendingNotificationRequests()
    }
    fun callback_pendingNotificationRequests(f_return_callback: (Array<Any>) -> Unit) {
        Task {
            f_return_callback(pendingNotificationRequests())
        }
    }

    fun removePendingNotificationRequests(withIdentifiers: Array<String>) {
        val identifiers = withIdentifiers
        val activity_1 = UIApplication.shared.androidActivity.sref()
        if (activity_1 == null) {
            return
        }

        // Get all notification identifiers from the shared preferences.
        val preferences = activity_1.getSharedPreferences("__skip_usernotifications", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        val ids = HashSet<String>(preferences.getStringSet("ids", HashSet<String>()) ?: HashSet<String>())

        // Get the work manager.
        val workManager = WorkManager.getInstance(activity_1)

        // Cancel all pending notifications using the work manager.
        val now = System.currentTimeMillis()
        for (identifier in identifiers.sref()) {
            val triggerTime = preferences.getLong("date_" + identifier, 0)
            if (triggerTime > now) {
                workManager.cancelAllWorkByTag(identifier)
                ids.remove(identifier)
                editor.remove("date_" + identifier)
            }
        }

        // Update the notification identifiers in the shared preferences.
        editor.putStringSet("ids", ids)
        editor.apply()
    }

    fun removeAllPendingNotificationRequests() {
        val pendingNotifications = getPendingNotificationRequests()
        val identifiers = pendingNotifications.compactMap { it ->
            (it as? UNNotificationRequest)?.identifier
        }
        removePendingNotificationRequests(withIdentifiers = identifiers)
    }

    suspend fun deliveredNotifications(): Array<Any> = Async.run l@{
        return@l getDeliveredNotifications()
    }
    fun callback_deliveredNotifications(f_return_callback: (Array<Any>) -> Unit) {
        Task {
            f_return_callback(deliveredNotifications())
        }
    }

    fun removeDeliveredNotifications(withIdentifiers: Array<String>) {
        val identifiers = withIdentifiers
        val activity_2 = UIApplication.shared.androidActivity.sref()
        if (activity_2 == null) {
            return
        }

        // Get all notification identifiers from the shared preferences.
        val preferences = activity_2.getSharedPreferences("__skip_usernotifications", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        val ids = HashSet<String>(preferences.getStringSet("ids", HashSet<String>()) ?: HashSet<String>())

        // Get the notification manager.
        val notificationManager = (activity_2.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).sref()

        // Cancel all delivered notifications using the notification manager.
        val now = System.currentTimeMillis()
        for (identifier in identifiers.sref()) {
            val triggerTime = preferences.getLong("date_" + identifier, 0)
            if (triggerTime < now) {
                notificationManager.cancel(identifier.hashValue)
                ids.remove(identifier)
                editor.remove("date_" + identifier)
            }
        }

        // Update the notification identifiers in the shared preferences.
        editor.putStringSet("ids", ids)
        editor.apply()
    }

    fun removeAllDeliveredNotifications() {
        val deliveredNotifications = getDeliveredNotifications()
        val identifiers = deliveredNotifications.compactMap { it ->
            (it as? UNNotification)?.request?.identifier
        }
        removeDeliveredNotifications(withIdentifiers = identifiers)
    }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    fun setNotificationCategories(categories: Set<AnyHashable>) = Unit

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    suspend fun getNotificationCategories(): Set<AnyHashable> = Async.run {
        fatalError()
    }

    private fun getAllNotificationRequests(): Array<Tuple2<String, Long>> {
        val activity_3 = UIApplication.shared.androidActivity.sref()
        if (activity_3 == null) {
            return arrayOf()
        }
        val preferences = activity_3.getSharedPreferences("__skip_usernotifications", Context.MODE_PRIVATE)
        val ids = (preferences.getStringSet("ids", null) ?: java.util.HashSet<String>()).sref()

        var all: Array<Tuple2<String, Long>> = arrayOf()
        val iterator = ids.iterator()
        while (iterator.hasNext()) {
            val id = iterator.next() as String
            val time = preferences.getLong("date_" + id, 0)
            all.append(Tuple2(id, time.sref()))
        }

        return all.sref()
    }

    private fun getPendingNotificationRequests(): Array<Any> {
        val now = Date().currentTimeMillis
        return getAllNotificationRequests()
            .filter { it -> it.timestamp > now }
            .map { it -> UNNotificationRequest(identifier = it.id, content = UNMutableNotificationContent(), trigger = null) }
    }

    private fun getDeliveredNotifications(): Array<Any> {
        val now = Date().currentTimeMillis
        return getAllNotificationRequests()
            .filter { it -> it.timestamp <= now }
            .map l@{ it ->
                val request = UNNotificationRequest(identifier = it.id, content = UNMutableNotificationContent(), trigger = null)
                return@l UNNotification(request = request, date = Date(timeIntervalSince1970 = Double(it.timestamp) / 1000.0))
            }
    }

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {
        private val shared = UNUserNotificationCenter()

        fun current(): UNUserNotificationCenter = shared
    }
}

interface UNUserNotificationCenterDelegate {
    suspend fun userNotificationCenter(center: UNUserNotificationCenter, didReceive: UNNotificationResponse): Unit = Unit

    suspend fun userNotificationCenter(center: UNUserNotificationCenter, willPresent: UNNotification): UNNotificationPresentationOptions = Async.run l@{
        val notification = willPresent
        return@l UNNotificationPresentationOptions.of()
    }

    fun userNotificationCenter(center: UNUserNotificationCenter, openSettingsFor: UNNotification?) = Unit
}

class UNAuthorizationOptions: OptionSet<UNAuthorizationOptions, Int>, MutableStruct {
    override var rawValue: Int

    constructor(rawValue: Int) {
        this.rawValue = rawValue
    }

    override val rawvaluelong: ULong
        get() = ULong(rawValue)
    override fun makeoptionset(rawvaluelong: ULong): UNAuthorizationOptions = UNAuthorizationOptions(rawValue = Int(rawvaluelong))
    override fun assignoptionset(target: UNAuthorizationOptions) {
        willmutate()
        try {
            assignfrom(target)
        } finally {
            didmutate()
        }
    }

    private constructor(copy: MutableStruct) {
        @Suppress("NAME_SHADOWING", "UNCHECKED_CAST") val copy = copy as UNAuthorizationOptions
        this.rawValue = copy.rawValue
    }

    override var supdate: ((Any) -> Unit)? = null
    override var smutatingcount = 0
    override fun scopy(): MutableStruct = UNAuthorizationOptions(this as MutableStruct)

    private fun assignfrom(target: UNAuthorizationOptions) {
        this.rawValue = target.rawValue
    }

    @androidx.annotation.Keep
    companion object {

        val badge = UNAuthorizationOptions(rawValue = 1 shl 0) // For bridging
        val sound = UNAuthorizationOptions(rawValue = 1 shl 1) // For bridging
        val alert = UNAuthorizationOptions(rawValue = 1 shl 2) // For bridging
        val carPlay = UNAuthorizationOptions(rawValue = 1 shl 3) // For bridging
        val criticalAlert = UNAuthorizationOptions(rawValue = 1 shl 4) // For bridging
        val providesAppNotificationSettings = UNAuthorizationOptions(rawValue = 1 shl 5) // For bridging
        var provisional = UNAuthorizationOptions(rawValue = 1 shl 6)
            get() = field.sref({ this.provisional = it })
            set(newValue) {
                field = newValue.sref()
            } // For bridging

        fun of(vararg options: UNAuthorizationOptions): UNAuthorizationOptions {
            val value = options.fold(Int(0)) { result, option -> result or option.rawValue }
            return UNAuthorizationOptions(rawValue = value)
        }
    }
}

@androidx.annotation.Keep
class UNNotification: skip.lib.SwiftProjecting {
    val request: UNNotificationRequest
    val date: Date

    constructor(request: UNNotificationRequest, date: Date) {
        this.request = request
        this.date = date.sref()
    }

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {
    }
}

@androidx.annotation.Keep
class UNNotificationRequest: skip.lib.SwiftProjecting {
    val identifier: String
    val content: UNNotificationContent
    val trigger: UNNotificationTrigger?

    constructor(identifier: String, content: UNNotificationContent, trigger: UNNotificationTrigger?) {
        this.identifier = identifier
        this.content = content
        this.trigger = trigger
    }

    constructor(identifier: String, content: UNNotificationContent): this(identifier = identifier, content = content, trigger = UNPushNotificationTrigger(repeats = false)) {
    }

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {
    }
}

val UNNotificationDefaultActionIdentifier = "UNNotificationDefaultActionIdentifier" // For bridging
val UNNotificationDismissActionIdentifier = "UNNotificationDismissActionIdentifier" // For bridging

@androidx.annotation.Keep
class UNNotificationResponse: skip.lib.SwiftProjecting {
    val actionIdentifier: String
    val notification: UNNotification

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    val targetScene: Any?
        get() {
            fatalError()
        }

    constructor(actionIdentifier: String = "UNNotificationDefaultActionIdentifier", notification: UNNotification) {
        this.actionIdentifier = actionIdentifier
        this.notification = notification
    }

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {
    }
}

@androidx.annotation.Keep
class UNNotificationPresentationOptions: OptionSet<UNNotificationPresentationOptions, Int>, MutableStruct, skip.lib.SwiftProjecting {
    override var rawValue: Int

    constructor(rawValue: Int) {
        this.rawValue = rawValue
    }

    override val rawvaluelong: ULong
        get() = ULong(rawValue)
    override fun makeoptionset(rawvaluelong: ULong): UNNotificationPresentationOptions = UNNotificationPresentationOptions(rawValue = Int(rawvaluelong))
    override fun assignoptionset(target: UNNotificationPresentationOptions) {
        willmutate()
        try {
            assignfrom(target)
        } finally {
            didmutate()
        }
    }

    private constructor(copy: MutableStruct) {
        @Suppress("NAME_SHADOWING", "UNCHECKED_CAST") val copy = copy as UNNotificationPresentationOptions
        this.rawValue = copy.rawValue
    }

    override var supdate: ((Any) -> Unit)? = null
    override var smutatingcount = 0
    override fun scopy(): MutableStruct = UNNotificationPresentationOptions(this as MutableStruct)

    private fun assignfrom(target: UNNotificationPresentationOptions) {
        this.rawValue = target.rawValue
    }

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {

        val badge = UNNotificationPresentationOptions(rawValue = 1 shl 0) // For bridging
        val banner = UNNotificationPresentationOptions(rawValue = 1 shl 1) // For bridging
        val list = UNNotificationPresentationOptions(rawValue = 1 shl 2) // For bridging
        val sound = UNNotificationPresentationOptions(rawValue = 1 shl 3) // For bridging
        val alert = UNNotificationPresentationOptions(rawValue = 1 shl 4) // For bridging

        fun of(vararg options: UNNotificationPresentationOptions): UNNotificationPresentationOptions {
            val value = options.fold(Int(0)) { result, option -> result or option.rawValue }
            return UNNotificationPresentationOptions(rawValue = value)
        }
    }
}

@androidx.annotation.Keep
@Suppress("MUST_BE_INITIALIZED", "MUST_BE_INITIALIZED_OR_FINAL_OR_ABSTRACT")
open class UNNotificationContent: skip.lib.SwiftProjecting {
    open var title: String
    open var subtitle: String
    open var body: String
    open var badge: java.lang.Number? = null
    open val bridgedBadge: Int?
        get() {
            return badge?.intValue
        }
    open var sound: UNNotificationSound? = null
    open var launchImageName: String
    open var userInfo: Dictionary<AnyHashable, Any>
        get() = field.sref({ this.userInfo = it })
        set(newValue) {
            field = newValue.sref()
        }
    open val bridgedUserInfo: Dictionary<AnyHashable, Any>
        get() {
            return userInfo.filter l@{ entry ->
                val value = entry.value
                return@l value is Boolean || value is Double || value is Float || value is Int || value is Long || value is String || value is Array<*> || value is Dictionary<*, *> || value is Set<*>
            }
        }
    open var attachments: Array<UNNotificationAttachment>
        get() = field.sref({ this.attachments = it })
        set(newValue) {
            field = newValue.sref()
        }
    open var categoryIdentifier: String
    open var threadIdentifier: String
    open var targetContentIdentifier: String? = null
    open var summaryArgument: String
    open var summaryArgumentCount: Int
    open var filterCriteria: String? = null

    constructor(title: String = "", subtitle: String = "", body: String = "", badge: java.lang.Number? = null, sound: UNNotificationSound? = UNNotificationSound.default, launchImageName: String = "", userInfo: Dictionary<AnyHashable, Any> = dictionaryOf(), attachments: Array<UNNotificationAttachment> = arrayOf(), categoryIdentifier: String = "", threadIdentifier: String = "", targetContentIdentifier: String? = null, summaryArgument: String = "", summaryArgumentCount: Int = 0, filterCriteria: String? = null) {
        this.title = title
        this.subtitle = subtitle
        this.body = body
        this.badge = badge
        this.sound = sound
        this.launchImageName = launchImageName
        this.userInfo = userInfo
        this.attachments = attachments
        this.categoryIdentifier = categoryIdentifier
        this.threadIdentifier = threadIdentifier
        this.targetContentIdentifier = targetContentIdentifier
        this.summaryArgument = summaryArgument
        this.summaryArgumentCount = summaryArgumentCount
        this.filterCriteria = filterCriteria
    }

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object: CompanionClass() {

        override fun bridgedContent(title: String, subtitle: String, body: String, badge: Int?, sound: UNNotificationSound?, launchImageName: String, userInfo: Dictionary<AnyHashable, Any>, attachments: Array<UNNotificationAttachment>, categoryIdentifier: String, threadIdentifier: String, targetContentIdentifier: String?, summaryArgument: String, summaryArgumentCount: Int, filterCriteria: String?): UNNotificationContent = UNNotificationContent(title = title, subtitle = subtitle, body = body, badge = if (badge == null) null else NSNumber(value = Int(badge!!)), sound = sound, launchImageName = launchImageName, userInfo = userInfo, attachments = attachments, categoryIdentifier = categoryIdentifier, threadIdentifier = threadIdentifier, targetContentIdentifier = targetContentIdentifier, summaryArgument = summaryArgument, summaryArgumentCount = summaryArgumentCount, filterCriteria = filterCriteria)
    }
    open class CompanionClass {
        open fun bridgedContent(title: String, subtitle: String, body: String, badge: Int?, sound: UNNotificationSound?, launchImageName: String, userInfo: Dictionary<AnyHashable, Any>, attachments: Array<UNNotificationAttachment>, categoryIdentifier: String, threadIdentifier: String, targetContentIdentifier: String?, summaryArgument: String, summaryArgumentCount: Int, filterCriteria: String?): UNNotificationContent = UNNotificationContent.bridgedContent(title = title, subtitle = subtitle, body = body, badge = badge, sound = sound, launchImageName = launchImageName, userInfo = userInfo, attachments = attachments, categoryIdentifier = categoryIdentifier, threadIdentifier = threadIdentifier, targetContentIdentifier = targetContentIdentifier, summaryArgument = summaryArgument, summaryArgumentCount = summaryArgumentCount, filterCriteria = filterCriteria)
    }
}

class UNMutableNotificationContent: UNNotificationContent {
    override var title: String
        get() = super.title
        set(newValue) {
            super.title = newValue
        }
    override var subtitle: String
        get() = super.subtitle
        set(newValue) {
            super.subtitle = newValue
        }
    override var body: String
        get() = super.body
        set(newValue) {
            super.body = newValue
        }
    override var badge: java.lang.Number?
        get() = super.badge
        set(newValue) {
            super.badge = newValue
        }
    override var sound: UNNotificationSound?
        get() = super.sound
        set(newValue) {
            super.sound = newValue
        }
    override var launchImageName: String
        get() = super.launchImageName
        set(newValue) {
            super.launchImageName = newValue
        }
    override var userInfo: Dictionary<AnyHashable, Any>
        get() = super.userInfo.sref({ this.userInfo = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            super.userInfo = newValue
        }
    override var attachments: Array<UNNotificationAttachment>
        get() = super.attachments.sref({ this.attachments = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            super.attachments = newValue
        }
    override var categoryIdentifier: String
        get() = super.categoryIdentifier
        set(newValue) {
            super.categoryIdentifier = newValue
        }
    override var threadIdentifier: String
        get() = super.threadIdentifier
        set(newValue) {
            super.threadIdentifier = newValue
        }
    override var targetContentIdentifier: String?
        get() = super.targetContentIdentifier
        set(newValue) {
            super.targetContentIdentifier = newValue
        }
    override var summaryArgument: String
        get() = super.summaryArgument
        set(newValue) {
            super.summaryArgument = newValue
        }
    override var summaryArgumentCount: Int
        get() = super.summaryArgumentCount
        set(newValue) {
            super.summaryArgumentCount = newValue
        }
    override var filterCriteria: String?
        get() = super.filterCriteria
        set(newValue) {
            super.filterCriteria = newValue
        }

    constructor(title: String = "", subtitle: String = "", body: String = "", badge: java.lang.Number? = null, sound: UNNotificationSound? = UNNotificationSound.default, launchImageName: String = "", userInfo: Dictionary<AnyHashable, Any> = dictionaryOf(), attachments: Array<UNNotificationAttachment> = arrayOf(), categoryIdentifier: String = "", threadIdentifier: String = "", targetContentIdentifier: String? = null, summaryArgument: String = "", summaryArgumentCount: Int = 0, filterCriteria: String? = null): super(title, subtitle, body, badge, sound, launchImageName, userInfo, attachments, categoryIdentifier, threadIdentifier, targetContentIdentifier, summaryArgument, summaryArgumentCount, filterCriteria) {
    }

    @androidx.annotation.Keep
    companion object: UNNotificationContent.CompanionClass() {
    }
}

@androidx.annotation.Keep
class UNNotificationSound: skip.lib.SwiftProjecting {
    val name: UNNotificationSoundName
    val bridgedName: String
        get() = name.rawValue
    val volume: Float

    constructor(named: UNNotificationSoundName, volume: Float = 0.0f) {
        val name = named
        this.name = name
        this.volume = volume
    }

    constructor(named: String, volume: Float): this(named = UNNotificationSoundName(rawValue = named), volume = volume) {
    }

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {

        val default: UNNotificationSound
            get() = UNNotificationSound(named = UNNotificationSoundName(rawValue = "default"))

        val defaultCriticalSound: UNNotificationSound
            get() = UNNotificationSound(named = UNNotificationSoundName(rawValue = "default_critical"))

        fun defaultCriticalSound(withAudioVolume: Float): UNNotificationSound {
            val volume = withAudioVolume
            return UNNotificationSound(named = UNNotificationSoundName(rawValue = "default_critical"), volume = volume)
        }

        fun soundNamed(name: UNNotificationSoundName): UNNotificationSound = UNNotificationSound(named = name)
    }
}

class UNNotificationSoundName: RawRepresentable<String> {
    override val rawValue: String

    constructor(rawValue: String) {
        this.rawValue = rawValue
    }

    override fun equals(other: Any?): Boolean {
        if (other !is UNNotificationSoundName) return false
        return rawValue == other.rawValue
    }

    override fun hashCode(): Int {
        var result = 1
        result = Hasher.combine(result, rawValue)
        return result
    }

    @androidx.annotation.Keep
    companion object {
    }
}

val UNNotificationAttachmentOptionsTypeHintKey = "UNNotificationAttachmentOptionsTypeHintKey"
val UNNotificationAttachmentOptionsThumbnailHiddenKey = "UNNotificationAttachmentOptionsThumbnailHiddenKey"
val UNNotificationAttachmentOptionsThumbnailClippingRectKey = "UNNotificationAttachmentOptionsThumbnailClippingRectKey"
val UNNotificationAttachmentOptionsThumbnailTimeKey = "UNNotificationAttachmentOptionsThumbnailTimeKey"

@androidx.annotation.Keep
open class UNNotificationAttachment: skip.lib.SwiftProjecting {
    val identifier: String
    val url: URL
    val type: String
    val timeShift: Double

    constructor(identifier: String, url: URL, type: String = "public.data", timeShift: Double = 0.0) {
        this.identifier = identifier
        this.url = url.sref()
        this.type = type
        this.timeShift = timeShift
    }

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object: CompanionClass() {

        override fun attachment(withIdentifier: String, url: URL, options: Dictionary<AnyHashable, Any>?): UNNotificationAttachment {
            val identifier = withIdentifier
            return UNNotificationAttachment(identifier = identifier, url = url, type = "public.data")
        }
    }
    open class CompanionClass {
        open fun attachment(withIdentifier: String, url: URL, options: Dictionary<AnyHashable, Any>? = null): UNNotificationAttachment = UNNotificationAttachment.attachment(withIdentifier = withIdentifier, url = url, options = options)
    }
}

@androidx.annotation.Keep
open class UNNotificationTrigger: skip.lib.SwiftProjecting {

    val repeats: Boolean

    constructor(repeats: Boolean) {
        this.repeats = repeats
    }

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object: CompanionClass() {
    }
    open class CompanionClass {
    }
}

@androidx.annotation.Keep
class UNTimeIntervalNotificationTrigger: UNNotificationTrigger {

    val timeInterval: Double

    constructor(timeInterval: Double, repeats: Boolean): super(repeats = repeats) {
        this.timeInterval = timeInterval
    }

    fun nextTriggerDate(): Date? {
        val now = Date()
        return now.addingTimeInterval(this.timeInterval)
    }

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object: UNNotificationTrigger.CompanionClass() {
    }
}

@androidx.annotation.Keep
class UNCalendarNotificationTrigger: UNNotificationTrigger {

    val dateComponents: Any /* DateComponents */

    constructor(dateMatching: Any, repeats: Boolean): super(repeats = repeats) {
        val dateComponents = dateMatching
        this.dateComponents = dateComponents.sref()
    }

    fun nextTriggerDate(): Date? {
        val calendar = Calendar.current.sref()
        val now = Date()
        val nextDate_0 = calendar.nextDate(after = now, matching = this.dateComponents as DateComponents, matchingPolicy = Calendar.MatchingPolicy.nextTime, repeatedTimePolicy = Calendar.RepeatedTimePolicy.first, direction = Calendar.SearchDirection.forward)
        if (nextDate_0 == null) {
            return null
        }

        if (!this.repeats && nextDate_0 <= now) {
            return null
        }

        return nextDate_0.sref()
    }

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object: UNNotificationTrigger.CompanionClass() {
    }
}

class UNLocationNotificationTrigger: UNNotificationTrigger {
    val region: Any /* CLRegion */

    constructor(region: Any, repeats: Boolean): super(repeats = repeats) {
        this.region = region.sref()
    }

    @androidx.annotation.Keep
    companion object: UNNotificationTrigger.CompanionClass() {
    }
}

class UNPushNotificationTrigger: UNNotificationTrigger {
    constructor(repeats: Boolean): super(repeats = repeats) {
    }

    @androidx.annotation.Keep
    companion object: UNNotificationTrigger.CompanionClass() {
    }
}

@androidx.annotation.Keep
enum class UNAuthorizationStatus(override val rawValue: Int, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): RawRepresentable<Int>, skip.lib.SwiftProjecting {
    notDetermined(0),
    denied(1),
    authorized(2),
    provisional(3),
    ephemeral(4);

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {
        fun init(rawValue: Int): UNAuthorizationStatus? {
            return when (rawValue) {
                0 -> UNAuthorizationStatus.notDetermined
                1 -> UNAuthorizationStatus.denied
                2 -> UNAuthorizationStatus.authorized
                3 -> UNAuthorizationStatus.provisional
                4 -> UNAuthorizationStatus.ephemeral
                else -> null
            }
        }
    }
}

fun UNAuthorizationStatus(rawValue: Int): UNAuthorizationStatus? = UNAuthorizationStatus.init(rawValue = rawValue)

enum class UNShowPreviewsSetting(override val rawValue: Int, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): RawRepresentable<Int> {
    always(0),
    whenAuthenticated(1),
    never(2);

    @androidx.annotation.Keep
    companion object {
        fun init(rawValue: Int): UNShowPreviewsSetting? {
            return when (rawValue) {
                0 -> UNShowPreviewsSetting.always
                1 -> UNShowPreviewsSetting.whenAuthenticated
                2 -> UNShowPreviewsSetting.never
                else -> null
            }
        }
    }
}

fun UNShowPreviewsSetting(rawValue: Int): UNShowPreviewsSetting? = UNShowPreviewsSetting.init(rawValue = rawValue)

enum class UNNotificationSetting(override val rawValue: Int, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): RawRepresentable<Int> {
    notSupported(0),
    disabled(1),
    enabled(2);

    @androidx.annotation.Keep
    companion object {
        fun init(rawValue: Int): UNNotificationSetting? {
            return when (rawValue) {
                0 -> UNNotificationSetting.notSupported
                1 -> UNNotificationSetting.disabled
                2 -> UNNotificationSetting.enabled
                else -> null
            }
        }
    }
}

fun UNNotificationSetting(rawValue: Int): UNNotificationSetting? = UNNotificationSetting.init(rawValue = rawValue)

enum class UNAlertStyle(override val rawValue: Int, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): RawRepresentable<Int> {
    none(0),
    banner(1),
    alert(2);

    @androidx.annotation.Keep
    companion object {
        fun init(rawValue: Int): UNAlertStyle? {
            return when (rawValue) {
                0 -> UNAlertStyle.none
                1 -> UNAlertStyle.banner
                2 -> UNAlertStyle.alert
                else -> null
            }
        }
    }
}

fun UNAlertStyle(rawValue: Int): UNAlertStyle? = UNAlertStyle.init(rawValue = rawValue)

@androidx.annotation.Keep
open class UNNotificationSettings: java.lang.Object, skip.lib.SwiftProjecting {
    private val _authorizationStatus: UNAuthorizationStatus

    open val authorizationStatus: UNAuthorizationStatus
        get() = _authorizationStatus

    constructor(authorizationStatus: UNAuthorizationStatus): super() {
        this._authorizationStatus = authorizationStatus
    }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    open val soundSetting: UNNotificationSetting
        get() {
            fatalError()
        }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    open val badgeSetting: UNNotificationSetting
        get() {
            fatalError()
        }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    open val alertSetting: UNNotificationSetting
        get() {
            fatalError()
        }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    open val notificationCenterSetting: UNNotificationSetting
        get() {
            fatalError()
        }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    open val lockScreenSetting: UNNotificationSetting
        get() {
            fatalError()
        }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    open val carPlaySetting: UNNotificationSetting
        get() {
            fatalError()
        }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    open val alertStyle: UNAlertStyle
        get() {
            fatalError()
        }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    open val showPreviewsSetting: UNShowPreviewsSetting
        get() {
            fatalError()
        }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    open val criticalAlertSetting: UNNotificationSetting
        get() {
            fatalError()
        }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    open val providesAppNotificationSettings: Boolean
        get() {
            fatalError()
        }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    open val announcementSetting: UNNotificationSetting
        get() {
            fatalError()
        }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    open val timeSensitiveSetting: UNNotificationSetting
        get() {
            fatalError()
        }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    open val scheduledDeliverySetting: UNNotificationSetting
        get() {
            fatalError()
        }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    open val directMessagesSetting: UNNotificationSetting
        get() {
            fatalError()
        }

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object: CompanionClass() {
    }
    open class CompanionClass {
    }
}

open class NotificationWorker: Worker {
    constructor(context: Context, params: WorkerParameters): super(context, params) {
    }

    override fun doWork(): ListenableWorker.Result {
        // Get the data which should be displayed in the notification.
        val inputData = getInputData()
        val id = inputData.getInt("id", 0)
        val title = inputData.getString("title") ?: ""
        val body = inputData.getString("body") ?: ""
        val imageAttachmentUrl = inputData.getString("image_url")

        val context = getApplicationContext()
        val intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName())
        intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val bundle = android.os.Bundle()
        val allData = inputData.keyValueMap.sref()
        for ((key, value) in allData.sref()) {
            if (key == "title" || key == "body" || key == "id" || key == "image_url") {
                continue
            }
            val matchtarget_5 = value as? String
            if (matchtarget_5 != null) {
                val s = matchtarget_5
                bundle.putString(key, s)
            } else {
                val matchtarget_6 = value as? Boolean
                if (matchtarget_6 != null) {
                    val b = matchtarget_6
                    bundle.putBoolean(key, b)
                } else {
                    val matchtarget_7 = value as? Int
                    if (matchtarget_7 != null) {
                        val i = matchtarget_7
                        bundle.putInt(key, i)
                    } else {
                        val matchtarget_8 = value as? Double
                        if (matchtarget_8 != null) {
                            val d = matchtarget_8
                            bundle.putDouble(key, d)
                        } else {
                            bundle.putString(key, value.toString())
                        }
                    }
                }
            }
        }
        intent?.putExtras(bundle)

        // Create the notification channel.
        val channelID = "tools.skip.firebase.messaging" // Match AndroidManifest.xml
        val notificationManager = (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).sref()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appName = context.getApplicationInfo().loadLabel(context.getPackageManager()).toString()
            notificationManager.createNotificationChannel(NotificationChannel(channelID, appName, NotificationManager.IMPORTANCE_DEFAULT))
        }

        // Build the notification.
        val pendingIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = NotificationCompat.Builder(context, channelID)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        // Update the notification icon.
        if (imageAttachmentUrl != null) {
            val url = imageAttachmentUrl.sref()
            builder.setSmallIcon(IconCompat.createWithContentUri(url))
        } else {
            // Notification icon: must be a resource with transparent background and white logo
            // eg: to be used as a default icon must be added in the AndroidManifest.xml with the following code:
            // <meta-data
            // android:name="com.google.firebase.messaging.default_notification_icon"
            // android:resource="@drawable/ic_notification" />
            var resId = context.getResources().getIdentifier("ic_notification", "drawable", context.getPackageName())
            if (resId == 0) {
                resId = context.getResources().getIdentifier("ic_launcher", "mipmap", context.getPackageName())
            }
            builder.setSmallIcon(IconCompat.createWithResource(context, resId))
        }

        // Display the notification.
        notificationManager.notify(id, builder.build())
        return ListenableWorker.Result.success()
    }

    @androidx.annotation.Keep
    companion object: CompanionClass() {
    }
    open class CompanionClass {
    }
}
