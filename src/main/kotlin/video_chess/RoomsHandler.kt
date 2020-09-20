package video_chess

class RoomsHandler {
    private val rooms = mutableMapOf<String, Room>()

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
                it.players.forEach { player -> player.notify(position) }
        }
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
}

private data class Room(val board: BoardHandler, val players: MutableList<Player> = mutableListOf())

interface Player {
    fun notify(message: String): Unit
    fun isDisconnected(): Boolean
}
