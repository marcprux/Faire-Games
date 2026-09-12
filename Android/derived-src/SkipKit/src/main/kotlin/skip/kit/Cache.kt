package skip.kit

import skip.lib.*

// Copyright 2025–2026 Skip
// SPDX-License-Identifier: MPL-2.0
import skip.foundation.*

/// A cache of keys to values. It can be configured to automatically evict entries when the total entries surpass a given cost limit, as well as remove entries under memory pressure.
///
/// On iOS, this is backed by an [`NSCache`](https://developer.apple.com/documentation/foundation/nscache), and on Android it uses a [`android.util.LruCache`](https://developer.android.com/reference/android/util/LruCache). The exact implementation behavior of the caches varies between the platforms, but is generally considered to behave in an optimal way for each operating system.
@androidx.annotation.Keep
open class Cache<Key, Value>: skip.lib.SwiftProjecting {
    private val cacheLock = NSLock()
    private val manager: CacheManager<Key, Value>

    constructor(evictOnBackground: Boolean = true, limit: Int? = null, cost: ((Value) -> Int)? = null) {
        this.manager = CacheManager(evictOnBackground = evictOnBackground, limit = limit, cost = cost)
    }

    /// Lock the cache for an atomic operation.
    open fun lock(): Unit = cacheLock.lock()

    /// Lock the cache at the end of an atomic operation.
    open fun unlock(): Unit = cacheLock.unlock()

    /// Evict all entries from the cache.
    open fun clear() {
        var deferaction_0: (() -> Unit)? = null
        try {
            lock()
            deferaction_0 = {
                unlock()
            }
            manager.clear()
        } finally {
            deferaction_0?.invoke()
        }
    }

    /// Gets the given key's value from the cache
    open fun getValue(for_: Key): Value? {
        val key = for_
        return this[key].sref()
    }

    /// Sets the given value for the key in the cache
    open fun putValue(value: Value, for_: Key) {
        val key = for_
        this[key] = value.sref()
    }

    /// Get or set a value in the cache.
    open operator fun get(key: Key): Value? {
        var deferaction_1: (() -> Unit)? = null
        try {
            lock()
            deferaction_1 = {
                unlock()
            }
            return manager.get(key = key)
        } finally {
            deferaction_1?.invoke()
        }
    }

    /// Get or set a value in the cache.
    open operator fun set(key: Key, newValue: Value?) {
        var deferaction_2: (() -> Unit)? = null
        try {
            lock()
            deferaction_2 = {
                unlock()
            }
            manager.put(key = key, value = newValue)
        } finally {
            deferaction_2?.invoke()
        }
    }

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object: CompanionClass() {
    }
    open class CompanionClass {
    }
}

private class CacheManager<Key, Value> {
    internal val cache: LRUCostCache<Key, Value>
    internal val limit: Int?
    internal val cacheCallbacks: CacheManagerCallbacks<*, *>

    internal constructor(evictOnBackground: Boolean, limit: Int?, cost: ((Value) -> Int)?) {
        this.cache = LRUCostCache(limit = limit ?: Int.MAX_VALUE, cost = cost)
        this.limit = limit
        this.cacheCallbacks = CacheManagerCallbacks(cache = this, evictOnBackground = evictOnBackground)
        ProcessInfo.processInfo.androidContext.registerComponentCallbacks(cacheCallbacks)
    }

    fun finalize() {
        // de-register the callbacks when this CacheManager is gc'd (which will be possible since the CacheManagerCallbacks only maintains a weak reference to this instance)
        ProcessInfo.processInfo.androidContext.unregisterComponentCallbacks(cacheCallbacks)
    }

    internal fun clear(): Unit = cache.evictAll()

    internal fun get(key: Key): Value? = cache.get(key)

    internal fun put(key: Key, value: Value?) {
        if (value != null) {
            this.cache.put(key, value)
        } else {
            this.cache.remove(key)
        }
    }
}

private class CacheManagerCallbacks<Key, Value>: android.content.ComponentCallbacks2 {
    // We need to maintain a separate ComponentCallbacks2 with a weak reference to the cache, otherwise it will never be GC'd
    private val cacheRef: java.lang.ref.WeakReference<CacheManager<Key, Value>>
    private val evictOnBackground: Boolean

    internal constructor(cache: CacheManager<Key, Value>, evictOnBackground: Boolean) {
        this.cacheRef = java.lang.ref.WeakReference(cache)
        this.evictOnBackground = evictOnBackground
    }

    internal fun clear() {
        cacheRef.get()?.clear()
    }

    override fun onLowMemory(): Unit = this.clear()

    override fun onTrimMemory(level: Int) {
        /// `TRIM_MEMORY_UI_HIDDEN`: Your app's UI is no longer visible. This is a good time to release large memory allocations that are used only by your UI, such as Bitmaps, or resources related to video playback or animations.
        if (level >= android.content.ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            if (evictOnBackground) {
                this.clear()
            }
        }

        /// `TRIM_MEMORY_BACKGROUND`: Your app's process is considered to be in the background, and has become eligible to be killed in order to free memory for other processes. Releasing more memory will prolong the time that your process can remain cached in memory. An effective strategy is to release resources that can be re-built when the user returns to your app.
        if (level >= android.content.ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) {
            if (evictOnBackground) {
                this.clear()
            }
        }

        // deprecated in API 35: TRIM_MEMORY_COMPLETE, TRIM_MEMORY_MODERATE, TRIM_MEMORY_BACKGROUND, TRIM_MEMORY_UI_HIDDEN, TRIM_MEMORY_RUNNING_CRITICAL, TRIM_MEMORY_RUNNING_LOW, or TRIM_MEMORY_RUNNING_MODERATE
    }

    override fun onConfigurationChanged(config: android.content.res.Configuration) = Unit

}

/// A cache that takes an optional cost evaluator function to determine the cost of an entry
private class LRUCostCache<Key, Value>: android.util.LruCache<Key, Value> {
    internal val cost: ((Value) -> Int)?

    internal constructor(limit: Int? = null, cost: ((Value) -> Int)? = null): super(limit ?: Int.MAX_VALUE) {
        this.cost = cost
    }

    /// Returns the size of the entry for key and value in user-defined units. The default implementation returns 1 so that size is the number of entries and max size is the maximum number of entries.
    override fun sizeOf(key: Key, value: Value): Int {
        val matchtarget_0 = cost
        if (matchtarget_0 != null) {
            val cost = matchtarget_0
            return cost(value)
        } else {
            return super.sizeOf(key, value) // i.e., 1
        }
    }
}

