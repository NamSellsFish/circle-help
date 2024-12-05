package server.circlehelp.delegated_classes

import server.circlehelp.api.response.ArbitratedAttendance
import server.circlehelp.api.response.AttendanceResponse
import server.circlehelp.api.response.AttendanceType
import server.circlehelp.entities.WorkShift

class PunchOutAttendanceArbiter(private val attendanceTypeArbiter: AttendanceTypeArbiter)
    : AttendanceTypeArbiter {

    override fun decide(
        arbiratedAttendance: ArbitratedAttendance,
        workShift: WorkShift
    ): AttendanceResponse {
        if (arbiratedAttendance.punchOutTime == null)
            return attendanceTypeArbiter.decide(arbiratedAttendance, workShift)
        else
            return arbiratedAttendance.decide(
                if (arbiratedAttendance.punchOutTime < workShift.endTime)
                    AttendanceType.EarlyLeave
                else
                if (arbiratedAttendance.punchOutTime in workShift.endTime..workShift.endTime.plusMinutes(15))
                    AttendanceType.FullAttendance
                else
                    AttendanceType.Overtime
            )
    }
}