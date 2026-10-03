package com.insigniaempresarial.core.network

import android.content.Context
import com.insigniaempresarial.core.network.api.InsigniaApi
import com.insigniaempresarial.core.network.mock.MockInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object NetworkFactory {
    fun createApi(context: Context): InsigniaApi {
        val accountsJson = context.assets.open("mock/accounts.json").bufferedReader().use { it.readText() }
        val transactionsJson = context.assets.open("mock/transactions.json").bufferedReader().use { it.readText() }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(MockInterceptor(accountsJson, transactionsJson))
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl("https://api.insignia.local/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(InsigniaApi::class.java)
    }
}
