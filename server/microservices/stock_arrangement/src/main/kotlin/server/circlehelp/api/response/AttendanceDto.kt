package server.circlehelp.api.response

import com.fasterxml.jackson.annotation.JsonFormat
import server.circlehelp.auth.User
import server.circlehelp.entities.Attendance
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class AttendanceDto(
    @JsonFormat(pattern="yyyy-MM-dd")
    val date: LocalDate,
    @JsonFormat(pattern="HH:mm:ss")
    val punchInTime: LocalTime,
    @JsonFormat(pattern="HH:mm:ss")
    val punchOutTime: LocalTime?
) {
    constructor(attendance: Attendance) : this(attendance.date, attendance.punchInTime, attendance.punchOutTime)
}
