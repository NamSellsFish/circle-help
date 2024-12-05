package server.circlehelp.api

import jakarta.validation.Valid
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import server.circlehelp.annotations.RepeatableReadTransaction
import server.circlehelp.api.request.AttendanceRequest
import server.circlehelp.api.request.AttendanceRequestType
import server.circlehelp.api.response.ArbitratedAttendance
import server.circlehelp.api.response.AttendanceDto
import server.circlehelp.api.response.AttendanceResponse
import server.circlehelp.api.response.UserAttendanceDto
import server.circlehelp.entities.Attendance
import server.circlehelp.entities.Location
import server.circlehelp.entities.WorkplaceLocation
import server.circlehelp.repositories.AttendanceRepository
import server.circlehelp.repositories.readonly.ReadonlyAttendanceRepository
import server.circlehelp.repositories.readonly.ReadonlyWorkShiftRepository
import server.circlehelp.services.AccountService
import server.circlehelp.services.AttendanceArbiterManager
import server.circlehelp.services.Logic
import server.circlehelp.services.Logic.Companion.d
import server.circlehelp.value_classes.UrlValue
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime

const val management = "/management"
private const val thisBase = "$baseURL$management"

@RestController
class EmployeeManagementController(
    private val readonlyAttendanceRepository: ReadonlyAttendanceRepository,
    private val readonlyWorkShiftRepository: ReadonlyWorkShiftRepository,

    private val attendanceRepository: AttendanceRepository,

    private val accountService: AccountService,
    private val clock: Clock,
    private val workplaceLocation: WorkplaceLocation,
    attendanceArbiterManager: AttendanceArbiterManager,
    private val logic: Logic
) {

    private val attendanceArbiter = attendanceArbiterManager.topAttendanceArbiter

    /*
    @PutMapping("$thisBase/punchIn")
    @RepeatableReadTransaction
    fun punchIn(@CurrentSecurityContext securityContext: SecurityContext) {
        val user = accountService.getUser(securityContext)!!

        val workShift = readonlyWorkShiftRepository.findByUser(user)

        if (workShift != null && LocalTime.now(clock)
            in workShift.startTime.plusMinutes(30) ..workShift.startTime.plusHours(5))

        if (readonlyAttendanceRepository
            .existsByUserAndDate(user)
            .not())
            attendanceRepository.save(Attendance(user, TODO()))
    }

    @PutMapping("$thisBase/punchOut")
    @RepeatableReadTransaction
    fun punchOut(@CurrentSecurityContext securityContext: SecurityContext) {
        val user = accountService.getUser(securityContext)!!

        val attendance = readonlyAttendanceRepository.findByUserAndDate(user)

        val workShift = readonlyWorkShiftRepository.findByUser(user)

        if (workShift != null && LocalTime.now(clock)
            in workShift.startTime.plusMinutes(30) ..workShift.startTime.plusHours(5))

        if (attendance != null) {

            attendanceRepository.save(attendance.punchOut())
        }
    }

     */

    @RepeatableReadTransaction
    @PostMapping("$baseURL/checkAttendance")
    fun checkAttendance(@RequestBody @Valid attendanceRequest: AttendanceRequest,
                        @CurrentSecurityContext securityContext: SecurityContext) : AttendanceResponse {

        val user = accountService.getUser(securityContext)!!

        val workShift = readonlyWorkShiftRepository.findByUser(user)

        val attendance = readonlyAttendanceRepository.findByUserAndDate(user, attendanceRequest.currDate)

        if (attendanceRequest.type == AttendanceRequestType.PunchIn && attendance != null)
            throw IllegalArgumentException("Already punched in.")

        if (attendanceRequest.type == AttendanceRequestType.PunchIn && attendanceRequest.punchInTime == null )
            throw IllegalArgumentException("Punching in requires provided time.")

        if (attendanceRequest.type == AttendanceRequestType.PunchOut && attendance == null)
            throw IllegalArgumentException("Cannot punch out before punching in.")

        if (attendanceRequest.type == AttendanceRequestType.PunchOut && attendance!!.punchOutTime != null)
            throw IllegalArgumentException("Already punched out.")

        if (attendanceRequest.type == AttendanceRequestType.PunchOut && attendanceRequest.punchOutTime == null )
            throw IllegalArgumentException("Punching out requires provided time.")

        val distance = logic.haversine(workplaceLocation.location, Location(attendanceRequest.currLatitude, attendanceRequest.currLongitude))

        val limit = 30.d

        if (distance > limit)
            throw IllegalArgumentException("Your distance '$distance' must be less than $limit meters away to be validated for attendance.")

        val result = attendanceArbiter.decide(ArbitratedAttendance(
            attendanceRequest.currDate,
            if (attendanceRequest.type == AttendanceRequestType.PunchIn)
                attendanceRequest.punchInTime
            else
                null,
            if (attendanceRequest.type == AttendanceRequestType.PunchOut)
                attendanceRequest.punchOutTime
            else
                null,
            UrlValue(attendanceRequest.imageUrl)
        ), workShift!!)


        if (attendanceRequest.type == AttendanceRequestType.PunchIn)
        attendanceRepository.save(Attendance(
            user,
            result.currDate,
            result.punchInTime!!,
            null,
            UrlValue(result.imageUrl),
            result.type
        ))
        else
            attendanceRepository.save(attendance!!.apply {
                punchOutTime = attendanceRequest.punchOutTime!!
                type = result.type
            })

        return result
    }

    @RepeatableReadTransaction(readOnly = true)
    @GetMapping("$baseURL/attendanceInfoItems")
    fun attendanceInfoItems(@CurrentSecurityContext securityContext: SecurityContext): List<AttendanceResponse> {
        val user = accountService.getUser(securityContext)!!

        return readonlyAttendanceRepository.findAllByUser(user, Attendance.sortByDateDesc).map(AttendanceResponse::fromEntity)
    }

    @GetMapping("$thisBase/attendanceHistory")
    @RepeatableReadTransaction(readOnly = true)
    fun getPersonalAttendanceHistory(@CurrentSecurityContext securityContext: SecurityContext) : List<AttendanceDto> {
        val user = accountService.getUser(securityContext)!!

        return readonlyAttendanceRepository.findAllByUser(user, Attendance.sortByDateDesc).map(::AttendanceDto)
    }

    @GetMapping("/admin$thisBase/allAttendanceHistory")
    @RepeatableReadTransaction(readOnly = true)
    fun getAdminAllAttendanceHistory() : List<UserAttendanceDto> {

        return readonlyAttendanceRepository
            .findAll(Attendance.sortByDateDesc)
            .groupBy { it.user }.entries.toList()
            .map { UserAttendanceDto(it.key, it.value.map(::AttendanceDto)) }
    }
}