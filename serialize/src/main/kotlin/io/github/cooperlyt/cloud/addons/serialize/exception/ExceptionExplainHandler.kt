package io.github.cooperlyt.cloud.addons.serialize.exception

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ValidationException
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Configuration
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.net.BindException


@Configuration
@ControllerAdvice
@ConditionalOnWebApplication
@ConditionalOnClass(HttpServletRequest::class)
@ConditionalOnMissingBean(annotation = [ControllerAdvice::class])
class ExceptionExplainHandler: ResponseEntityExceptionHandler() {

    @ExceptionHandler(
        BindException::class,
        ValidationException::class,
        MethodArgumentNotValidException::class
    )
    fun validationExceptionHandler(
        ex: Exception?,
        request: HttpServletRequest
    ): ResponseEntity<ValidationHttpStatusExplain> {
        return super.validationExceptionHandler(ex!!, request.contextPath)
    }

    @ExceptionHandler(ResponseDefineException::class)
    fun defineExceptionHandler(
        ex: ResponseDefineException?,
        request: HttpServletRequest
    ): ResponseEntity<HttpStatusExplain> {
        return super.defineExceptionHandler(ex!!, request.contextPath)
    }
}