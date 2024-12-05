import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import org.springframework.boot.jackson.JsonComponent
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

@JsonComponent
class VerboseDateDeserializer : JsonDeserializer<LocalDate>() {
    @Throws(IOException::class)
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): LocalDate {
        val dateString = p.text
        val matcher = DATE_PATTERN.matcher(dateString)

        if (matcher.matches()) {
            val formattedDate =
                matcher.group(1) + ", " + matcher.group(2) + " " + matcher.group(4) + " " + matcher.group(
                    5
                )
            return LocalDate.parse(formattedDate, DATE_FORMATTER)
        } else {
            throw IOException("Unrecognized date format: $dateString")
        }
    }

    companion object {
        private val DATE_PATTERN: Pattern = Pattern.compile(
            "([A-Za-z]+),\\s(\\d{1,2})(st|nd|rd|th)\\s([A-Za-z]+)\\s(\\d{4})"
        )
        private val DATE_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")
    }
}