package io.github.cooperlyt.cloud.addons.serialize.exception

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class ResponseDefineException : ResponseStatusException {


    val defineStatusCode: DefineStatusCode
    var args: Array<String> = arrayOf()

    constructor(httpStatus: HttpStatus, statusCode: DefineStatusCode) : super(httpStatus) {
        this.defineStatusCode = statusCode
    }

    constructor(status: ExceptionStatusCode) : super(status.httpStatus) {
        this.defineStatusCode = status
    }

    constructor(status: ExceptionStatusCode, vararg args: String) : super(status.httpStatus) {
        this.args = arrayOf(*args)
        this.defineStatusCode = status
    }

    constructor(status: ExceptionStatusCode, cause: Throwable) : super(status.httpStatus, cause.message, cause) {
        this.defineStatusCode = status
    }

    constructor(status: ExceptionStatusCode, cause: Throwable, vararg args: String) : super(status.httpStatus, cause.message, cause) {
        this.args = arrayOf(*args)
        this.defineStatusCode = status
    }

    constructor(status: ExceptionStatusCode, reason: String) : super(status.httpStatus, reason) {
        this.defineStatusCode = status
    }

    constructor(status: ExceptionStatusCode, reason: String, vararg args: String) : super(status.httpStatus, reason) {
        this.args = arrayOf(*args)
        this.defineStatusCode = status
    }

    constructor(status: ExceptionStatusCode, reason: String, cause: Throwable, vararg args: String) : super(status.httpStatus, reason, cause) {
        this.args = arrayOf(*args)
        this.defineStatusCode = status
    }
}