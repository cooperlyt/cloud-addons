package io.github.cooperlyt.cloud.addons.serialize.jackson

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.datatype.jsr310.DecimalUtils
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializerBase
import org.slf4j.LoggerFactory
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ZonedLocalDateTimeSerializer : InstantSerializerBase<LocalDateTime> {

    companion object {
        private const val serialVersionUID = 1L
        val INSTANCE = ZonedLocalDateTimeSerializer()

        private val logger = LoggerFactory.getLogger(ZonedLocalDateTimeSerializer::class.java)

    }

    private val sourceZoneId: ZoneId

    /**
     * Flag for `JsonFormat.Feature.WRITE_DATES_WITH_ZONE_ID`
     */
    protected val _writeZoneId: Boolean?

    constructor() : this(DateTimeFormatter.ISO_OFFSET_DATE_TIME, ZoneId.systemDefault())

    constructor(sourceZoneId: String) : this(DateTimeFormatter.ISO_OFFSET_DATE_TIME, ZoneId.of(sourceZoneId))

    constructor(sourceZoneId: ZoneId) : this(DateTimeFormatter.ISO_OFFSET_DATE_TIME, sourceZoneId)

    constructor(formatter: DateTimeFormatter) : this(formatter, ZoneId.systemDefault())

    constructor(formatter: DateTimeFormatter, sourceZoneId: String) : this(formatter, ZoneId.of(sourceZoneId))

    constructor(formatter: DateTimeFormatter, sourceZoneId: ZoneId) : super(
        LocalDateTime::class.java,
        { dt -> dt.atZone(sourceZoneId).toInstant().toEpochMilli() },
        { dt -> dt.atZone(sourceZoneId).toInstant().toEpochMilli() },
        { dt -> dt.atZone(sourceZoneId).nano },
        formatter
    ) {
        this.sourceZoneId = sourceZoneId
        this._writeZoneId = null
    }

    protected constructor(
        base: ZonedLocalDateTimeSerializer,
        useTimestamp: Boolean?,
        formatter: DateTimeFormatter?,
        writeZoneId: Boolean?,
        sourceZoneId: ZoneId
    ) : this(base, useTimestamp, base._useNanoseconds, formatter, base._shape, writeZoneId, sourceZoneId)

    protected constructor(
        base: ZonedLocalDateTimeSerializer,
        useTimestamp: Boolean?,
        useNanoseconds: Boolean?,
        formatter: DateTimeFormatter?,
        shape: JsonFormat.Shape?,
        writeZoneId: Boolean?,
        sourceZoneId: ZoneId
    ) : super(base, useTimestamp, useNanoseconds, formatter, shape) {
        this._writeZoneId = writeZoneId
        this.sourceZoneId = sourceZoneId
    }

//    override fun withFormat(
//        useTimestamp: Boolean?,
//        formatter: DateTimeFormatter?,
//        shape: JsonFormat.Shape?
//    ): ZonedLocalDateTimeSerializer {
//        return ZonedLocalDateTimeSerializer(this, useTimestamp, formatter, shape, _writeZoneId, sourceZoneId)
//    }

    override fun withFormat(
        useTimestamp: Boolean?,
        f: DateTimeFormatter?,
        shape: JsonFormat.Shape?
    ): ZonedLocalDateTimeSerializer {
        return ZonedLocalDateTimeSerializer(this, useTimestamp, _useNanoseconds, f, shape, _writeZoneId, sourceZoneId)
    }

    override fun withFeatures(writeZoneId: Boolean?, writeNanoseconds: Boolean?): ZonedLocalDateTimeSerializer {
        return ZonedLocalDateTimeSerializer(
            this, _useTimestamp, writeNanoseconds, _formatter, _shape, writeZoneId, sourceZoneId
        )
    }

    @Throws(IOException::class)
    override fun serialize(value: LocalDateTime, g: JsonGenerator, provider: SerializerProvider) {
        val _value = value.atZone(sourceZoneId).withZoneSameInstant(provider.timeZone.toZoneId())

        if (!useTimestamp(provider)) {
            if (shouldWriteWithZoneId(provider)) {
                g.writeString(DateTimeFormatter.ISO_ZONED_DATE_TIME.format(_value))
                return
            }
        }

        if (useTimestamp(provider)) {
            if (useNanoseconds(provider)) {
                g.writeNumber(
                    DecimalUtils.toBigDecimal(
                        _value.toEpochSecond(), _value.nano
                    )
                )
                return
            }
            g.writeNumber(_value.toInstant().toEpochMilli())
            return
        }

        g.writeString(formatValue(_value, provider))

        logger.trace("serialize LocalDateTime: {} to {}", value, _value)
    }

    /**
     * @since 2.8
     */
    fun shouldWriteWithZoneId(ctxt: SerializerProvider): Boolean {
        return _writeZoneId ?: ctxt.isEnabled(SerializationFeature.WRITE_DATES_WITH_ZONE_ID)
    }

    override fun serializationShape(provider: SerializerProvider): JsonToken {
        return if (!useTimestamp(provider) && shouldWriteWithZoneId(provider)) {
            JsonToken.VALUE_STRING
        } else {
            super.serializationShape(provider)
        }
    }

    protected fun formatValue(value: ZonedDateTime, provider: SerializerProvider): String {
        var formatter = _formatter ?: DateTimeFormatter.ISO_OFFSET_DATE_TIME

        if (formatter.zone == null) {
            if (provider.config.hasExplicitTimeZone() && provider.isEnabled(SerializationFeature.WRITE_DATES_WITH_CONTEXT_TIME_ZONE)) {
                formatter = formatter.withZone(provider.timeZone.toZoneId())
            }
        }

        return formatter.format(value)
    }
}
