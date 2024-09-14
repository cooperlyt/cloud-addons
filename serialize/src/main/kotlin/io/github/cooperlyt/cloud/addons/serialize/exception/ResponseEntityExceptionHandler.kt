package io.github.cooperlyt.cloud.addons.serialize.exception

import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException


open class ResponseEntityExceptionHandler {

    companion object {
        private val logger =  org.slf4j.LoggerFactory.getLogger(ResponseEntityExceptionHandler::class.java)
    }

    protected fun validationExceptionHandler(e: Exception, path: String): ResponseEntity<ValidationHttpStatusExplain> {
        val resultMap = mutableMapOf<String, String>()

        when (e) {
            is MethodArgumentNotValidException -> {
                val bindingResult = e.bindingResult
                bindingResult.fieldErrors.forEach { fieldError ->
                    resultMap[fieldError.field] = fieldError.defaultMessage ?: ""
                }
            }
            is ConstraintViolationException -> {
                e.constraintViolations.forEach { constraintViolation ->
                    resultMap[constraintViolation.propertyPath.toString()] = constraintViolation.messageTemplate
                }
            }
        }

        logger.warn("Valid exception: $resultMap", e)
        return ResponseEntity(ValidationHttpStatusExplain(resultMap, path), HttpStatus.BAD_REQUEST)
    }

    protected fun defineExceptionHandler(ex: ResponseDefineException, path: String): ResponseEntity<HttpStatusExplain> {
        logger.warn("Define exception: ${ex.responseDefineStatus}", ex)
        return ResponseEntity(HttpStatusExplain(ex.responseDefineStatus, ex.args, path), ex.statusCode)
    }
}