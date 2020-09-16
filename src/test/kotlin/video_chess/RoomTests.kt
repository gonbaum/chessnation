package video_chess

import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.selects.SelectClause2
import kotlinx.coroutines.selects.SelectInstance
import kotlin.test.*

class RoomTests {
    @Test
    fun `sends starting fen position and another message`() {
        val room = Room("test")
        val socket = CollectingSocket()
        runBlocking {
            room.addSocket(socket)
            room.notifySockets("message1")
        }
        assertEquals(
            listOf("fen|rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", "message1"),
            socket.messages
        )
    }

    @Test
    fun `removes closed sockets`() {
        val room = Room("test")
        runBlocking {
            room.addSocket(ClosedSocket())
            room.removeClosedSockets()
        }
        assertEquals(0, room.sockets.size)
    }
}

private open class AbstractTestSocket : SendChannel<Frame> {
    @ExperimentalCoroutinesApi
    override val isClosedForSend: Boolean = false

    @ExperimentalCoroutinesApi
    override val isFull: Boolean = false

    @InternalCoroutinesApi
    override val onSend: SelectClause2<Frame, SendChannel<Frame>> = NoActionSelectClause2()

    class NoActionSelectClause2 : SelectClause2<Frame, SendChannel<Frame>> {
        @InternalCoroutinesApi
        override fun <R> registerSelectClause2(
            select: SelectInstance<R>,
            param: Frame,
            block: suspend (SendChannel<Frame>) -> R
        ) = Unit
    }

    override fun close(cause: Throwable?): Boolean = true

    @ExperimentalCoroutinesApi
    override fun invokeOnClose(handler: (cause: Throwable?) -> Unit) = Unit

    override fun offer(element: Frame): Boolean = true

    override suspend fun send(element: Frame) = Unit
}

private class CollectingSocket : AbstractTestSocket() {
    val messages: MutableList<String> = mutableListOf()

    override suspend fun send(element: Frame) {
        super.send(element)
        element as Frame.Text
        messages.add(element.readText())
    }
}

private class ClosedSocket : AbstractTestSocket() {
    @ExperimentalCoroutinesApi
    override val isClosedForSend: Boolean = true
}
