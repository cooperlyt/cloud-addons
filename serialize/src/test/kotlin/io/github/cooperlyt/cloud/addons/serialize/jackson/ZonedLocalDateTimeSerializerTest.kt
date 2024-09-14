package io.github.cooperlyt.cloud.addons.serialize.jackson

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedLocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedLocalDateTimeSerializer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class ZonedLocalDateTimeSerializerTest {


    private class TestDateTime2 {
        val localDateTime: LocalDateTime? = null
    }



    private class TestDateTime {
        val zonedDateTime: ZonedDateTime = ZonedDateTime.now()

        val localDateTime: LocalDateTime = zonedDateTime.toLocalDateTime()
    }




    private class TestDateTimeResult {

        val zonedDateTime: ZonedDateTime? = null

        val localDateTime: ZonedDateTime? = null
    }


    @Test
    @Throws(JsonProcessingException::class)
    fun testDeserialization() {
        val testStr = "{\"localDateTime\":\"2024-01-20T14:43:42+08:00\"}"
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val zoneId = ZoneId.of("Asia/Shanghai")

        val javaTimeModule = JavaTimeModule()


        javaTimeModule.addSerializer(LocalDateTime::class.java, ZonedLocalDateTimeSerializer.INSTANCE)


        javaTimeModule.addDeserializer(LocalDateTime::class.java, ZonedLocalDateTimeDeserializer(zoneId))


        //builder.modules(javaTimeModule);
        val objectMapper = ObjectMapper()
        objectMapper.setDateFormat(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"))
        objectMapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"))
        objectMapper.setDateFormat(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"))



        objectMapper.registerModule(javaTimeModule)

        val obj = objectMapper.readValue(testStr, TestDateTime2::class.java)

        System.out.println(obj.localDateTime)

        println(objectMapper.writeValueAsString(obj))
    }


    @Test
    fun testDateTime() {
        val testDateTime = TestDateTime()

        println(testDateTime.zonedDateTime)
        println(testDateTime.localDateTime)

        try {
            //DateTimeFormatter dateTimeFormatter =  DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");


            val zoneId = ZoneId.of("Asia/Shanghai")

            val javaTimeModule = JavaTimeModule()


            javaTimeModule.addSerializer(LocalDateTime::class.java, ZonedLocalDateTimeSerializer.INSTANCE)


            javaTimeModule.addDeserializer(LocalDateTime::class.java, ZonedLocalDateTimeDeserializer(zoneId))


            //builder.modules(javaTimeModule);
            val objectMapper = ObjectMapper()
            //objectMapper.setDateFormat(new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
            objectMapper.setTimeZone(TimeZone.getTimeZone("UTC"))

            objectMapper.registerModule(javaTimeModule)
            val json = objectMapper.writeValueAsString(testDateTime)

            println("serializer test---------------------------------------------------")

            println(json)

            val testDateTime11 = objectMapper.readValue(json, TestDateTimeResult::class.java)

            println(testDateTime11.zonedDateTime)
            println(testDateTime11.localDateTime)

            assertEquals(testDateTime11.zonedDateTime , testDateTime11.localDateTime)
            println("deserializer test-------------------------------------------------")


            val testStr = "{\"zonedDateTime\":\"2023-08-02T16:00:00.000Z\"," +
                    "\"localDateTime\":\"2023-08-02T16:00:00.000Z\"}"


            println(testStr)

            val testDateTime1 = objectMapper.readValue(testStr, TestDateTime::class.java)

            println(
                testDateTime1.zonedDateTime.withZoneSameInstant(ZoneId.of("Asia/Shanghai")).toLocalDateTime()
            )
            println(testDateTime1.localDateTime)

            assertEquals(
                testDateTime1.zonedDateTime.withZoneSameInstant(ZoneId.of("Asia/Shanghai")).toLocalDateTime(),
                    testDateTime1.localDateTime
            )


            println("deserializer UTC test-------------------------------------------------")

            val testUTCStr = "{\"zonedDateTime\":\"2023-08-02T16:00:00.000+08:00\"," +
                    "\"localDateTime\":\"2023-08-02T16:00:00.000+08:00\"}"
            println(testUTCStr)

            val testUTCDateTime1 = objectMapper.readValue(testUTCStr, TestDateTime::class.java)

            println(
                testUTCDateTime1.zonedDateTime.withZoneSameInstant(ZoneId.of("Asia/Shanghai")).toLocalDateTime()
            )
            println(testUTCDateTime1.localDateTime)

            assertEquals(
                testUTCDateTime1.zonedDateTime.withZoneSameInstant(ZoneId.of("Asia/Shanghai")).toLocalDateTime()
                    ,testUTCDateTime1.localDateTime
            )


            println("deserializer UTC test-------------------------------------------------")

            val testTimestamp = "{\"zonedDateTime\":\"1696954607.968220000\"," +
                    "\"localDateTime\":\"1696954607.968220000\"}"
            println(testTimestamp)

            val testTimestampTime1 = objectMapper.readValue(testTimestamp, TestDateTime::class.java)

            println(
                testTimestampTime1.zonedDateTime.withZoneSameInstant(ZoneId.of("Asia/Shanghai")).toLocalDateTime()
            )
            println(testTimestampTime1.localDateTime)

            assertEquals(
                testTimestampTime1.zonedDateTime.withZoneSameInstant(ZoneId.of("Asia/Shanghai")).toLocalDateTime()
                    ,testTimestampTime1.localDateTime
            )
        } catch (e: JsonProcessingException) {
            throw RuntimeException(e)
        }

        //objectMapper.readValue("{\"date\":\"2020-01-01T00:00:00.000Z\",\"localDateTime\":\"2020-01-01T00:00:00.000\",\"localDate\":\"2020-01-01\",\"localTime\":\"00:00:00.000\"}",TestDateTime.class);
    }
}