package server.circlehelp.delegated_classes

import server.circlehelp.api.response.ArbitratedAttendance
import server.circlehelp.api.response.AttendanceResponse
import server.circlehelp.api.response.AttendanceType
import server.circlehelp.entities.Attendance
import server.circlehelp.entities.WorkShift

class PunchInAttendanceArbiter(private val attendanceTypeArbiter: AttendanceTypeArbiter) : AttendanceTypeArbiter {

    override fun decide(
        arbiratedAttendance: ArbitratedAttendance,
        workShift: WorkShift,
    ): AttendanceResponse {
        return if (arbiratedAttendance.punchInTime == null)
            attendanceTypeArbiter.decide(arbiratedAttendance, workShift)
        else
            return arbiratedAttendance.decide(
                if (arbiratedAttendance.punchInTime > workShift.startTime)
                    AttendanceType.Late
                else
                    AttendanceType.PunchIn
            )
    }
}