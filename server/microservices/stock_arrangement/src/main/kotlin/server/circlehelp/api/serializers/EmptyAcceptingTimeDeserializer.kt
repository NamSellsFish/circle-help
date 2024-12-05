package server.circlehelp.api.serializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import org.slf4j.LoggerFactory
import java.io.IOException
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.log


class EmptyAcceptingTimeDeserializer : JsonDeserializer<LocalTime?>() {

    private val logger = LoggerFactory.getLogger(EmptyAcceptingTimeSerializer::class.java)

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(parser: JsonParser, context: DeserializationContext): LocalTime? {

        logger.info(parser.text)

        val timeStr: String = parser.text.trim()

        if (timeStr == "--:--") {

            return getNullValue(context)
        }
        return LocalTime.parse(timeStr, TIME_FORMATTER)
    }

    companion object {
        private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}