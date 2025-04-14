package io.github.cooperlyt.cloud.addons.rabbit

import com.rabbitmq.client.Channel
import org.slf4j.LoggerFactory
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.messaging.Message

import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.function.Consumer

abstract class AcknowledgeConsumer<T> {

    companion object {
        private val logger = LoggerFactory.getLogger(AcknowledgeConsumer::class.java)
    }

    abstract fun processMessage(message: T): Mono<Void>

    /**
     *
     * 考虑废弃此方法，因为 streams 支持重试，而且重试失败后还可放入死信队列
     *
     */
//    protected fun messageRetryStrategy(): RetryBackoffSpec {
//        return Retry.backoff(2, Duration.ofSeconds(1))
//            .jitter(0.2)
//            .transientErrors(true)
//            .doBeforeRetry { logger.warn("message retrying...") }
//    }


    /**
     *
     *
     * Reactive 有三种方法收消息：
     * 1.
     *     protected fun consumer(): (Flux<Message<T>>) -> Mono<Void> {
     *         return { flux ->
     *             flux
     *                 .doOnNext { message -> logger.debug("receive message {}", message) }
     *                 .flatMap { message ->
     *                     processMessage(message.payload)
     *                         .doOnError{ error -> logger.warn("process message error", error) }
     *                         .retryWhen(messageRetryStrategy())
     *                         .doOnSuccess {
     *                             logger.debug("process message success")
     *                             val deliveryTag = message.headers[AmqpHeaders.DELIVERY_TAG] as Long
     *                             val channel = message.headers[AmqpHeaders.CHANNEL] as Channel
     *                             channel.basicAck(deliveryTag, false) // Manual ACK
     *                         }
     *                         .doOnError { error ->
     *                             logger.warn("process message fail", error)
     *                             val deliveryTag = message.headers[AmqpHeaders.DELIVERY_TAG] as Long
     *                             val channel = message.headers[AmqpHeaders.CHANNEL] as Channel
     *                             channel.basicNack(deliveryTag, false, true) // Manual NACK and requeue
     *                         }
     *                 }
     *                 .onErrorResume { Mono.empty() }
     *                 .then()
     *         }
     *     }
     * 2.
     *    protected fun consumer(): Consumer<Flux<Message<T>>> {
     *         return Consumer { flux ->
     *             flux.flatMap { message ->
     *                     processMessage(message.payload)
     *                         .doOnError{ error -> logger.warn("process message error", error) }
     *                         .retryWhen(messageRetryStrategy())
     *                         .doOnSuccess {
     *                             logger.debug("process message success")
     *                             val deliveryTag = message.headers[AmqpHeaders.DELIVERY_TAG] as Long
     *                             val channel = message.headers[AmqpHeaders.CHANNEL] as Channel
     *                             channel.basicAck(deliveryTag, false) // Manual ACK
     *                         }
     *                         .doOnError { error ->
     *                             logger.warn("process message fail, back to MQ", error)
     *                             val deliveryTag = message.headers[AmqpHeaders.DELIVERY_TAG] as Long
     *                             val channel = message.headers[AmqpHeaders.CHANNEL] as Channel
     *                             channel.basicNack(deliveryTag, false, true) // Manual NACK and requeue
     *                         }
     *                 }
     *                 .onErrorResume {
     *                     logger.error("not a define exception, message will be discard", it)
     *                     Mono.empty()
     *                 }
     *                 .then()
     *                 .subscribeOn(Schedulers.boundedElastic())
     *                 .subscribe()
     *         }
     *     }
     *
     *     这种需要自己 .subscribe()
     *
     *
     * 以上两种都有以下两个问题。
     *  1.处理出错后，下一条消息到达可能会发生 no subscribe 错误.
     *  2.spring stream 可以用配置方式设置重试，这里没必要
     *
     *  所以用以下这种方法： 或 Consumer<T> 如果不需要请求头的话
     */
    fun consumer(): Consumer<Message<T>> {
        return Consumer<Message<T>> { originMessage ->
            logger.debug("receive a message: {}", originMessage)
            Mono.justOrEmpty(originMessage)
                .flatMap { message ->
                    processMessage(message.payload)
                        .doOnSuccess {
                            logger.debug("process message success {}", message)
                            val deliveryTag = message.headers[AmqpHeaders.DELIVERY_TAG] as Long
                            val channel = message.headers[AmqpHeaders.CHANNEL] as Channel
                            channel.basicAck(deliveryTag, false) // Manual ACK
                        }
                        .doOnError { error ->
                            logger.warn("process message fail $message", error)
                            val deliveryTag = message.headers[AmqpHeaders.DELIVERY_TAG] as Long
                            val channel = message.headers[AmqpHeaders.CHANNEL] as Channel
                            // channel.basicNack(deliveryTag, false, true) // Manual NACK and multiple , requeue
                            channel.basicReject(deliveryTag, error !is InvalidMessageException) // Manual NACK and not requeue
                        }

                }
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe()
        }
    }
}