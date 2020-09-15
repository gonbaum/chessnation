package video_chess

import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import java.io.File

data class Room(
    // todo: add name
    val board: BoardHandler = BoardHandler(),
    val sockets: MutableSet<SendChannel<Frame>> = mutableSetOf()
) {

    fun resetBoard() {
        board.resetBoard()
    }

    fun updateBoard(text: String) {
        board.updateBoard(text)
    }

    suspend fun addSocket(socket: SendChannel<Frame>) {
        sockets.add(socket)
        socket.send(Frame.Text("fen|${board.getFen()}"))
    }

    @ExperimentalCoroutinesApi
    fun removeClosedSockets() {
        val closedSockets = sockets
            .filter { it.isClosedForSend }
        sockets.removeAll(closedSockets)
    }

    suspend fun notifySockets(message: String) {
        sockets.forEach {
            it.send(Frame.Text(message))
        }
    }
}

val rooms = mutableMapOf<String, Room>()

@ExperimentalCoroutinesApi
fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 8080
    embeddedServer(
        factory = Netty,
        port = port,
        module = Application::module
    ).start()

    // Add two testing channels
    rooms["testing123"] = Room()
    rooms["iloveberlin"] = Room()
}

@ExperimentalCoroutinesApi // because of isClosedForSend from SendChannel
fun Application.module() {
    install(WebSockets) {}
    install(Routing) {
        static("static") {
            files("pieces")
        }
        webSocket("/ws/{roomName}") {
            val roomName = call.parameters["roomName"]
            val room = rooms[roomName]
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
            call.respondFile(File("index.html"))
        }
        get("/room/{roomName}") {
            val roomName = call.parameters["roomName"]
            if (rooms.containsKey(roomName)) {
                println("GET: $roomName")
                call.respondFile(File("channel.html"))
            }
        }
    }
}
