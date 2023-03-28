package dev.josearaujo.espresso

import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import java.lang.reflect.Method
import java.util.concurrent.ExecutorService

internal fun OkHttpClient.compat(): OkHttp3Compat {
    val getDispatcherMethod = javaClass.getMethod("dispatcher")
    val dispatcher = getDispatcherMethod.invoke(this@compat) as Dispatcher

    val executorServiceMethod = dispatcher.javaClass.getMethod("executorService")
    val executorService = executorServiceMethod.invoke(dispatcher) as ExecutorService

    val dispatcherCompat: OkHttp3DispatcherCompat = runCatching {

        val setIdleCallbackMethod = dispatcher.javaClass.getMethod("setIdleCallback", Runnable::class.java)

        // running on OkHttp version 3.x
        DispatcherCompat(
            dispatcherDelegate = dispatcher,
            executorService = executorService,
            setIdleCallbackMethod = setIdleCallbackMethod
        )
    }.onFailure {
        // running on OkHttp version >= 4.x
        val setIdleCallbackMethod = dispatcher.javaClass.getMethod("idleCallback", Runnable::class.java)

        DispatcherCompat(
            dispatcherDelegate = dispatcher,
            executorService = executorService,
            setIdleCallbackMethod = setIdleCallbackMethod
        )
    }.getOrThrow()

    return object: OkHttp3Compat {
        override val clientDelegate: OkHttpClient = this@compat

        override val dispatcherCompat: OkHttp3DispatcherCompat = dispatcherCompat
    }
}

internal interface OkHttp3Compat {
    val clientDelegate: OkHttpClient
    val dispatcherCompat: OkHttp3DispatcherCompat
}

internal interface OkHttp3DispatcherCompat {
    val dispatcherDelegate: Dispatcher
    val executorService: ExecutorService
    fun setIdleCallbackCompat(runnable: Runnable?)
}

private class DispatcherCompat(
    override val dispatcherDelegate: Dispatcher,
    override val executorService: ExecutorService,
    private val setIdleCallbackMethod: Method
): OkHttp3DispatcherCompat {

    override fun setIdleCallbackCompat(runnable: Runnable?) {
        setIdleCallbackMethod.invoke(dispatcherDelegate, runnable)
    }
}