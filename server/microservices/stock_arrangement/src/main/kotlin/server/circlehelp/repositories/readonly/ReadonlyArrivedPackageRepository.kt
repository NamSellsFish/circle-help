package server.circlehelp.repositories.readonly

import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository
import server.circlehelp.entities.ArrivedPackage

@Repository
@Primary
interface ReadonlyArrivedPackageRepository : ReadonlyRepository<ArrivedPackage, Long> {

    fun findAllByOrderByDateTimeDescIdDesc() : List<ArrivedPackage>
}