package com.example.qualityapp.data

import android.content.Context
import android.net.Uri
import com.example.qualityapp.data.prefs.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: UserPreferencesRepository
) {
    /** فایل ممکن است در پوشه‌ای باشد که فایل‌منیجر نمی‌بیند؛ ولی متد getToken سالم است. */
    fun tokenFile(): File =
        File(context.getExternalFilesDir(null) ?: context.filesDir, "token.txt")

    fun tokenFilePath(): String = tokenFile().absolutePath

    /**
     * اولویت:
     *  1) فایل token.txt روی گوشی (اگر توسط ADB یا ابزار خاصی ساخته شده باشد)
     *  2) توکن ذخیره‌شده در صفحه تنظیمات (از جمله توکن بارگذاری‌شده از SAF)
     */
    suspend fun getToken(): String = withContext(Dispatchers.IO) {
        val f = tokenFile()
        if (f.exists()) {
            val fromFile = runCatching { f.readText(Charsets.UTF_8).trim() }.getOrNull()
            if (!fromFile.isNullOrBlank()) return@withContext fromFile
        }
        prefs.currentToken().trim()
    }

    suspend fun sourceLabel(): String = withContext(Dispatchers.IO) {
        val f = tokenFile()
        if (f.exists()) {
            val fromFile = runCatching { f.readText(Charsets.UTF_8).trim() }.getOrNull()
            if (!fromFile.isNullOrBlank()) return@withContext "فایل روی گوشی"
        }
        val p = prefs.currentToken().trim()
        if (p.isNotBlank()) "تنظیمات" else "خالی"
    }

    suspend fun tokenLength(): Int = getToken().length

    /**
     * بارگذاری توکن از فایلی که کاربر با SAF انتخاب کرده.
     * محتوا trim می‌شود، سپس در DataStore ذخیره می‌شود.
     * نتیجه: Result<String> با مقدار توکن یا خطا.
     */
    suspend fun importFromUri(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val text = context.contentResolver.openInputStream(uri)?.use { input ->
                input.bufferedReader(Charsets.UTF_8).readText()
            } ?: throw IOException("فایل باز نشد")

            val token = text.trim()
            if (token.isEmpty()) throw IOException("فایل خالی است")
            if (token.contains(' ')) throw IOException("فایل شامل فاصله است — فقط خود توکن را بگذارید")
            if (token.contains('\n')) throw IOException("فایل چند خطی است — فقط یک خط شامل توکن باشد")

            prefs.saveToken(token)
            token
        }
    }
}