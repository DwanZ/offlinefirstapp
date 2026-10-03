package com.insigniaempresarial.core.network.mock

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.util.UUID
import kotlin.random.Random

/**
 * Deterministic local mock API. Serves fixtures and simulates latency / intermittent 500s
 * so offline retry and FAILED sync UI can be exercised without a backend.
 */
class MockInterceptor(
    private val accountsJson: String,
    private val transactionsJson: String,
    private val failPostRate: Double = 0.12,
    private val random: Random = Random.Default,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        Thread.sleep(180)
        val request = chain.request()
        val path = request.url.encodedPath
        val method = request.method

        return when {
            method == "GET" && path.endsWith("/v1/accounts") ->
                success(request, accountsJson)

            method == "GET" && path.endsWith("/v1/transactions") ->
                success(request, transactionsJson)

            method == "POST" && path.endsWith("/v1/transactions") -> {
                if (random.nextDouble() < failPostRate) {
                    error(request, 500, """{"message":"mock upstream failure"}""")
                } else {
                    val remoteId = "remote_${UUID.randomUUID()}"
                    val body = """
                        {"id":"$remoteId","remoteId":"$remoteId","clientMutationId":"accepted"}
                    """.trimIndent()
                    success(request, body)
                }
            }

            else -> error(request, 404, """{"message":"mock route not found"}""")
        }
    }

    private fun success(request: okhttp3.Request, json: String): Response =
        Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(json.toResponseBody(JSON))
            .build()

    private fun error(request: okhttp3.Request, code: Int, json: String): Response =
        Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("Error")
            .body(json.toResponseBody(JSON))
            .build()

    companion object {
        private val JSON = "application/json".toMediaType()
    }
}
