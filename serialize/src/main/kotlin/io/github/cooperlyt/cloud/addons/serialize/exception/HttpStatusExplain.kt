package io.github.cooperlyt.cloud.addons.serialize.exception

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Date

open class HttpStatusExplain @JsonCreator constructor(
    @JsonProperty("timestamp") val timestamp: Date,
    @JsonProperty("path") val path: String,
    @JsonProperty("code") private val code: Int,
    @JsonProperty("message")private val message: String
) : DefineStatusCode {

    var args: Array<String> = arrayOf()

    protected constructor(path: String, code: Int, message: String) : this(Date(), path, code, message)

    constructor(statusCode: DefineStatusCode, args: Array<String>, path: String) : this(
        Date(),
        path,
        statusCode.getCode(),
        statusCode.getMessage()
    ) {
        this.args = args
    }

    override fun getCode(): Int {
        return this.code
    }

    override fun getMessage(): String {
        return this.message
    }
}