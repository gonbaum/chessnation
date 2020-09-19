package video_chess

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.File

val rooms = mutableSetOf<Room>()

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
            files("${sourcesPath}/resources/pieces")
        }
        webSocket("/ws/{roomName}") {
            val roomName = call.parameters["roomName"]
            val room = rooms.find { it.name == roomName }
            if (room != null) {
                room.addSocket(outgoing)
                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            val text = frame.readText()
                            log.info("On: $roomName, received: $text")
                            if (text == "reset") {
                                room.resetBoard()
                            } else {
                                room.updateBoard(text)
                            }

                            room.removeClosedSockets()
                            room.notifySockets(text)
                        }
                    }
                }
            }
        }
        get("/") {
            call.respondFile(File("${sourcesPath}/index.html"))
        }
        get("/room/{roomName}") {
            val roomName = call.parameters["roomName"]!!
            log.info("GET: $roomName")
            if (rooms.any { it.name == roomName }) {
                call.respondFile(File("${sourcesPath}/channel.html"))
            } else if (roomName.length >= 12 && !roomName.contains(Regex("\\s"))) {
                rooms.add(Room(roomName))
                log.info("$roomName created")
                call.respondFile(File("${sourcesPath}/channel.html"))
            } else {
                log.error("$roomName not created")
                call.respond(HttpStatusCode.BadRequest, "incorrect room name")
            }
        }
    }
}
