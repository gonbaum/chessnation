package video_chess

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

class RoomsHandler {
    private val rooms = mutableMapOf<String, Room>()
    private val log = LoggerFactory.getLogger(RoomsHandler::class.java)

    fun createRoom(name: String) {
        if (rooms.containsKey(name))
            return

        rooms[name] = Room(BoardHandler())
    }

    fun addPlayer(roomName: String, player: Player) {
        forRoom(roomName) {
            it.players.add(player)
            player.notify("fen|${it.board.getFen()}")
        }
    }

    fun updatePosition(name: String, position: String) {
        forRoom(name) {
            removeDisconnectedPlayers()
            if (it.board.updateBoard(position))
                it.players.forEach { player ->
                    player.notify(position)
                    player.notify("captured|${it.board.getCapturedPieces()}")
                }
        }

        printStatus()
    }

    fun resetPosition(name: String) {
        forRoom(name) {
            it.board.resetBoard()
            it.players.forEach { player -> player.notify("reset") }
        }
    }

    private fun removeDisconnectedPlayers() {
        rooms.values.forEach { room -> room.players.removeIf { it.isDisconnected() } }
    }

    private fun forRoom(roomName: String, action: (Room) -> Unit) {
        val room = rooms[roomName]
        if (room != null) {
            action(room)
        }
    }

    private fun printStatus() {
        GlobalScope.launch {
            val playersString = rooms.values.map { it.players.count() }.joinToString { it.toString() }
            log.info("Rooms count ${rooms.count()}, with players: $playersString")
        }
    }
}

private data class Room(val board: BoardHandler, val players: MutableList<Player> = mutableListOf())

interface Player {
    fun notify(message: String): Unit
    fun isDisconnected(): Boolean
}
