package com.example.qualityapp.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * لاگ ساده در حافظه که در پنل دیباگ اپ نشان داده می‌شود.
 * حداکثر ۲۰۰ خط نگه می‌دارد تا حافظه پر نشود.
 */
object DebugLog {
    private val fmt = SimpleDateFormat("HH:mm:ss", Locale.US)
    private val _lines = MutableStateFlow<List<String>>(emptyList())
    val lines: StateFlow<List<String>> = _lines.asStateFlow()

    fun log(tag: String, message: String) {
        val line = "[${fmt.format(Date())}] $tag: $message"
        _lines.update { (it + line).takeLast(200) }
    }

    fun error(tag: String, message: String, t: Throwable? = null) {
        log(tag, "❌ $message")
        if (t != null) {
            log(tag, "   نوع: ${t.javaClass.simpleName}")
            log(tag, "   پیام: ${t.message}")
            t.stackTrace.take(12).forEach { log(tag, "   at $it") }
            t.cause?.let { c ->
                log(tag, "   cause: ${c.javaClass.simpleName}: ${c.message}")
            }
        }
    }

    fun clear() = _lines.update { emptyList() }
}