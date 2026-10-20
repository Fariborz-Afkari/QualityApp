package com.example.qualityapp.data.repository

import android.util.Base64
import android.util.Log
import com.example.qualityapp.BuildConfig
import com.example.qualityapp.data.model.Questionnaire
import com.example.qualityapp.data.model.Submission
import com.example.qualityapp.data.network.GitHubApiService
import com.example.qualityapp.data.network.GitHubUpdateFileRequest
import com.example.qualityapp.data.prefs.UserPreferencesRepository
import com.squareup.moshi.Moshi
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionnaireRepository @Inject constructor(
    private val apiService: GitHubApiService,
    private val moshi: Moshi,
    private val prefs: UserPreferencesRepository
) {
    private val owner = BuildConfig.GITHUB_USERNAME
    private val repo  = BuildConfig.GITHUB_REPO

    /** توکن را از تنظیمات می‌خواند؛ اگر خالی باشد null برمی‌گرداند. */
    private suspend fun authHeader(): String? {
        val t = prefs.currentToken()
        return if (t.isBlank()) null else "Bearer $t"
    }

    // ---------------------------------------------------------------
    // 1) لیست پرسشنامه‌ها (فایل‌های .json داخل questionnaires/)
    // ---------------------------------------------------------------
    suspend fun listQuestionnaires(): List<String> {
        val response = apiService.listDirectory(
            owner = owner,
            repo = repo,
            path = QUESTIONNAIRES_DIR,
            authHeader = authHeader()
        )
        if (!response.isSuccessful) {
            throw IOException("خطا در دریافت لیست پرسشنامه‌ها (${response.code()})")
        }
        return response.body()
            .orEmpty()
            .filter { it.type == "file" && it.name.endsWith(".json", ignoreCase = true) }
            .map { it.name }
            .sorted()
    }

    // ---------------------------------------------------------------
    // 2) خواندن یک پرسشنامه با نام فایل
    // ---------------------------------------------------------------
    suspend fun loadQuestionnaire(fileName: String): Questionnaire {
        val path = "$QUESTIONNAIRES_DIR/$fileName"
        val response = apiService.getFile(
            owner = owner,
            repo = repo,
            path = path,
            authHeader = authHeader()
        )
        if (!response.isSuccessful) {
            throw IOException("فایل پرسشنامه یافت نشد (${response.code()})")
        }
        val item = response.body() ?: throw IOException("پاسخ خالی از گیت‌هاب")
        val base64 = item.content ?: throw IOException("محتوای فایل خالی است")
        val json = decodeBase64(base64)
        return moshi.adapter(Questionnaire::class.java).fromJson(json)
            ?: throw IOException("خطا در تبدیل JSON پرسشنامه")
    }

    // ---------------------------------------------------------------
    // 3) ذخیره پاسخ‌ها در پوشه submissions (ایجاد یا به‌روزرسانی)
    // ---------------------------------------------------------------
    suspend fun saveSubmission(fileName: String, submission: Submission): GitHubCommitResult {
        // بررسی وجود توکن — نوشتن بدون توکن ممکن نیست
        val token = prefs.currentToken()
        if (token.isBlank()) {
            throw IOException("برای ذخیره در گیت‌هاب باید توکن در تنظیمات وارد شود.")
        }
        val auth = "Bearer $token"

        val path = "$SUBMISSIONS_DIR/$fileName"
        val json = moshi.adapter(Submission::class.java).toJson(submission)
        val base64Content = encodeBase64(json)

        // اگر فایل قبلاً وجود دارد، باید sha آن را بگیریم
        val existingSha = tryGetSha(path, auth)

        val request = GitHubUpdateFileRequest(
            message = "Add submission: $fileName",
            content = base64Content,
            sha = existingSha,
            branch = BuildConfig.GITHUB_BRANCH
        )

        val response = apiService.createOrUpdateFile(
            owner = owner,
            repo = repo,
            path = path,
            authHeader = auth,
            body = request
        )
        if (!response.isSuccessful) {
            val err = response.errorBody()?.string()
            Log.e(TAG, "saveSubmission failed: ${response.code()} - $err")
            throw IOException("ذخیره در گیت‌هاب ناموفق بود (${response.code()})")
        }

        val body = response.body()
        return GitHubCommitResult(
            filePath = path,
            commitSha = body?.commit?.sha,
            commitUrl = body?.commit?.htmlUrl
        )
    }

    // ---------------------------------------------------------------
    // کمکی‌ها
    // ---------------------------------------------------------------
    private suspend fun tryGetSha(path: String, auth: String): String? = try {
        val r = apiService.getFile(owner, repo, path, auth)
        if (r.isSuccessful) r.body()?.sha else null
    } catch (_: Exception) {
        null
    }

    private fun decodeBase64(content: String): String {
        val clean = content.replace("\n", "").replace("\r", "").replace(" ", "")
        return String(Base64.decode(clean, Base64.DEFAULT), Charsets.UTF_8)
    }

    private fun encodeBase64(text: String): String =
        Base64.encodeToString(text.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)

    data class GitHubCommitResult(
        val filePath: String,
        val commitSha: String?,
        val commitUrl: String?
    )

    companion object {
        private const val TAG = "QuestionnaireRepo"
        const val QUESTIONNAIRES_DIR = "questionnaires"
        const val SUBMISSIONS_DIR = "submissions"
    }
}