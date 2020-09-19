package video_chess

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NameGeneratorTests {
    @Test
    fun `generates using single word`() {
        val nameGenerator = NameGenerator()
        assertEquals("TestTestTest", nameGenerator.generateRandomName(listOf("Test")))
    }

    @Test
    fun `works with multiple words`() {
        val nameGenerator = NameGenerator()
        val actual = nameGenerator.generateRandomName(
            listOf("Test", "Kotlin")
        )
        assertTrue(
            listOf(
                "TestTestTest",
                "KotlinKotlinKotlin",
                "TestTestKotlin",
                "TestKotlinTest",
                "KotlinTestTest",
                "KotlinKotlinTest",
                "KotlinTestKotlin",
                "TestKotlinKotlin"
            ).contains(actual),
            "$actual is incorrect."
        )
    }
}
