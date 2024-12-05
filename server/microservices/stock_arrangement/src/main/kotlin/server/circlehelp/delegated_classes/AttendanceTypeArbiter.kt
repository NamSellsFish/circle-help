package server.circlehelp.delegated_classes

import server.circlehelp.api.response.ArbitratedAttendance
import server.circlehelp.api.response.AttendanceResponse
import server.circlehelp.entities.WorkShift

fun interface AttendanceTypeArbiter {

    fun decide(
        arbiratedAttendance: ArbitratedAttendance,
        workShift: WorkShift
    ): AttendanceResponse

}