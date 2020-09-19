package video_chess

import io.ktor.http.cio.websocket.Frame
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel

data class Room(
    val name: String,
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Room

        return name == other.name
    }

    override fun hashCode(): Int = name.hashCode()
}
