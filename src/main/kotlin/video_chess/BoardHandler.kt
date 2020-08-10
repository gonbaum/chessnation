package video_chess

import com.github.bhlangonijr.chesslib.*
import com.github.bhlangonijr.chesslib.move.Move

class BoardHandler {
    private var board = Board()

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

    fun resetBoard() {
        board = Board()
    }
}