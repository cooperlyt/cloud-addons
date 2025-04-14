package io.github.cooperlyt.cloud.addons.rabbit

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.cloud.stream.binder.rabbit.RabbitMessageChannelBinder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.converter.MessageConverter

@Configuration
@ConditionalOnClass(RabbitMessageChannelBinder::class)
class RabbitMQAutoConfigure {

    companion object {
        private val logger = LoggerFactory.getLogger(RabbitMQAutoConfigure::class.java)
    }

    /**
     * spring cloud function 的一个BUG 已在 spring 3.3.4 中解决 https://github.com/spring-projects/spring-framework/pull/33714
     *
     *
     */
    class CustomMessageMarshallingConverter(objectMapper: ObjectMapper) : MappingJackson2MessageConverter(objectMapper)

    /**
     *
     *
     * 在旧版本中似乎不需要使用这种方法，会根据 content-type 自已选择 相应的 Converter，
     * 但在  Spring cloud 2023.0.0 - Spring cloud 2023.0.3 中都会报 [java.lang.ClassCastException] 原因是 spring amqp 的 SimpleConverter 不支持 JSON 给转成了 [ByteArray] , 所以在转成所请求类的时候报错
     * 而 旧的 [org.springframework.amqp.support.converter.Jackson2JsonMessageConverter] 似乎已被弃用， 经我试验注出它并没有效果，也不会被使用，
     * 查看源码发现， rabbitMq 的 [org.springframework.cloud.stream.binder.rabbit.RabbitMessageChannelBinder] 已将转换器写死为一个私有类，代理了 [org.springframework.amqp.support.converter.SimpleMessageConverter] 不可更改
     * 应该是Spring cloud 更改了转换的层级， 类型转换不再由spring stream 负责， 而是由 spring message 负责 ，所以这时注出的是  [org.springframework.messaging.converter.MessageConverter] 而不是 [org.springframework.amqp.support.converter]
     *
     * 有意思的是此方法如果直接返回 [MappingJackson2MessageConverter] 会不起作用， 而使用一个代理类就有效了，这点不知为何？
     *
     * @param objectMapper  [MappingJackson2MessageConverter] 这个里本身不会使用 jackson-module-kotlin 在kotlin 中直接使用会报 ’没有默认构造器‘ 错误 ，即使引入了 KotlinModule
     *
     */
    @Bean
    fun messageConverter(objectMapper: ObjectMapper): MessageConverter {
        logger.debug("register MappingJackson2MessageConverter ")

        //objectMapper.registerModules(KotlinModule.Builder().)

//        if (logger.isDebugEnabled){
//            val test = "{\"businessId\":1336493025009664,\"tenant\":\"first\",\"type\":\"LEASE\",\"success\":false}"
//
//            val value =  objectMapper.readValue(test, StockFreezeStatusMessage::class.java)
//            logger.debug("convert value is {}", value)
//
//
//            val view: Class<*> = StockFreezeStatusMessage::class.java
//
//            val javaType: JavaType = objectMapper.constructType(object: TypeReference<StockFreezeStatusMessage>(){})
//
//            val v = objectMapper.readerWithView(view).forType(javaType).readValue<Any>(test)
//
//            logger.debug("convert value is {}", v)
//
//        }

        return CustomMessageMarshallingConverter(objectMapper)
    }
}