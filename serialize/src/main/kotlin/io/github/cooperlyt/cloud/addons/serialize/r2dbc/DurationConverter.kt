package io.github.cooperlyt.cloud.addons.serialize.r2dbc

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import java.time.Duration

@ReadingConverter
class SecondsToDurationConverter : Converter<Long, Duration> {
    override fun convert(source: Long): Duration = Duration.ofSeconds(source)
}

@WritingConverter
class DurationToSecondsConverter : Converter<Duration, Long> {
    override fun convert(source: Duration): Long = source.seconds
}