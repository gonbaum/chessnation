package network

object IOUtils {
    @JvmStatic
    fun closeQuietly(closeable: AutoCloseable) {
        try {
            closeable.close()
        } catch (e: Exception) {
            //Let's quietly ignore this!
        }
    }
}