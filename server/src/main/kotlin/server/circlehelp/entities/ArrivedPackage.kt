package server.circlehelp.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity
class ArrivedPackage(
    @Id @GeneratedValue var id: Long? = null,
    var date: LocalDateTime,
    var supplier: String
)