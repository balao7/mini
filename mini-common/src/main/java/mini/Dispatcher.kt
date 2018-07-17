package mini

class Dispatcher(private val verifyThreads: Boolean = true) {
    private val actionReducers: MutableList<ActionReducer> = ArrayList()
    private val interceptors: MutableList<Interceptor> = ArrayList()
    private val actionReducerLink: Chain = object : Chain {
        override fun proceed(action: Action): Action {
            actionReducers.forEach { it.reduce(action) }
            return action
        }
    }
    private var interceptorChain: Chain = buildChain()
    private var dispatching: Boolean = false

    private fun buildChain(): Chain {
        return interceptors.fold(actionReducerLink)
        { chain, interceptor ->
            object : Chain {
                override fun proceed(action: Action): Action = interceptor(action, chain)
            }
        }
    }

    fun addActionReducer(actionReducer: ActionReducer) {
        synchronized(this) {
            actionReducers.add(actionReducer)
        }
    }

    fun removeActionReducer(actionReducer: ActionReducer) {
        synchronized(this) {
            actionReducers.remove(actionReducer)
        }
    }

    fun addInterceptor(interceptor: Interceptor) {
        synchronized(this) {
            interceptors += interceptor
            interceptorChain = buildChain()
        }
    }

    fun removeInterceptor(interceptor: Interceptor) {
        synchronized(this) {
            interceptors -= interceptor
            interceptorChain = buildChain()
        }
    }

    /**
     * Post an event that will dispatch the action on the Ui thread
     * and return immediately.
     */
    fun dispatchOnUi(action: Action) {
        onUi { dispatch(action) }
    }

    /**
     * Post and event that will dispatch the action on the Ui thread
     * and block until the dispatch is complete.
     *
     * Can't be called from the main thread.
     */
    fun dispatchOnUiSync(action: Action) {
        if (verifyThreads) assertNotOnUiThread()
        onUiSync { dispatch(action) }
    }

    fun dispatch(action: Action) {
        if (verifyThreads) assertOnUiThread()
        if (dispatching) throw IllegalStateException("Nested dispatch calls")
        dispatching = true
        interceptorChain.proceed(action)
        dispatching = false
    }
}