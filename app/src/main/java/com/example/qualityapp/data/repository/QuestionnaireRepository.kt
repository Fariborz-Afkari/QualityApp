package com.example.qualityapp.data.repository

import android.util.Base64
import com.example.qualityapp.BuildConfig
import com.example.qualityapp.data.DebugLog
import com.example.qualityapp.data.TokenProvider
import com.example.qualityapp.data.model.Questionnaire
import com.example.qualityapp.data.model.Submission
import com.example.qualityapp.data.network.GitHubApiService
import com.example.qualityapp.data.network.GitHubContentItem
import com.example.qualityapp.data.network.GitHubUpdateFileRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionnaireRepository @Inject constructor(
    private val apiService: GitHubApiService,
    private val moshi: Moshi,
    private val tokenProvider: TokenProvider
) {
    private val owner = BuildConfig.GITHUB_USERNAME
    private val dataRepo = BuildConfig.GITHUB_DATA_REPO
    private val branch = BuildConfig.GITHUB_BRANCH
    private val questionnairesPath = BuildConfig.QUESTIONNAIRES_PATH
    private val submissionsPath = BuildConfig.SUBMISSIONS_PATH

    private suspend fun authHeader(): String? {
        val token = tokenProvider.getToken()
        return if (token.isBlank()) null else "Bearer $token"
    }

    // ---------------------------------------------------------------
    suspend fun listQuestionnaires(): List<String> {
        val path = questionnairesPath.ifBlank { "" }
        val token = authHeader()

        DebugLog.log("Config", "owner=$owner")
        DebugLog.log("Config", "repo=$dataRepo")
        DebugLog.log("Config", "path='$path'")
        DebugLog.log("Config", "branch=$branch")
        DebugLog.log("Token", "منبع: ${tokenProvider.sourceLabel()}")
        DebugLog.log("Token", "طول: ${tokenProvider.tokenLength()} کاراکتر")

        return try {
            DebugLog.log("HTTP", "GET repos/$owner/$dataRepo/contents/$path ...")
            val response = apiService.getContentsRaw(owner, dataRepo, path, token)
            DebugLog.log("HTTP", "Status: ${response.code()}")

            if (!response.isSuccessful) {
                val err = runCatching { response.errorBody()?.string() }.getOrNull()
                DebugLog.log("HTTP", "Error body: ${err?.take(500)}")
                throw IOException(
                    when (response.code()) {
                        401 -> "توکن نامعتبر است (401). توکن را در تنظیمات بررسی کنید."
                        403 -> "دسترسی کافی نیست یا Rate Limit (403)"
                        404 -> "مسیر '$path' پیدا نشد (404)"
                        else -> "HTTP ${response.code()}"
                    }
                )
            }

            val raw = response.body()?.string().orEmpty()
            DebugLog.log("HTTP", "RAW length: ${raw.length} chars")
            DebugLog.log("HTTP", "RAW starts with: '${raw.trim().take(2)}'")
            DebugLog.log("HTTP", "RAW preview: ${raw.take(1500)}")

            val trimmed = raw.trim()
            val items: List<GitHubContentItem> = when {
                trimmed.startsWith("[") -> {
                    val type = Types.newParameterizedType(
                        List::class.java, GitHubContentItem::class.java
                    )
                    val parsed = moshi.adapter<List<GitHubContentItem>>(type).fromJson(raw)
                    DebugLog.log("Parse", "آرایه شناسایی شد — ${parsed?.size ?: 0} آیتم پارس شد")
                    parsed ?: emptyList()
                }
                trimmed.startsWith("{") -> {
                    val parsed = moshi.adapter(GitHubContentItem::class.java).fromJson(raw)
                    DebugLog.log("Parse", "شیء تکی شناسایی شد — ${if (parsed != null) 1 else 0} آیتم")
                    if (parsed != null) listOf(parsed) else emptyList()
                }
                else -> throw IOException("پاسخ غیرمنتظره: ${raw.take(200)}")
            }

            // ---- لاگ کامل هر آیتم قبل از فیلتر ----
            items.forEachIndexed { i, item ->
                DebugLog.log("Item[$i]", "name='${item.name}', type='${item.type}', size=${item.size}")
            }
            DebugLog.log("Filter", "تعداد کل آیتم‌های دریافتی: ${items.size}")

            // ---- فیلتر مرحله‌به‌مرحله با لاگ ----
            val files = items.filter { it.type == "file" }
            DebugLog.log("Filter", "بعد از type=file → ${files.size} آیتم")

            val jsonFiles = files.filter { it.name.endsWith(".json", ignoreCase = true) }
            DebugLog.log("Filter", "بعد از endsWith(.json) → ${jsonFiles.size} آیتم")

            val names = jsonFiles.map { it.name }.sorted()
            DebugLog.log("OK", "لیست نهایی (${names.size}): $names")

            names

        } catch (e: Exception) {
            DebugLog.error("listQuestionnaires", "شکست خورد", e)
            throw IOException(e.message ?: "خطای نامشخص", e)
        }
    }

    // ---------------------------------------------------------------
    suspend fun loadQuestionnaire(fileName: String): Questionnaire {
        val dir = questionnairesPath.ifBlank { "" }
        val path = if (dir.isBlank()) fileName else "$dir/$fileName"

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
        val item = moshi.adapter(GitHubContentItem::class.java).fromJson(raw)
            ?: throw IOException("پاسخ null")

        val base64 = item.content ?: throw IOException("محتوای فایل خالی")
        val json = String(
            Base64.decode(base64.replace("\n", "").replace("\r", ""), Base64.DEFAULT),
            Charsets.UTF_8
        )
        return moshi.adapter(Questionnaire::class.java).fromJson(json)
            ?: throw IOException("خطا در تبدیل پرسشنامه")
    }

    // ---------------------------------------------------------------
    suspend fun saveSubmission(fileName: String, submission: Submission): GitHubCommitResult {
        val token = authHeader() ?: throw IOException(
            "توکن در دسترس نیست. فایل token.txt یا صفحه تنظیمات را بررسی کنید."
        )
        val dir = submissionsPath.ifBlank { "submissions" }
        val path = "$dir/$fileName"

        val json = moshi.adapter(Submission::class.java).toJson(submission)
        val base64 = Base64.encodeToString(json.toByteArray(), Base64.NO_WRAP)

        val existingSha = runCatching {
            apiService.getContentsRaw(owner, dataRepo, path, token).let {
                if (it.isSuccessful) {
                    moshi.adapter(GitHubContentItem::class.java)
                        .fromJson(it.body()?.string() ?: "")?.sha
                } else null
            }
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
}