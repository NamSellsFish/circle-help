package server.circlehelp.api.response

import VerboseDateDeserializer
import VerboseDateSerializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import server.circlehelp.api.serializers.EmptyAcceptingTimeDeserializer
import server.circlehelp.api.serializers.EmptyAcceptingTimeSerializer
import server.circlehelp.api.serializers.matchesUrl
import server.circlehelp.value_classes.UrlValue
import java.time.LocalDate
import java.time.LocalTime

data class ArbitratedAttendance(
    @JsonDeserialize(using = VerboseDateDeserializer::class)
    @JsonSerialize(using = VerboseDateSerializer::class)
    val currDate: LocalDate,

    @JsonDeserialize(using = EmptyAcceptingTimeDeserializer::class)
    @JsonSerialize(using = EmptyAcceptingTimeSerializer::class)
    val punchInTime: LocalTime?,

    @JsonDeserialize(using = EmptyAcceptingTimeDeserializer::class)
    @JsonSerialize(using = EmptyAcceptingTimeSerializer::class)
    val punchOutTime: LocalTime?,

    @Size(max = 255)
    val imageUrl: UrlValue,
) {
    fun decide(type: AttendanceType): AttendanceResponse {
        return AttendanceResponse(
            type, currDate, punchInTime, punchOutTime, imageUrl.url
        )
    }
}

