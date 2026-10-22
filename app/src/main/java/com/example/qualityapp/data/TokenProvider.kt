package com.example.qualityapp.data

import android.content.Context
import com.example.qualityapp.data.prefs.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: UserPreferencesRepository
) {
    fun tokenFile(): File =
        File(context.getExternalFilesDir(null) ?: context.filesDir, "token.txt")

    fun tokenFilePath(): String = tokenFile().absolutePath

    /**
     * اولویت:
     *  1) فایل token.txt روی گوشی (اگر وجود داشت و خالی نبود)
     *  2) توکن ذخیره‌شده در صفحه تنظیمات (DataStore)
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
}