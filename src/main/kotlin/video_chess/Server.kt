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
import io.ktor.websocket.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import org.slf4j.Logger

val channels = HashSet<SendChannel<Frame>>()
val boardHandler = BoardHandler()

@ExperimentalCoroutinesApi
fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 8080
    embeddedServer(
        factory = Netty,
        port = port,
        module = Application::module
    ).start()
}

@ExperimentalCoroutinesApi // because of isClosedForSend from SendChannel
fun Application.module() {
    install(WebSockets) {}
    install(Routing) {
        static("static") {
            files("pieces")
            file("index.html")
        }
        webSocket("/ws") {
            handleNewClient(log)
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()
                        log.info("Received on web socket: $text")
                        if (text == "reset") {
                            boardHandler.resetBoard()
                        } else {
                            boardHandler.updateBoard(text)
                        }
                        channels.removeIf {
                            log.warn("Removing closed channel: $it")
                            it.isClosedForSend
                        }
                        channels.forEach {
                            log.info("Sending to: $it")
                            it.send(Frame.Text(text))
                        }
                    }
                }
            }
        }
        get("/") {
            call.respondRedirect("/static/index.html", permanent = true)
        }
    }
}

private suspend fun DefaultWebSocketServerSession.handleNewClient(log: Logger) {
    log.info("Adding new channel: $outgoing")
    channels.add(outgoing)
    outgoing.send(Frame.Text("fen|${boardHandler.getFen()}"))
}

