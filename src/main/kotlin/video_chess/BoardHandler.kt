package video_chess

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import com.github.bhlangonijr.chesslib.move.MoveGenerator
import kotlinx.coroutines.delay

class BoardHandler {
    private val board = Board()

    fun updateBoard(text: String) {
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

    fun getFen(): String = board.fen

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
}