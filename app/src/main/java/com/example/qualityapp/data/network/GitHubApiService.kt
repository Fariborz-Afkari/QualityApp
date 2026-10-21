package com.example.qualityapp.data.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path

interface GitHubApiService {

    // -- نسخه خام: پاسخ را به‌صورت ResponseBody برمی‌گرداند (برای دیباگ و پارس دستی) --
    @GET("repos/{owner}/{repo}/contents/{path}")
    suspend fun getContentsRaw(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String,
        @Header("Authorization") authHeader: String? = null
    ): Response<ResponseBody>

    @PUT("repos/{owner}/{repo}/contents/{path}")
    suspend fun createOrUpdateFile(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String,
        @Header("Authorization") authHeader: String? = null,
        @Body body: GitHubUpdateFileRequest
    ): Response<GitHubUpdateFileResponse>
}