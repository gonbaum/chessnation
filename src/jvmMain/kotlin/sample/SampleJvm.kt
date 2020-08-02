package sample

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.content.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.html.*
import network.JsonClient
import network.JsonStreamProcessor
import network.ResponseContext
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicHeader
import org.slf4j.LoggerFactory

fun main() {
    embeddedServer(Netty, port = 8080, host = "127.0.0.1") {
        routing {
            get("/") {
                streamIncomingEvents()
                call.respondHtml {
                    head {
                        title("Hello from Ktor!")
                    }
                }
            }
            static("/static") {
                resource("video-chess.js")
            }
        }
    }.start(wait = true)
}

const val API_URL = "https://lichess.org/api"
val headers = listOf(BasicHeader("Authorization", "Bearer ___"))
val httpClient =
    JsonClient(HttpClientBuilder.create().setDefaultHeaders(headers).build())

fun streamIncomingEvents() {
    println("start string incoming events")
    httpClient.getAndStream("${API_URL}/stream/event", AcceptAllStreamProcessor())
}

class AcceptAllStreamProcessor : JsonStreamProcessor {
    override fun processJson(json: JsonNode?, context: ResponseContext?) {
        val node = json as ObjectNode
        val type = node.get("type").asText()
        log.info("Received JSON: $node")
        if (type == "challenge") {
            val challengeId = node.get("challenge").get("id").asText()
            httpClient.post("${API_URL}/challenge/$challengeId/accept")
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(AcceptAllStreamProcessor::class.java)
    }
}


