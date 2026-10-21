package com.example.qualityapp.data.repository

import android.util.Base64
import android.util.Log
import com.example.qualityapp.BuildConfig
import com.example.qualityapp.data.model.Questionnaire
import com.example.qualityapp.data.model.Submission
import com.example.qualityapp.data.network.GitHubApiService
import com.example.qualityapp.data.network.GitHubContentItem
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
    // مالک هر دو مخزن یکسان است، اما مخزن داده MetroQuality است
    private val owner = BuildConfig.GITHUB_USERNAME
    private val dataRepo = BuildConfig.GITHUB_DATA_REPO
    private val branch = BuildConfig.GITHUB_BRANCH
    private val questionnairesPath = BuildConfig.QUESTIONNAIRES_PATH
    private val submissionsPath = BuildConfig.SUBMISSIONS_PATH

    private suspend fun authHeader(): String? {
        val t = prefs.currentToken()
        return if (t.isBlank()) null else "Bearer $t"
    }

    // ---------------------------------------------------------------
    // 1) لیست پرسشنامه‌های موجود
    // ---------------------------------------------------------------
    suspend fun listQuestionnaires(): List<String> {
        val path = questionnairesPath.ifBlank { "" }
        val response = apiService.listDirectory(
            owner = owner,
            repo = dataRepo,
            path = path,
            authHeader = authHeader()
        )
        if (!response.isSuccessful) {
            throw IOException("خطا در دریافت لیست پرسشنامه‌ها (${response.code()})")
        }

        val body = response.body()
        // اگر path خالی باشد و ریشه شامل فایل باشد، GitHub ممکن است یک شیء برگرداند نه آرایه
        // اما در عمل با Accept: application/vnd.github+json آرایه برمی‌گرداند
        val items: List<GitHubContentItem> = body ?: emptyList()

        return items
            .filter { it.type == "file" && it.name.endsWith(".json", ignoreCase = true) }
            .map { it.name }
            .sorted()
    }

    // ---------------------------------------------------------------
    // 2) خواندن یک پرسشنامه با نام فایل
    // ---------------------------------------------------------------
    suspend fun loadQuestionnaire(fileName: String): Questionnaire {
        val dir = questionnairesPath.ifBlank { "" }
        val path = if (dir.isBlank()) fileName else "$dir/$fileName"

        val response = apiService.getFile(
            owner = owner,
            repo = dataRepo,
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
    // 3) ذخیره پاسخ‌ها در مخزن داده
    // ---------------------------------------------------------------
    suspend fun saveSubmission(fileName: String, submission: Submission): GitHubCommitResult {
        val token = prefs.currentToken()
        if (token.isBlank()) {
            throw IOException("برای ذخیره در گیت‌هاب باید توکن در تنظیمات وارد شود.")
        }
        val auth = "Bearer $token"

        val dir = submissionsPath.ifBlank { "submissions" }
        val path = "$dir/$fileName"

        val json = moshi.adapter(Submission::class.java).toJson(submission)
        val base64Content = encodeBase64(json)

        val existingSha = tryGetSha(path, auth)

        val request = GitHubUpdateFileRequest(
            message = "Add submission: $fileName",
            content = base64Content,
            sha = existingSha,
            branch = branch
        )

        val response = apiService.createOrUpdateFile(
            owner = owner,
            repo = dataRepo,
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
        val r = apiService.getFile(owner, dataRepo, path, auth)
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
    }
}