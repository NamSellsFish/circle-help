package server.circlehelp.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Version
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime
import kotlin.reflect.KClass

@Entity
class TableAudit protected constructor(
    @Id @Column(length = 80)
    val tableName: String,
    @LastModifiedDate
    //@Version
    val updatedDate: LocalDateTime = LocalDateTime.now()
) {

    fun asUpdated(): TableAudit {
        return TableAudit(tableName)
    }

    companion object {

        inline fun <reified T> fromClass(): TableAudit {
            return withPascalCase(T::class.simpleName!!)
        }

        inline fun <reified T> toSnakeCase(): String {
            return toSnakeCase(T::class.simpleName!!)
        }

        fun toSnakeCase(className: String): String {
            return className.replace("([a-z])([A-Z]+)".toRegex(), "$1_$2").lowercase()
        }

        fun withPascalCase(className: String): TableAudit {
            return TableAudit(toSnakeCase(className))
        }

        fun withSnakeCase(className: String) : TableAudit {
            return TableAudit(className)
        }

        fun fromClass(classObj: Class<*>): TableAudit {
            return withPascalCase(classObj.simpleName)
        }

        fun fromClass(classObj: KClass<*>): TableAudit {
            return withPascalCase(classObj.simpleName!!)
        }
    }
}