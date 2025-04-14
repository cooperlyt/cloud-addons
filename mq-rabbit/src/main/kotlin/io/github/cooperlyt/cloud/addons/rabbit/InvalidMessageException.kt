package io.github.cooperlyt.cloud.addons.rabbit

class InvalidMessageException: Exception {
    constructor(message: String): super(message)
    constructor(message: String, cause: Throwable): super(message, cause)
    constructor(cause: Throwable): super(cause)
}