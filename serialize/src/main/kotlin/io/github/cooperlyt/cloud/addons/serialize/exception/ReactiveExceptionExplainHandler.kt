package io.github.cooperlyt.cloud.addons.serialize.exception

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Configuration
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.core.annotation.Order
import org.springframework.web.bind.annotation.*
import org.springframework.core.Ordered
import org.springframework.web.server.ServerWebExchange
import org.springframework.http.ResponseEntity

import java.net.BindException
import jakarta.validation.ValidationException
import org.springframework.web.bind.MethodArgumentNotValidException

@Configuration
@RestControllerAdvice
@ConditionalOnWebApplication
@ConditionalOnClass(org.springframework.web.reactive.config.WebFluxConfigurer::class)
@Order(Ordered.HIGHEST_PRECEDENCE)
class ReactiveExceptionExplainHandler : ResponseEntityExceptionHandler() {

    companion object{
        private val logger = LoggerFactory.getLogger(ReactiveExceptionExplainHandler::class.java)
    }

    @ExceptionHandler(ResponseDefineException::class)
    fun defineExceptionHandler(ex: ResponseDefineException, exchange: ServerWebExchange): ResponseEntity<HttpStatusExplain> {
        logger.warn("response define exception path info: ${exchange.request.path.pathWithinApplication()}")
        return super.defineExceptionHandler(ex, exchange.request.path.pathWithinApplication().value())
    }

    @ExceptionHandler(BindException::class, ValidationException::class, MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: Exception, exchange: ServerWebExchange): ResponseEntity<ValidationHttpStatusExplain> {
        logger.warn("argument exception path info: ${exchange.request.path.pathWithinApplication()}")
        return super.validationExceptionHandler(e, exchange.request.path.pathWithinApplication().value())
    }
}