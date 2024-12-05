package server.circlehelp.api.response

class ErrorsResponseException(val errorsResponse: ErrorsResponse,
                              message: String = errorsResponse.errors.body.joinToString(),
                              throwable: Throwable? = null)
    : RuntimeException(message, throwable) {

}