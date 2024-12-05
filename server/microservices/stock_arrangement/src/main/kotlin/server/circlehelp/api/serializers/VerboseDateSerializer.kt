import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class VerboseDateSerializer : JsonSerializer<LocalDate>() {
    @Throws(IOException::class)
    override fun serialize(value: LocalDate, gen: JsonGenerator, serializers: SerializerProvider) {
        var formattedDate = value.format(DATE_FORMATTER)
        formattedDate = addOrdinalSuffix(formattedDate)
        gen.writeString(formattedDate)
    }

    private fun addOrdinalSuffix(date: String): String {
        val parts = date.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val day = parts[1].toInt()

        val suffix = if (day >= 11 && day <= 13) {
            "th"
        } else {
            when (day % 10) {
                1 -> "st"
                2 -> "nd"
                3 -> "rd"
                else -> "th"
            }
        }

        return String.format("%s %d%s %s %s", parts[0], day, suffix, parts[2], parts[3])
    }

    companion object {
        private val DATE_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")
    }
}