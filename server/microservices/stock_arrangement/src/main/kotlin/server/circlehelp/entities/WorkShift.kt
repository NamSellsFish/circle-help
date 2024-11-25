package server.circlehelp.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import lombok.EqualsAndHashCode
import server.circlehelp.auth.User
import java.time.LocalTime

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class WorkShift(
    @OneToOne @MapsId("userEmailField") val user: User,
    @Column(nullable = false) val startTime: LocalTime,
    @Column(nullable = false) val endTime: LocalTime
    ) {

    @Id
    @EqualsAndHashCode.Include
    val userEmailField: String = user.email
}