package video_chess

import com.github.bhlangonijr.chesslib.*
import com.github.bhlangonijr.chesslib.move.Move
import com.github.bhlangonijr.chesslib.move.MoveGenerator
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
import kotlinx.coroutines.delay

val channels = HashSet<SendChannel<Frame>>()
val board = Board()

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
                        updateBoard(text)
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

private fun updateBoard(text: String) {
    when (text.length) {
        4 -> board.doMove( // regular move
            Move(
                Square.fromValue(text.substring(0, 2).toUpperCase()),
                Square.fromValue(text.substring(2, 4).toUpperCase())
            )
        )
        5 -> board.doMove( // promotion
            Move(
                Square.fromValue(text.substring(0, 2).toUpperCase()),
                Square.fromValue(text.substring(2, 4).toUpperCase()),
                if (board.sideToMove == Side.WHITE) Piece.WHITE_QUEEN else Piece.BLACK_QUEEN
            )
        )
        else -> throw IllegalArgumentException("Incorrect move received: $text")
    }
}

private suspend fun sendStartGameSequence() {
    listOf("w", "b").shuffled().zip(channels)
        .forEach { it.second.send(Frame.Text(it.first)) }
    channels.forEach { it.send(Frame.Text("start")) }
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
