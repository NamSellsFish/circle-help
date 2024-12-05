package server.circlehelp.api.response

import VerboseDateDeserializer
import VerboseDateSerializer
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import server.circlehelp.api.serializers.EmptyAcceptingHourSerializer
import server.circlehelp.api.serializers.EmptyAcceptingTimeDeserializer
import server.circlehelp.api.serializers.EmptyAcceptingTimeSerializer
import server.circlehelp.api.serializers.matchesUrl
import server.circlehelp.entities.Attendance
import server.circlehelp.value_classes.UrlValue
import java.time.LocalDate
import java.time.LocalTime

data class AttendanceResponse(
    val type: AttendanceType,

    @JsonFormat(pattern = "yyyy-MM-dd")
    val currDate: LocalDate,

    @JsonDeserialize(using = EmptyAcceptingTimeDeserializer::class)
    @JsonSerialize(using = EmptyAcceptingTimeSerializer::class)
    val punchInTime: LocalTime?,

    @JsonDeserialize(using = EmptyAcceptingTimeDeserializer::class)
    @JsonSerialize(using = EmptyAcceptingTimeSerializer::class)
    val punchOutTime: LocalTime?,

    @Size(max = 255)
    @Pattern(regexp = matchesUrl)
    val imageUrl: String,
) {

    @JsonSerialize(using = EmptyAcceptingHourSerializer::class)
    val totalHours: LocalTime? =
        if (punchOutTime != null &&
        punchInTime != null)
            LocalTime.ofNanoOfDay(punchOutTime.toNanoOfDay() - punchInTime.toNanoOfDay())
        else
            null

    companion object {
        fun fromEntity(attendance: Attendance): AttendanceResponse {
            return AttendanceResponse(
                attendance.type,
                attendance.date,
                attendance.punchInTime,
                attendance.punchOutTime,
                attendance.imageUrl.url
            )
        }
    }
}
