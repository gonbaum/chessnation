package video_chess

import kotlin.test.Test
import kotlin.test.assertEquals

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
}