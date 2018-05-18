package com.minivac.mini.flux

import mini.Grove
import mini.Store

/**
 * Handy alias to use with dagger
 */
typealias StoreMap = Map<Class<*>, Store<*>>

typealias LazyStoreMap = dagger.Lazy<Map<Class<*>, Store<*>>>

/**
 * Sort and create Stores initial state.
 */
fun initStores(uninitializedStores: Collection<Store<*>>) {
    val now = System.currentTimeMillis()

    val stores = uninitializedStores.toList()

    val initTimes = LongArray(stores.size)
    for (i in 0 until stores.size) {
        val start = System.currentTimeMillis()
        stores[i].init()
        stores[i].state //Create initial state
        initTimes[i] += System.currentTimeMillis() - start
    }

    val elapsed = System.currentTimeMillis() - now

    Grove.d { "┌ Application with ${stores.size} stores loaded in $elapsed ms" }
    Grove.d { "├────────────────────────────────────────────" }
    for (i in 0..stores.size - 1) {
        val store = stores[i]
        var boxChar = "├"
        if (store === stores[stores.size - 1]) {
            boxChar = "└"
        }
        Grove.d { "$boxChar ${store.javaClass.simpleName} - ${initTimes[i]} ms" }
    }
}

fun disposeStores(stores: Iterable<Store<*>>) {
    // stores.forEach { it.dispose() }
}