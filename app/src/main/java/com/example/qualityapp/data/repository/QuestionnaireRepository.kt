package com.example.qualityapp.data.repository

import android.util.Base64
import android.util.Log
import com.example.qualityapp.BuildConfig
import com.example.qualityapp.data.Secrets
import com.example.qualityapp.data.model.Questionnaire
import com.example.qualityapp.data.model.Submission
import com.example.qualityapp.data.network.GitHubApiService
import com.example.qualityapp.data.network.GitHubContentItem
import com.example.qualityapp.data.network.GitHubUpdateFileRequest
import com.example.qualityapp.data.prefs.UserPreferencesRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
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
    private val dataRepo = BuildConfig.GITHUB_DATA_REPO
    private val branch = BuildConfig.GITHUB_BRANCH
    private val questionnairesPath = BuildConfig.QUESTIONNAIRES_PATH
    private val submissionsPath = BuildConfig.SUBMISSIONS_PATH

    /**
     * اولویت: توکن hardcoded در Secrets، سپس توکن ذخیره‌شده در تنظیمات.
     */
    private suspend fun authHeader(): String? {
        val hardcoded = Secrets.GITHUB_TOKEN
        val token = if (hardcoded.isNotBlank() && !hardcoded.startsWith("ghp_XXXX"))
            hardcoded
        else
            prefs.currentToken()
        return if (token.isBlank()) null else "Bearer $token"
    }

    // ---------------------------------------------------------------
    // 1) لیست پرسشنامه‌ها با لاگ کامل و پیام خطای دقیق
    // ---------------------------------------------------------------
    suspend fun listQuestionnaires(): List<String> {
        val path = questionnairesPath.ifBlank { "" }
        Log.d(TAG, "GET contents owner=$owner repo=$dataRepo path='$path'")

        val response = try {
            apiService.getContentsRaw(
                owner = owner,
                repo = dataRepo,
                path = path,
                authHeader = authHeader()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Network error while listing", e)
            throw IOException("خطای شبکه: ${e.javaClass.simpleName}: ${e.message}", e)
        }

        // خطای HTTP
        if (!response.isSuccessful) {
            val err = runCatching { response.errorBody()?.string() }.getOrNull()
            Log.e(TAG, "HTTP ${response.code()}: $err")
            val userMsg = when (response.code()) {
                401 -> "توکن نامعتبر است یا دسترسی ندارد (401)"
                403 -> "محدودیت نرخ یا دسترسی ناکافی (403)"
                404 -> "مسیر '$path' در مخزن $dataRepo/$owner پیدا نشد (404)"
                else -> "خطای HTTP ${response.code()}"
            }
            throw IOException("$userMsg\n\n$err".trim())
        }

        // خواندن بدنه
        val raw = runCatching { response.body()?.string() }.getOrNull()
        if (raw.isNullOrBlank()) {
            throw IOException("پاسخ گیت‌هاب خالی بود")
        }
        Log.d(TAG, "RAW (${raw.length} chars): ${raw.take(800)}")

        // پارس دستی — پاسخ می‌تواند آرایه یا شیء باشد
        return try {
            val trimmed = raw.trim()
            val items: List<GitHubContentItem> = when {
                trimmed.startsWith("[") -> {
                    val type = Types.newParameterizedType(
                        List::class.java, GitHubContentItem::class.java
                    )
                    moshi.adapter<List<GitHubContentItem>>(type).fromJson(raw)
                        ?: emptyList()
                }
                trimmed.startsWith("{") -> {
                    val single = moshi.adapter(GitHubContentItem::class.java).fromJson(raw)
                    if (single != null) listOf(single) else emptyList()
                }
                else -> throw IOException("پاسخ غیرمنتظره: ${raw.take(200)}")
            }

            items.filter { it.type == "file" && it.name.endsWith(".json", true) }
                .map { it.name }
                .sorted()
                .also { Log.d(TAG, "Parsed ${it.size} questionnaires: $it") }

        } catch (e: Exception) {
            Log.e(TAG, "Parse error. RAW=$raw", e)
            throw IOException("خطای پارس JSON: ${e.message}\n\nRAW: ${raw.take(300)}", e)
        }
    }

    // ---------------------------------------------------------------
    // 2) خواندن پرسشنامه (همان منطق خام)
    // ---------------------------------------------------------------
    suspend fun loadQuestionnaire(fileName: String): Questionnaire {
        val dir = questionnairesPath.ifBlank { "" }
        val path = if (dir.isBlank()) fileName else "$dir/$fileName"
        Log.d(TAG, "GET file $path")

        val response = try {
            apiService.getContentsRaw(owner, dataRepo, path, authHeader())
        } catch (e: Exception) {
            throw IOException("خطای شبکه: ${e.message}", e)
        }

        if (!response.isSuccessful) {
            val err = runCatching { response.errorBody()?.string() }.getOrNull()
            throw IOException("HTTP ${response.code()}: ${err?.take(300) ?: "بدون بدنه"}")
        }

        val raw = response.body()?.string() ?: throw IOException("پاسخ خالی")
        val item = try {
            moshi.adapter(GitHubContentItem::class.java).fromJson(raw)
        } catch (e: Exception) {
            throw IOException("خطای پارس: ${e.message}", e)
        } ?: throw IOException("پاسخ null")

        val base64 = item.content ?: throw IOException("محتوای فایل خالی")
        val json = String(
            Base64.decode(base64.replace("\n", "").replace("\r", ""), Base64.DEFAULT),
            Charsets.UTF_8
        )
        return moshi.adapter(Questionnaire::class.java).fromJson(json)
            ?: throw IOException("خطا در تبدیل پرسشنامه")
    }

    // ---------------------------------------------------------------
    // 3) ذخیره پاسخ‌ها
    // ---------------------------------------------------------------
    suspend fun saveSubmission(fileName: String, submission: Submission): GitHubCommitResult {
        val token = authHeader() ?: throw IOException(
            "برای ذخیره باید توکن داشته باشید (Secrets.kt یا صفحه تنظیمات)"
        )
        val dir = submissionsPath.ifBlank { "submissions" }
        val path = "$dir/$fileName"

        val json = moshi.adapter(Submission::class.java).toJson(submission)
        val base64 = Base64.encodeToString(json.toByteArray(), Base64.NO_WRAP)

        val existingSha = runCatching {
            apiService.getContentsRaw(owner, dataRepo, path, token)
                .let { if (it.isSuccessful) {
                    moshi.adapter(GitHubContentItem::class.java)
                        .fromJson(it.body()?.string() ?: "")?.sha
                } else null }
        }.getOrNull()

        val req = GitHubUpdateFileRequest(
            message = "Add submission: $fileName",
            content = base64,
            sha = existingSha,
            branch = branch
        )
        val response = apiService.createOrUpdateFile(owner, dataRepo, path, token, req)
        if (!response.isSuccessful) {
            val err = runCatching { response.errorBody()?.string() }.getOrNull()
            throw IOException("ذخیره ناموفق (${response.code()}): ${err?.take(300)}")
        }
        val body = response.body()
        return GitHubCommitResult(path, body?.commit?.sha, body?.commit?.htmlUrl)
    }

    data class GitHubCommitResult(
        val filePath: String, val commitSha: String?, val commitUrl: String?
    )

    companion object { private const val TAG = "QuestionnaireRepo" }
}