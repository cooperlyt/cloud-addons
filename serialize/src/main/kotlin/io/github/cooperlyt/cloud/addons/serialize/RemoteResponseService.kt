package io.github.cooperlyt.cloud.addons.serialize

import io.github.cooperlyt.cloud.addons.serialize.exception.DefineStatusCode
import io.github.cooperlyt.cloud.addons.serialize.exception.HttpStatusExplain
import io.github.cooperlyt.cloud.addons.serialize.exception.ResponseDefineException
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.ClientResponse
import reactor.core.publisher.Mono

abstract class RemoteResponseService {

    companion object {
        private val logger = LoggerFactory.getLogger(RemoteResponseService::class.java)
    }

    protected fun <T> responseError(response: ClientResponse): Mono<T> {
        logger.warn("request return error. http code: ${response.statusCode().value()}")

        if (HttpStatus.NOT_FOUND.isSameCodeAs(response.statusCode())) {
            return Mono.empty()
        }

        return response.bodyToMono(HttpStatusExplain::class.java)
            .switchIfEmpty(Mono.error(ResponseDefineException(HttpStatus.BAD_GATEWAY, object : DefineStatusCode {
                //                override val code: Int
//                    get() = 8000001
//                override val message: String
//                    get() = "REMOTE_RESPONSE_ERROR"
                override fun getCode(): Int {
                    return 8000001
                }

                override fun getMessage(): String {
                    return "REMOTE_RESPONSE_ERROR"
                }
            })))
            .flatMap { explain ->
                logger.warn(explain.toString())
                logger.warn("code: ${explain.getCode()}")
                logger.warn("path: ${explain.path}")
                logger.warn("msg: ${explain.getMessage()}")
                Mono.error<T>(
                    ResponseDefineException(HttpStatus.valueOf(response.statusCode().value()), explain)
                )
            }
    }

    protected fun <T> sourceResponse(elementClass: Class<out T>, response: ClientResponse): Mono<T> {
        return if (response.statusCode().isError) {
            responseError(response)
        } else {
            response.bodyToMono(elementClass)
        }
    }

    protected fun <T> sourceResponse(elementTypeRef: ParameterizedTypeReference<T>, response: ClientResponse): Mono<T> {
        return if (response.statusCode().isError) {
            responseError(response)
        } else {
            response.bodyToMono(elementTypeRef)
        }
    }

}