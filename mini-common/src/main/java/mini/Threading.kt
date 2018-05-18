package mini

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import java.util.concurrent.Semaphore

val uiHandler by lazy { Handler(Looper.getMainLooper()) }

fun assertNotOnUiThread() {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        error("This method can not be called from the main application thread")
    }
}

fun assertOnUiThread() {
    if (Looper.myLooper() != Looper.getMainLooper()) {
        error("This method can only be called from the main application thread")
    }
}

inline fun onUi(crossinline block: () -> Unit) {
    uiHandler.post { block() }
}

inline fun onUiDelayed(delay: Long, crossinline block: () -> Unit) {
    uiHandler.postDelayed({ block() }, delay)
}

inline fun onUiSync(crossinline block: () -> Unit) {
    assertNotOnUiThread()
    val sem = Semaphore(0)
    onUi {
        block()
        sem.release()
    }
    sem.acquireUninterruptibly()
}