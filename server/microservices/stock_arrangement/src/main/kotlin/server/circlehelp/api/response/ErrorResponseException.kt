package server.circlehelp.api.response

class ErrorResponseException(val errorResponse: ErrorResponse, message: String = errorResponse.errors.body.joinToString())
    : RuntimeException(message) {
}