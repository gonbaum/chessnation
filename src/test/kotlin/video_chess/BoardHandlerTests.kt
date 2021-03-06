package video_chess

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class BoardHandlerTests {
    @Test
    fun `handles promotion`() {
        val boardHandler = BoardHandler()
        boardHandler.updateBoard("e2e4")
        boardHandler.updateBoard("f7f5")
        boardHandler.updateBoard("e4f5")
        boardHandler.updateBoard("g7g6")
        boardHandler.updateBoard("f5f6")
        boardHandler.updateBoard("e7e6")
        boardHandler.updateBoard("f6f7")
        boardHandler.updateBoard("e8e7")
        boardHandler.updateBoard("f7g8Q")

        assertEquals("rnbq1bQr/ppppk2p/4p1p1/8/8/8/PPPP1PPP/RNBQKBNR b KQ - 0 5", boardHandler.getFen())
    }

    @Test
    fun `supports resetting the board`() {
        val boardHandler = BoardHandler()
        boardHandler.updateBoard("e2e4")
        boardHandler.resetBoard()

        assertEquals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", boardHandler.getFen())
    }

    @Test
    fun `returns false for incorrect move`() {
        val boardHandler = BoardHandler()
        assertFalse(boardHandler.updateBoard("e7e5"))
    }

    @Test
    fun `returns empty captured pieces`() {
        val boardHandler = BoardHandler()
        boardHandler.updateBoard("e2e4")
        assertEquals("", boardHandler.getCapturedPieces())
    }

    @Test
    fun `returns taken pawns in Scandinavian`() {
        val boardHandler = BoardHandler()
        boardHandler.updateBoard("e2e4")
        boardHandler.updateBoard("d7d5")
        boardHandler.updateBoard("e4d5")
        boardHandler.updateBoard("d8d5")
        assertEquals("Pp", boardHandler.getCapturedPieces())
    }
}
