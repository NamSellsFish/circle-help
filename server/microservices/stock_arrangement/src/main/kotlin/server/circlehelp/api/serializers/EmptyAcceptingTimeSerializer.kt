package server.circlehelp.api.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class EmptyAcceptingTimeSerializer : JsonSerializer<LocalTime?>() {
    override fun serialize(
        value: LocalTime?,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        return gen.writeString(
        if (value != null)
            value.format(dateTimeFormatter)
        else "--:--")
    }

    companion object {
        private val dateTimeFormatter = DateTimeFormatter.ofPattern(hourMinuteFormat)
    }
}