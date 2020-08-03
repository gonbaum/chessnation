package sample

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.move.MoveGenerator
import io.ktor.application.install
import io.ktor.http.cio.websocket.Frame
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.delay

fun main() {
    embeddedServer(Netty, port = 8080, host = "127.0.0.1") {
        install(WebSockets) {}
        routing {
            webSocket("/ws") {
                outgoing.send(Frame.Text("hello"))
                playGame { outgoing.send(Frame.Text(it)) }
            }
            get("/") {
                println("hello")
            }
        }
    }.start(wait = true)
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
