package io.github.cooperlyt.cloud.addons.serialize.jackson

import org.springframework.boot.context.properties.ConfigurationProperties


@ConfigurationProperties(prefix = "mis.jackson")
class TypeScriptJacksonProperties {

    var zonedDate: ZonedTimeProperties? = null


    var longType: LongProperties? = null


    class ZonedTimeProperties {
        var enable: Boolean? = null

        var localTimeZone: String? = null
    }


    class LongProperties {
        var toString: Boolean? = null
    }
}
