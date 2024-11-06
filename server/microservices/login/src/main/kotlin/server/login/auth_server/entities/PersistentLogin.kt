package server.server.login.auth_server.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "persistent_logins")
class PersistentLogin(
    @Column(nullable = false, length = 64)
    var username: String,

    @jakarta.persistence.Id
    @Column(length = 64)
    var series: String,

    @Column(nullable = false, length = 64)
    var token: String,

    @Column(nullable = false)
    var lastUsed: LocalDateTime

) {

}