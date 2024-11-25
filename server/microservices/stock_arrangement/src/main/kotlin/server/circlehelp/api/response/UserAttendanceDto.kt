package server.circlehelp.api.response

import server.circlehelp.auth.User

data class UserAttendanceDto(
    val user: User,
    val attendances: Iterable<AttendanceDto>
)