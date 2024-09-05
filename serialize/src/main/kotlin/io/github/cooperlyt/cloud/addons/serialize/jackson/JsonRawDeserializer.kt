package io.github.cooperlyt.cloud.addons.serialize.jackson

import java.io.IOException

class JsonRawDeserializer : com.fasterxml.jackson.databind.JsonDeserializer<String?>() {
    @Throws(IOException::class, com.fasterxml.jackson.core.JsonProcessingException::class)
    override fun deserialize(
        jsonParser: com.fasterxml.jackson.core.JsonParser,
        deserializationContext: com.fasterxml.jackson.databind.DeserializationContext
    ): String {
        val mapper: com.fasterxml.jackson.databind.ObjectMapper =
            jsonParser.codec as com.fasterxml.jackson.databind.ObjectMapper
        val node: com.fasterxml.jackson.databind.JsonNode =
            mapper.readTree(jsonParser)
        return mapper.writeValueAsString(node)
    }
}
