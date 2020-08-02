package network

import com.fasterxml.jackson.databind.JsonNode
import network.Json.parseJson
import network.Json.readJson
import org.apache.http.client.methods.CloseableHttpResponse
import java.io.IOException

class JsonResponse(private val httpResponse: CloseableHttpResponse) : AutoCloseable {
    fun toJson(): JsonNode {
        return try {
            readJson(httpResponse.entity.content)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun <T> toObject(c: Class<T>?): T {
        return try {
            parseJson(httpResponse.entity.content, c)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    override fun close() {
        try {
            httpResponse.close()
        } catch (e: IOException) {
            //closing quietly!
        }
    }

}