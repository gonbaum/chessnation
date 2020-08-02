package network

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import java.io.IOException
import java.io.InputStream

object Json {
    private val objectMapper = ObjectMapper()
    fun readJson(json: String?): JsonNode {
        return try {
            objectMapper.readTree(json)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    @JvmStatic
    fun readJson(inputStream: InputStream?): JsonNode {
        return try {
            objectMapper.readTree(inputStream)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun <T> parseJson(inputStream: InputStream?, c: Class<T>?): T {
        return try {
            objectMapper.readValue(inputStream, c)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun <T> parseJson(node: JsonNode?, c: Class<T>?): T {
        return try {
            objectMapper.treeToValue(node, c)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    @JvmStatic
    fun writeObjectToJson(`object`: Any?): ObjectNode {
        return try {
            readJson(objectMapper.writeValueAsString(`object`)) as ObjectNode
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun createJsonObject(): ObjectNode {
        return JsonNodeFactory.instance.objectNode()
    }
}