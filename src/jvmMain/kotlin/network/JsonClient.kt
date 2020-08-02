package network

import com.fasterxml.jackson.databind.JsonNode
import network.IOUtils.closeQuietly
import network.Json.readJson
import network.Json.writeObjectToJson
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class JsonClient(private val client: CloseableHttpClient) : AutoCloseable {
    fun getAndStream(url: String?, processor: JsonStreamProcessor) {
        try {
            execute(HttpGet(url)).use { response -> streamJsonResponse(response, processor) }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private fun streamJsonResponse(
        response: HttpResponse,
        processor: JsonStreamProcessor
    ) {
        val entity = response.entity
        try {
            val reader =
                BufferedReader(InputStreamReader(entity.content)) //Reader will close when connection closes
            val responseContext = ResponseContext(true)
            while (true) {
                Thread.sleep(10)
                if (!responseContext.isRunning) {
                    break
                }
                val line = reader.readLine()
                if (line != null && line != "") {
                    processor.processJson(readJson(line), responseContext)
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
    }

    fun get(url: String?): JsonResponse {
        return JsonResponse(execute(HttpGet(url)))
    }

    fun post(url: String?, body: Any?): JsonResponse {
        return post(url, writeObjectToJson(body))
    }

    @JvmOverloads
    fun post(url: String?, json: JsonNode? = null): JsonResponse {
        val httpPost = HttpPost(url)
        if (json != null) {
            val body: HttpEntity = StringEntity(json.toString(), ContentType.APPLICATION_JSON)
            httpPost.entity = body
        }
        return JsonResponse(execute(httpPost))
    }

    private fun execute(request: HttpUriRequest): CloseableHttpResponse {
        log.info("Making request: " + request.method + " " + request.uri)
        val response: CloseableHttpResponse
        response = try {
            client.execute(request)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        checkResponse(response)
        return response
    }

    private fun checkResponse(response: CloseableHttpResponse) {
        val statusLine = response.statusLine
        log.info("Got response: " + response.statusLine)
        val statusCode = statusLine.statusCode
        if (statusCode in 400..599) {
            closeQuietly(response)
            throw ResponseException(statusCode, statusLine.reasonPhrase)
        }
    }

    @Throws(Exception::class)
    override fun close() {
        client.close()
    }

    companion object {
        private val log = LoggerFactory.getLogger(JsonClient::class.java)
    }

}