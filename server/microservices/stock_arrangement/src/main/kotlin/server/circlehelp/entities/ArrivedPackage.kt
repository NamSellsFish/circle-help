package server.circlehelp.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import org.jetbrains.annotations.NotNull
import java.time.LocalDateTime

@Entity
class ArrivedPackage(
    @Column(columnDefinition = "TIMESTAMP") var date: LocalDateTime,
    var supplier: String,
    @Id @GeneratedValue var id: Long? = null
)