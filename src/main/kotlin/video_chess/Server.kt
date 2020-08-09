package video_chess

import io.ktor.application.*
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.content.*
import io.ktor.response.respondRedirect
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.channels.SendChannel

val channels = HashSet<SendChannel<Frame>>()
val boardHandler = BoardHandler()

fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 8080
    embeddedServer(
        factory = Netty,
        port = port,
        module = Application::module
    ).start()
}

fun Application.module() {
    install(WebSockets) {}
    install(Routing) {
        static("static") {
            files("pieces")
            file("index.html")
        }
        webSocket("/ws") {
            channels.add(outgoing)
            if (channels.size > 1) {
                log.info("Channels count ${channels.size}")
                sendStartGameSequence()
            }
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()
                        log.info("Received on web socket: $text")
                        boardHandler.updateBoard(text)
                        channels.filter { it != outgoing }.forEach { it.send(Frame.Text(text)) }
                    }
                }
            }
        }
        get("/") {
            call.respondRedirect("/static/index.html", permanent = true)
        }
    }
}

private suspend fun sendStartGameSequence() {
    listOf("w", "b").shuffled().zip(channels)
        .forEach { it.second.send(Frame.Text(it.first)) }
    channels.forEach { it.send(Frame.Text("start")) }
}
