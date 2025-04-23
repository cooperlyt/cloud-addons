package io.github.cooperlyt.cloud.addons.rabbit

import com.rabbitmq.client.Channel
import org.slf4j.LoggerFactory
import org.springframework.amqp.ImmediateAcknowledgeAmqpException
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.messaging.Message

import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.function.Consumer

abstract class AcknowledgeConsumer<T>(private val maxRetriesDLQ: Int = 10, private val lotQueue: ConfirmPublisher<T>? = null) {

    companion object {
        private val logger = LoggerFactory.getLogger(AcknowledgeConsumer::class.java)
    }

    abstract fun processMessage(message: T): Mono<Void>

    //关于重试
    //1. 能过 代码设置 Mono.retryWhen() 来实现重试 适合自定义是否处理成功，如合同生成时只要合同生成，即使电子印单位失败也算消息成功
    //2. 通过 Binder 的配置来实现重试，可配合 DLQ
    //3. 通过配置 DLQ 实现重试
    //
    //                    auto-bind-dlq：启用 DLQ，自动创建死信队列（默认命名为 destination.group.dlq）。
    //                    dlq-ttl：DLQ 中消息的存活时间，过期后路由到 dlq-dead-letter-exchange。
    //                    dlq-dead-letter-exchange：设置为空表示使用默认交换机，消息会路由回原始队列。
    //                    republish-to-dlq：启用后，失败消息会附带异常信息（如堆栈跟踪）发布到 DLQ。
    //                    requeue-rejected：设为 false 确保失败消息路由到 DLQ 而不是重新入队。


//    如果 spring.cloud.stream.bindings.<channelName>.consumer.requeue-rejected 设置为 true（默认值），抛出异常会导致消息重新入队（相当于 requeue = true），不会进入 DLQ。
//    如果 requeue-rejected 设置为 false 且启用了 DLQ（auto-bind-dlq = true），抛出异常会导致消息被拒绝（requeue = false）并发送到 DLQ。

    private fun parkLotQueue(message: Message<T>): Boolean {
        return lotQueue ?.run { lotQueue.sendNoConfirmMessage(message) } ?: false
    }



    fun consumer(): Consumer<Message<T>> {
        return Consumer<Message<T>> { originMessage ->
            logger.debug("receive a message: {}", originMessage)
            Mono.justOrEmpty(originMessage)
                .flatMap { message ->



                    processMessage(message.payload)
                        //TODO 以下两个方法好像不需要，spring stream 会自根据 requeue-rejected 自动处理！ 去掉 basicAck 和 basicReject 测试一下


                        .doOnSuccess {
                            logger.debug("process message success {}", message)
                            val deliveryTag = message.headers[AmqpHeaders.DELIVERY_TAG] as Long
                            val channel = message.headers[AmqpHeaders.CHANNEL] as Channel
                            channel.basicAck(deliveryTag, false) // Manual ACK
                        }
                        .doOnError { error ->

                            val headers = message.headers


                            val xDeath = headers["x-death"] as? List<Map<String, Any>>
                            val retryCount = xDeath?.firstOrNull()?.get("count") as? Long ?: 0

                            logger.warn("process message is fail retryCount: $retryCount for:  $message", error)

                            //TODO 创建一个额外的“最终死信队列”（parking lot queue），当重试次数达到上限时，将消息路由到该队列。
//                            如果 retryCount 超过指定次数（例如 3 次），抛出 ImmediateAcknowledgeAmqpException，消息会被确认并丢弃，不再进入 DLQ。
                            if (retryCount >= maxRetriesDLQ) { // 最大重试 3 次
                                if (!parkLotQueue(message))
                                    throw ImmediateAcknowledgeAmqpException("Max retries exceeded") // 丢弃消息
                            }

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