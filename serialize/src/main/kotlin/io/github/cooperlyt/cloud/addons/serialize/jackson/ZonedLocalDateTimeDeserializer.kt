package io.github.cooperlyt.cloud.addons.serialize.jackson

import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import org.slf4j.LoggerFactory
import java.io.IOException

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime


class ZonedLocalDateTimeDeserializer : InstantDeserializer<LocalDateTime> {

    companion object {
        private val logger = LoggerFactory.getLogger(ZonedLocalDateTimeDeserializer::class.java)
    }

    private val targetZoneId: ZoneId

    constructor() : this(DateTimeFormatter.ISO_OFFSET_DATE_TIME, ZoneId.systemDefault())

    constructor(targetZoneId: ZoneId) : this(DateTimeFormatter.ISO_OFFSET_DATE_TIME, targetZoneId)

    constructor(formatter: DateTimeFormatter) : this(formatter, ZoneId.systemDefault())

    constructor(formatter: DateTimeFormatter, targetZoneId: String) : this(formatter, ZoneId.of(targetZoneId))

    /**
     * replaceZeroOffsetAsZ, 如果没有偏移为‘Z’即UTC时区
     */
    constructor(formatter: DateTimeFormatter, targetZoneId: ZoneId) : super(
        LocalDateTime::class.java, formatter,
        { a -> ZonedDateTime.from(a).withZoneSameInstant(targetZoneId).toLocalDateTime() },
        { a -> ZonedDateTime.ofInstant(Instant.ofEpochMilli(a.value), a.zoneId).withZoneSameInstant(targetZoneId).toLocalDateTime() },
        { a -> ZonedDateTime.ofInstant(Instant.ofEpochSecond(a.integer, a.fraction.toLong()), a.zoneId).withZoneSameInstant(targetZoneId).toLocalDateTime() },
        { a, _ -> a },  // 忽略时区转换，目标时区应为targetZoneId
        false
    ) {
        this.targetZoneId = targetZoneId
    }

    protected constructor(base: ZonedLocalDateTimeDeserializer, leniency: Boolean?, targetZoneId: ZoneId) : super(base, leniency) {
        this.targetZoneId = targetZoneId
    }

    override fun withDateFormat(formatter: DateTimeFormatter): ZonedLocalDateTimeDeserializer {
        return ZonedLocalDateTimeDeserializer(formatter, targetZoneId)
    }

    override fun withLeniency(leniency: Boolean?): ZonedLocalDateTimeDeserializer {
        return ZonedLocalDateTimeDeserializer(this, leniency, targetZoneId)
    }

    override fun withShape(shape: JsonFormat.Shape): ZonedLocalDateTimeDeserializer {
        return this
    }

    @Throws(IOException::class)
    override fun deserialize(parser: JsonParser, context: DeserializationContext): LocalDateTime {
        logger.trace("deserialize LocalDateTime: {} to {}", parser.text, targetZoneId)
        return super.deserialize(parser, context)
    }
}