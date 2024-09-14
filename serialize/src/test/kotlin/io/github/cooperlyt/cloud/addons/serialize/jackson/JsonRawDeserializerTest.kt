package io.github.cooperlyt.cloud.addons.serialize.jackson

import com.fasterxml.jackson.annotation.JsonRawValue
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.IOException

class JsonRawDeserializerTest {


    private data class PP(
        @JsonDeserialize(using = JsonRawDeserializer::class)
        @JsonRawValue
        var aa: String? = null,

        @JsonDeserialize(contentUsing = JsonRawDeserializer::class)
        @JsonRawValue
        var ll: MutableList<String> = ArrayList()
    ) {



        init {
            ll.add("{\"name\":\"小王\",\"country\":\"中国\",\"courseList\":\"\"}")
            ll.add("{\"name\":\"小王1\",\"country\":\"中国\",\"courseList\":\"\"}")
            ll.add("{\"name\":\"小王2\",\"country\":\"中国\",\"courseList\":\"\"}")
            ll.add("{\"name\":\"小王3\",\"country\":\"中国\",\"courseList\":\"\"}")
        }

    }

    @Test
    fun deserialize() {


        val mapper = ObjectMapper()
        try {
            val pp = PP()
            pp.aa = ("{\"option\":[{\"label\":\"第一个\",\"key\":\"第一个\"},{\"label\":\"第二个\",\"key\":\"第二个\"}]}")
            val ss = mapper.writeValueAsString(pp)
            println(ss)

            // assertEquals(ss , "{\"aa\":{\"option\":[{\"label\":\"第一个\",\"key\":\"第一个\"},{\"label\":\"第二个\",\"key\":\"第二个\"}]}}");
//            val p = mapper.readValue(
//                ss,
//                PP::class.java
//            )
//
//            assertEquals(pp, p)
//            println(p.aa)
//
//
//            val p2 = PP()
//            val s2 = mapper.writeValueAsString(p2)
//            println("out=>$s2")
//
//            assertEquals(s2, "{\"aa\":null}")
//            val p2r = mapper.readValue(
//                s2,
//                PP::class.java
//            )



            println()


        } catch (e: JsonProcessingException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}