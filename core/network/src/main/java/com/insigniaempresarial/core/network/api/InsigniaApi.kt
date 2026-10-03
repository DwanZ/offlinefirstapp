package com.insigniaempresarial.core.network.api

import com.insigniaempresarial.core.network.dto.AccountDto
import com.insigniaempresarial.core.network.dto.TransactionDto
import com.insigniaempresarial.core.network.dto.UpsertTransactionRequest
import com.insigniaempresarial.core.network.dto.UpsertTransactionResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface InsigniaApi {
    @GET("v1/accounts")
    suspend fun getAccounts(@Query("since") since: Long? = null): List<AccountDto>

    @GET("v1/transactions")
    suspend fun getTransactions(@Query("since") since: Long? = null): List<TransactionDto>

    @POST("v1/transactions")
    suspend fun upsertTransaction(@Body body: UpsertTransactionRequest): UpsertTransactionResponse
}
