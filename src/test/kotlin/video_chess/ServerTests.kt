package video_chess

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.nio.charset.Charset
import kotlin.test.assertEquals

class ServerTests {
    @Test
    fun `responds OK on root`() {
        serverTest {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(ContentType.Text.Html.withCharset(Charset.defaultCharset()), response.contentType())
            }
        }
    }

    @Test
    fun `serves pieces`() {
        serverTest {
            listOf("wB", "wK", "wN", "wP", "wQ", "wR", "bB", "bK", "bN", "bP", "bQ", "bR")
                .map { "/static/$it.png" }
                .forEach {
                    handleRequest(HttpMethod.Get, it).apply {
                        assertEquals(HttpStatusCode.OK, response.status(), "Piece $it not found.")
                        assertEquals(ContentType.Image.PNG, response.contentType())
                    }
                }
        }
    }

    @Test
    fun `allows room creation and joining`() {
        serverTest {
            // someone creates the room
            handleRequest(HttpMethod.Get, "/room/BerlinBerlin").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(ContentType.Text.Html.withCharset(Charset.defaultCharset()), response.contentType())
            }
            // someone else joins the room
            handleRequest(HttpMethod.Get, "/room/BerlinBerlin").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(ContentType.Text.Html.withCharset(Charset.defaultCharset()), response.contentType())
            }
        }
    }

    @Test
    fun `disallows room creation with short name`() {
        serverTest {
            handleRequest(HttpMethod.Get, "/room/Berlin").apply {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }

    @Test
    fun `disallows room creation with space`() {
        serverTest {
            handleRequest(HttpMethod.Get, "/room/Berlin Berlin").apply {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }

    private fun serverTest(callback: suspend TestApplicationEngine.() -> Unit): Unit {
        withTestApplication(Application::module) {
            runBlocking { callback() }
        }
    }
}