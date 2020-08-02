package network

import com.fasterxml.jackson.databind.JsonNode

interface JsonStreamProcessor {
    fun processJson(json: JsonNode?, context: ResponseContext?)
}