package network

class ResponseContext(var isRunning: Boolean) {

    fun closeResponse() {
        isRunning = false
    }

}