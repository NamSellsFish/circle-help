package server.circlehelp.api.request

import VerboseDateDeserializer
import VerboseDateSerializer
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import jakarta.validation.constraints.Size
import server.circlehelp.api.serializers.EmptyAcceptingTimeDeserializer
import server.circlehelp.api.serializers.EmptyAcceptingTimeSerializer
import server.circlehelp.value_classes.UrlValue
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime

data class AttendanceRequest(
    @Size(max = 255)
    val type: AttendanceRequestType,


    val currDate: LocalDate,

    @JsonDeserialize(using = EmptyAcceptingTimeDeserializer::class)
    @JsonSerialize(using = EmptyAcceptingTimeSerializer::class)
    val punchInTime: LocalTime?,

    @JsonDeserialize(using = EmptyAcceptingTimeDeserializer::class)
    @JsonSerialize(using = EmptyAcceptingTimeSerializer::class)
    val punchOutTime: LocalTime?,

    @Size(max = 255)
    val imageUrl: String,

    val currLatitude: BigDecimal,
    val currLongitude: BigDecimal
)
