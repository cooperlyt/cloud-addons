package io.github.cooperlyt.cloud.addons.serialize.exception

import org.springframework.http.HttpStatusCode
import org.springframework.web.server.ResponseStatusException

open class ResponseDefineException : ResponseStatusException {


    val responseDefineStatus: ResponseDefineStatus
    var args: Array<String> = arrayOf()

    constructor(httpStatus: HttpStatusCode, statusCode: ResponseDefineStatus) : super(httpStatus) {
        this.responseDefineStatus = statusCode
    }

    constructor(httpStatus: HttpStatusCode, code: Int, message: String ) : super(httpStatus) {
        this.responseDefineStatus = object : ResponseDefineStatus {
            override fun getCode() = code

            override fun getMessage() = message
        }
    }

    constructor(status: ExceptionStatusCode) : super(status.httpStatus) {
        this.responseDefineStatus = status
    }

    constructor(status: ExceptionStatusCode, vararg args: String) : super(status.httpStatus) {
        this.args = arrayOf(*args)
        this.responseDefineStatus = status
    }

    constructor(status: ExceptionStatusCode, cause: Throwable) : super(status.httpStatus, cause.message, cause) {
        this.responseDefineStatus = status
    }

    constructor(status: ExceptionStatusCode, cause: Throwable, vararg args: String) : super(status.httpStatus, cause.message, cause) {
        this.args = arrayOf(*args)
        this.responseDefineStatus = status
    }

    constructor(status: ExceptionStatusCode, reason: String) : super(status.httpStatus, reason) {
        this.responseDefineStatus = status
    }

    constructor(status: ExceptionStatusCode, reason: String, vararg args: String) : super(status.httpStatus, reason) {
        this.args = arrayOf(*args)
        this.responseDefineStatus = status
    }

    constructor(status: ExceptionStatusCode, reason: String, cause: Throwable, vararg args: String) : super(status.httpStatus, reason, cause) {
        this.args = arrayOf(*args)
        this.responseDefineStatus = status
    }
}