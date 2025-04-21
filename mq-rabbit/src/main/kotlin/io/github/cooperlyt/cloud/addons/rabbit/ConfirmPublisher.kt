package io.github.cooperlyt.cloud.addons.rabbit

import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.connection.CorrelationData
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.integration.support.MessageBuilder
import org.springframework.messaging.Message
import reactor.core.publisher.Flux

import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.time.Duration
import java.util.*
import java.util.function.Supplier

/**
 * 主要用于使用消息系统实现TCC
 *
 * 如果不是TCC 那么可以考虑使用 RabbitTransactionManager 配合 Spring 中使用声明式事务管理来处理 RabbitMQ 消息的发送和接收, 与数据库事务等其他事务性操作结合起来，确保在事务回滚时，RabbitMQ 的操作也能被撤销
 *
 * 最佳实践：
 *  每次事务中最好仅发一条消息而且要放在事务处理最后执行，后在rabbit MQ 中通过 routing key 分发到不同的 queue 中去， 而不是发多条消息。
 *      原因是 rabbit MQ 不支持半消息， 如果在一次发送多条，那么可能导致前面的消息已经成功发送，而后面的消息发送失败导致事务回滚，那么前面已发面的消息不会被撤回。
 *      消息放在事务最后是因为rabbit MQ 不支持半消息 所以消息发送不会被回滚但数据库操作可被回滚
 */
open class ConfirmPublisher<T> {

    companion object {
        private val logger = LoggerFactory.getLogger(ConfirmPublisher::class.java)

        const val DEFAULT_CONFIRM_TIMEOUT = 10L
    }

    private val sinks: Sinks.Many<Message<T>> = Sinks.many().multicast().onBackpressureBuffer()

    fun sinks() : Supplier<Flux<Message<T>>> {
        return Supplier { with(sinks) { asFlux() } }
    }

    fun sendMessage(message: T): Mono<Boolean> {
        return sendMessage(message)
    }

    protected fun sendMessage(message: T, timeout: Duration, vararg headers: Pair<String,String>): Mono<Boolean> {
        return sendMessage(message, mapOf(*headers) , timeout)
    }

    protected fun sendMessage(message: T, vararg headers: Pair<String,String>): Mono<Boolean> {
        return sendMessage(message, mapOf(*headers) , Duration.ofSeconds(DEFAULT_CONFIRM_TIMEOUT))
    }

    private fun sendMessage(message: T,
                              headers: Map<String,String> = emptyMap(),
                              timeout: Duration = Duration.ofSeconds(DEFAULT_CONFIRM_TIMEOUT)
    ): Mono<Boolean> {
        val corr = CorrelationData(UUID.randomUUID().toString())

        val msg = MessageBuilder.withPayload(message!!)
            .setHeader(AmqpHeaders.PUBLISH_CONFIRM_CORRELATION,corr)
            .apply { headers.forEach { this.setHeader(it.key,it.value) } }
            .build()


//        while (sinks.tryEmitNext(msg).isFailure) {
//            LockSupport.parkNanos(10)
//        }

//        val emitResult = sinks.emitNext(msg) { _, emitResult ->
//            if (emitResult == Sinks.EmitResult.FAIL_NON_SERIALIZED || emitResult == Sinks.EmitResult.FAIL_OVERFLOW) {
//                logger.warn("Message emission failed with result: $emitResult. Retrying...")
//                true // 只在这些失败条件下进行重试
//            } else {
//                logger.error("Message emission failed with result: $emitResult")
//                false // 其他情况下不重试
//            }
//        }

//        sinks.emitNext(msg, Sinks.EmitFailureHandler.FAIL_FAST)

        if (sinks.tryEmitNext(msg).isFailure) {
            return Mono.just(false)
        }

        return Mono.fromFuture(corr.future)
            .timeout(timeout)
            .map { confirm ->
                corr.returned ?.let {
                    logger.warn("send confirm message Fail: Message returned: ${it.message}")
                    return@let false  // 返回空的 Mono，因为没有需要处理的逻辑
                } ?: run {
                    if (!confirm.isAck)
                        logger.warn("send confirm message Fail: Not Ack reason: ${confirm.reason} ")
                    return@run confirm.isAck
                }
            }
            .onErrorResume { throwable ->
                logger.warn("Failed to confirm message for id: $message", throwable)
                Mono.just(false)  // 错误发生时返回 false
            }
    }
}