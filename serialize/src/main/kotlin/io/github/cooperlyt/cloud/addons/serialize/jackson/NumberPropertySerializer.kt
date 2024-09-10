package io.github.cooperlyt.cloud.addons.serialize.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.io.IOException
import java.math.BigDecimal

interface BigDecimalPropertySerializer {

    val serializerValue: BigDecimal
}

class MoneySerializer : JsonSerializer<BigDecimalPropertySerializer>() {

    @Throws(IOException::class)
    override fun serialize(numberClazz: BigDecimalPropertySerializer, jsonGenerator: JsonGenerator, serializers: SerializerProvider) {
        // 将 locationValue 写入 JSON 输出
        jsonGenerator.writeNumber(numberClazz.serializerValue)
    }
}