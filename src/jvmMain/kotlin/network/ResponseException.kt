package network

class ResponseException(val statusCode: Int, override val message: String) : RuntimeException(message) 