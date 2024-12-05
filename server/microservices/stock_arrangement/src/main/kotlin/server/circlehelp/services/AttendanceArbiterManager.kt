package server.circlehelp.services

import org.springframework.stereotype.Service
import server.circlehelp.delegated_classes.AttendanceTypeArbiter
import server.circlehelp.delegated_classes.PunchInAttendanceArbiter
import server.circlehelp.delegated_classes.PunchOutAttendanceArbiter
import server.circlehelp.delegated_classes.TerminalAttendanceArbiter
import java.rmi.UnexpectedException

@Service
class AttendanceArbiterManager {

    val punchOutAttendanceArbiter = PunchOutAttendanceArbiter(TerminalAttendanceArbiter)

    val punchInAttendanceArbiter = PunchInAttendanceArbiter(punchOutAttendanceArbiter)

    val topAttendanceArbiter = punchInAttendanceArbiter
}