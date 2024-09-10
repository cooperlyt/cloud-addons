package io.github.cooperlyt.cloud.addons.serialize.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.util.StringUtils
import java.math.BigInteger
import java.time.LocalDateTime
import java.time.ZoneId

@AutoConfiguration
@ConditionalOnClass(ObjectMapper::class)
@EnableConfigurationProperties(TypeScriptJacksonProperties::class)
class TypeScriptJackson2ObjectConfigure {

    companion object {
        private val logger = LoggerFactory.getLogger(TypeScriptJackson2ObjectConfigure::class.java)
    }

    /**
     *
     * LocalDateTime 类型很坑， 没有时区信息，不能转成 yyyy-MM-dd'T'HH:mm:ss.SSSXXX  在使用时需要根据时区转换
     *
     * LocalDate 更坑，因为没有时间， 连转换都不可能。时区信息直接丢失
     *
     * yyyy-MM-dd'T'HH:mm:ss.SSS'Z'  Z means UTC
     *
     * yyyy-MM-dd'T'HH:mm:ss.SSSXXX XXX means UTC+8 for china
     *
     */
    @Bean
    @ConditionalOnClass(JavaTimeModule::class)
    @ConditionalOnProperty(prefix = "spring.addons.jackson.zoned-date", name = ["enable"])
    fun jackson2LocalDateTimeMapperBuilder(properties: TypeScriptJacksonProperties): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer { builder: Jackson2ObjectMapperBuilder ->
            val timeZone: String? = properties.zonedDate?.localTimeZone
            val zoneId =
                if (StringUtils.hasText(timeZone)) ZoneId.of(timeZone) else ZoneId.systemDefault()


            val javaTimeModule = JavaTimeModule()


            javaTimeModule.addSerializer(
                LocalDateTime::class.java,
                ZonedLocalDateTimeSerializer(zoneId)
            )


            javaTimeModule.addDeserializer(
                LocalDateTime::class.java,
                ZonedLocalDateTimeDeserializer(zoneId)
            )


            builder.modulesToInstall(javaTimeModule)

            logger.info("LocalDateTime Serializer and Deserializer has been installed")
            logger.debug("LocalDateTime Serializer and Deserializer has been installed with timeZone: {}", zoneId)
        }
    }

    /**
     * 使用此配置后，spring 返回一个Long 类型时 会带引号， 导致前端js无法解析成 number , 字符串也会被解析成 %20XXX%20
     *
     * 为了解决前端js对于long类型的精度丢失问题
     * @return register
     */
    @Bean
    @ConditionalOnProperty(prefix = "spring.addons.jackson.long-type", name = ["to-string"])
    fun jackson2LongMapperBuilder(): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer { builder: Jackson2ObjectMapperBuilder ->
            builder.serializerByType(
                BigInteger::class.java,
                ToStringSerializer.instance
            )
            builder.serializerByType(
                Long::class.java,
                ToStringSerializer.instance
            )
            builder.serializerByType(
                java.lang.Long.TYPE,
                ToStringSerializer.instance
            )

            logger.info("Long Serializer has been installed")
            logger.debug("Long Serializer has been installed --- toStringSerializer")
        }
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.addons.jackson.custom-class", name = ["enable"])
    fun jackson2MoneyMapperBuilder(): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer { builder: Jackson2ObjectMapperBuilder ->
            // 创建一个 SimpleModule 用来注册自定义的序列化器
            val module = SimpleModule()
            // 注册 MoneySerializer
            module.addSerializer(BigDecimalPropertySerializer::class.java, MoneySerializer())

            // 将模块添加到 Jackson ObjectMapper Builder 中
            builder.modulesToInstall(module)

            logger.info("BigDecimal property Serializer has been installed")
            logger.debug("Function… Serializer has been installed --- MoneySerializer")
        }
    }
}