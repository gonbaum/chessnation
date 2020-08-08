package sample

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.move.MoveGenerator
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.content.file
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.response.respondRedirect
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay

val channels = HashSet<SendChannel<Frame>>()

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
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()
                        println("Received on WS: $text")
                        val filter = channels.filter { it != outgoing }.forEach { it.send(Frame.Text(text)) }
                    }
                }
            }
//                outgoing.send(Frame.Text("hello"))
//                playGame { outgoing.send(Frame.Text(it)) }
        }
        get("/") {
            println("hello")
            call.respondRedirect("/static/index.html", permanent = true)
        }
    }
}

suspend fun playGame(send: suspend (String) -> Unit): String {
    val board = Board()
    while (true) {
        val generateLegalMoves = MoveGenerator.generateLegalMoves(board)
        val nextMove = generateLegalMoves.random()
        board.doMove(nextMove)
        send(nextMove.toString())

        delay(1500L)
    }
}
