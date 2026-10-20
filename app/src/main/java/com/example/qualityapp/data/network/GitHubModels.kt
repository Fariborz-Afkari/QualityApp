package com.example.qualityapp.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * پاسخ GET روی یک فایل یا لیست یک پوشه.
 * اگر مسیر یک پوشه باشد، گیت‌هاب آرایه‌ای از این آیتم‌ها برمی‌گرداند.
 * اگر مسیر یک فایل باشد، یک آیتم واحد با فیلد content برمی‌گرداند.
 */
@JsonClass(generateAdapter = true)
data class GitHubContentItem(
    @Json(name = "name") val name: String,
    @Json(name = "path") val path: String,
    @Json(name = "sha") val sha: String,
    @Json(name = "size") val size: Long? = null,
    @Json(name = "type") val type: String, // "file" یا "dir"
    @Json(name = "content") val content: String? = null,
    @Json(name = "encoding") val encoding: String? = null,
    @Json(name = "download_url") val downloadUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class GitHubUpdateFileRequest(
    @Json(name = "message") val message: String,
    @Json(name = "content") val content: String, // Base64
    @Json(name = "sha") val sha: String? = null,  // اگر فایل موجود است
    @Json(name = "branch") val branch: String? = null
)

@JsonClass(generateAdapter = true)
data class GitHubUpdateFileResponse(
    @Json(name = "content") val content: GitHubContentItem? = null,
    @Json(name = "commit") val commit: GitHubCommit? = null
)

@JsonClass(generateAdapter = true)
data class GitHubCommit(
    @Json(name = "sha") val sha: String? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "html_url") val htmlUrl: String? = null
)