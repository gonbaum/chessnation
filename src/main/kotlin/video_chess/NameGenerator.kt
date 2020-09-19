package video_chess

class NameGenerator {
    fun generateRandomName(availableWords: List<String> = getAvailableWords()): String {
        return (1..3).joinToString(separator = "") { availableWords.random() }
    }

    private fun getAvailableWords(): List<String> {
        return listOf("Berlin", "Paris", "London", "Warsaw", "Porto", "Oslo")
    }
}
