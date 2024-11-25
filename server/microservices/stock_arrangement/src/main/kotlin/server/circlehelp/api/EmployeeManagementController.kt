package server.circlehelp.api

import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RestController
import server.circlehelp.annotations.RepeatableReadTransaction
import server.circlehelp.api.response.AttendanceDto
import server.circlehelp.api.response.UserAttendanceDto
import server.circlehelp.entities.Attendance
import server.circlehelp.repositories.AttendanceRepository
import server.circlehelp.repositories.readonly.ReadonlyAttendanceRepository
import server.circlehelp.repositories.readonly.ReadonlyWorkShiftRepository
import server.circlehelp.services.AccountService
import java.time.LocalTime

const val management = "/management"
private const val thisBase = "$baseURL$management"

@RestController
class EmployeeManagementController(
    private val readonlyAttendanceRepository: ReadonlyAttendanceRepository,
    private val readonlyWorkShiftRepository: ReadonlyWorkShiftRepository,

    private val attendanceRepository: AttendanceRepository,

    private val accountService: AccountService,
) {

    @PutMapping("$thisBase/punchIn")
    @RepeatableReadTransaction
    fun punchIn(@CurrentSecurityContext securityContext: SecurityContext) {
        val user = accountService.getUser(securityContext)!!

        val workShift = readonlyWorkShiftRepository.findByUser(user)

        if (workShift != null && LocalTime.now()
            in workShift.startTime.plusMinutes(30) ..workShift.startTime.plusHours(5))

        if (readonlyAttendanceRepository
            .existsByUserAndDate(user)
            .not())
            attendanceRepository.save(Attendance(user))
    }

    @PutMapping("$thisBase/punchOut")
    @RepeatableReadTransaction
    fun punchOut(@CurrentSecurityContext securityContext: SecurityContext) {
        val user = accountService.getUser(securityContext)!!

        val attendance = readonlyAttendanceRepository.findByUserAndDate(user)

        val workShift = readonlyWorkShiftRepository.findByUser(user)

        if (workShift != null && LocalTime.now()
            in workShift.startTime.plusMinutes(30) ..workShift.startTime.plusHours(5))

        if (attendance != null) {

            attendanceRepository.save(attendance.punchOut())
        }
    }

    @GetMapping("$thisBase/attendanceHistory")
    fun getPersonalAttendanceHistory(@CurrentSecurityContext securityContext: SecurityContext) : List<AttendanceDto> {
        val user = accountService.getUser(securityContext)!!

        return readonlyAttendanceRepository.findAllByUser(user, Attendance.sortByDateDesc).map(::AttendanceDto)
    }

    @GetMapping("/admin$thisBase/allAttendanceHistory")
    fun getAdminAllAttendanceHistory() : List<UserAttendanceDto> {

        return readonlyAttendanceRepository
            .findAll(Attendance.sortByDateDesc)
            .groupBy { it.user }.entries.toList()
            .map { UserAttendanceDto(it.key, it.value.map(::AttendanceDto)) }
    }
}