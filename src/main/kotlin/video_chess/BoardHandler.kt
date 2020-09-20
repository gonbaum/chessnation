package video_chess

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import com.github.bhlangonijr.chesslib.move.MoveGenerator

class BoardHandler {
    private var board = Board()

    fun updateBoard(text: String): Boolean {
        val move = when (text.length) {
            4 ->  // regular move
                Move(
                    Square.fromValue(text.substring(0, 2).toUpperCase()),
                    Square.fromValue(text.substring(2, 4).toUpperCase())
                )

            5 -> // promotion
                Move(
                    Square.fromValue(text.substring(0, 2).toUpperCase()),
                    Square.fromValue(text.substring(2, 4).toUpperCase()),
                    if (board.sideToMove == Side.WHITE) Piece.WHITE_QUEEN else Piece.BLACK_QUEEN
                )
            else -> throw IllegalArgumentException("Incorrect move received: $text")
        }

        return if (MoveGenerator.generateLegalMoves(board).contains(move)) {
            board.doMove(move)
            true
        } else {
            println("Incorrect move received: $text")
            false
        }
    }

    fun getFen(): String = board.fen

    fun resetBoard() {
        board = Board()
    }
}
