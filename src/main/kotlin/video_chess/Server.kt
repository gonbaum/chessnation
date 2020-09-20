package video_chess

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.response.respond
import io.ktor.response.respondFile
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import java.io.File

val rooms = RoomsHandler()

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
    val sourcesPath = "src/frontendMain"
    install(Routing) {
        static("static") {
            files("$sourcesPath/resources/pieces")
        }
        webSocket("/ws/{roomName}") {
            val roomName = call.parameters["roomName"]!!
            rooms.addPlayer(roomName, SocketPlayer(outgoing))
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()
                        log.info("On: $roomName, received: $text")
                        if (text == "reset") {
                            rooms.resetPosition(roomName)
                        } else {
                            rooms.updatePosition(roomName, text)
                        }
                    }
                }
            }
        }
        get("/") {
            call.respondFile(File("$sourcesPath/index.html"))
        }
        get("/room/{roomName}") {
            val roomName = call.parameters["roomName"]!!
            log.info("GET: $roomName")
            if (roomName.length >= 12 && !roomName.contains(Regex("\\s"))) {
                rooms.createRoom(roomName)
                log.info("$roomName created")
                call.respondFile(File("$sourcesPath/channel.html"))
            } else {
                log.error("$roomName not created")
                call.respond(HttpStatusCode.BadRequest, "incorrect room name")
            }
        }
    }
}

class SocketPlayer(private val outgoing: SendChannel<Frame>) : Player {
    override fun notify(message: String) {
        GlobalScope.launch {
            outgoing.send(Frame.Text(message))
        }
    }

    override fun isDisconnected(): Boolean {
        return outgoing.isClosedForSend
    }
}
