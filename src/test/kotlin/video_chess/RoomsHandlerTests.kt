package video_chess

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class RoomTests {
    @Test
    fun `sends starting fen position and another move`() {
        val rooms = RoomsHandler()
        val roomName = "test"
        val player1 = TestPlayer()
        val player2 = TestPlayer()

        rooms.createRoom(roomName)
        rooms.addPlayer(roomName, player1)
        rooms.addPlayer(roomName, player2)
        rooms.updatePosition(roomName, "e2e4")
        rooms.updatePosition(roomName, "e7e5")

        val expectedMessages = listOf("fen|rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", "e2e4", "e7e5")
        assertEquals(expectedMessages, player1.messages)
        assertEquals(expectedMessages, player2.messages)
    }

    @Test
    fun `does not notify for incorrect move`() {
        val rooms = RoomsHandler()
        val roomName = "test"
        val player = TestPlayer()

        rooms.createRoom(roomName)
        rooms.addPlayer(roomName, player)
        rooms.updatePosition(roomName, "e1e5")

        assertEquals(listOf("fen|rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"), player.messages)
    }

    @Test
    fun `does not notify disconnected players`() {
        val rooms = RoomsHandler()
        val roomName = "test"
        val player = DisconnectedPlayer()

        rooms.createRoom(roomName)
        rooms.addPlayer(roomName, player)
        rooms.updatePosition(roomName, "e2e4")

        assertEquals("fen|rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", player.lastMessage)
    }

    @Test
    fun `supports multiroom`() {
        val rooms = RoomsHandler()
        rooms.createRoom("test1")
        val player1a = TestPlayer()
        val player1b = TestPlayer()
        rooms.addPlayer("test1", player1a)
        rooms.addPlayer("test1", player1b)
        rooms.updatePosition("test1", "e2e4")

        rooms.createRoom("test2")
        val player2a = TestPlayer()
        val player2b = TestPlayer()
        rooms.addPlayer("test2", player2a)
        rooms.addPlayer("test2", player2b)
        rooms.updatePosition("test2", "d2d4")
        rooms.updatePosition("test2", "h7h6")

        val startingPosition = "fen|rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        assertEquals(listOf(startingPosition, "e2e4"), player1a.messages)
        assertEquals(listOf(startingPosition, "e2e4"), player1b.messages)
        assertEquals(listOf(startingPosition, "d2d4", "h7h6"), player2a.messages)
        assertEquals(listOf(startingPosition, "d2d4", "h7h6"), player2b.messages)
    }
}

class TestPlayer() : Player {
    val messages: MutableList<String> = mutableListOf()

    override fun notify(message: String) {
        messages.add(message)
    }

    override fun isDisconnected(): Boolean {
        return false
    }
}

class DisconnectedPlayer : Player {
    var lastMessage: String? = null

    override fun notify(message: String) {
        lastMessage = message
    }

    override fun isDisconnected(): Boolean {
        return true
    }
}
