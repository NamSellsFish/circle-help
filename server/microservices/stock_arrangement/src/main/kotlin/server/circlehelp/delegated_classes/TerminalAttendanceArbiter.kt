package server.circlehelp.delegated_classes

import server.circlehelp.api.response.ArbitratedAttendance
import server.circlehelp.api.response.AttendanceResponse
import server.circlehelp.api.response.AttendanceType
import server.circlehelp.entities.WorkShift
import java.rmi.UnexpectedException

object TerminalAttendanceArbiter : AttendanceTypeArbiter {
    override fun decide(
        arbiratedAttendance: ArbitratedAttendance,
        workShift: WorkShift
    ): AttendanceResponse {
        throw UnexpectedException("All cases of ${AttendanceTypeArbiter::class.simpleName} should have been exhausted.")
    }
}