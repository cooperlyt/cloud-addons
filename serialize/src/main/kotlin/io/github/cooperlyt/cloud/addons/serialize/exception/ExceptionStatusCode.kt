package io.github.cooperlyt.cloud.addons.serialize.exception

import org.springframework.http.HttpStatus

interface ExceptionStatusCode : DefineStatusCode{

    val httpStatus: HttpStatus

    fun exception(): ResponseDefineException {
        return ResponseDefineException(this)
    }

    fun exception(vararg args: String): ResponseDefineException {
        return ResponseDefineException(this, *args)
    }

    fun exception(cause: Throwable): ResponseDefineException {
        return ResponseDefineException(this, cause)
    }

    fun exception(cause: Throwable, vararg args: String): ResponseDefineException {
        return ResponseDefineException(this, cause, *args)
    }

    fun exception(reason: String, cause: Throwable): ResponseDefineException {
        return ResponseDefineException(this, reason, cause)
    }

    fun exception(reason: String, cause: Throwable, vararg args: String): ResponseDefineException {
        return ResponseDefineException(this, reason, cause, *args)
    }
}