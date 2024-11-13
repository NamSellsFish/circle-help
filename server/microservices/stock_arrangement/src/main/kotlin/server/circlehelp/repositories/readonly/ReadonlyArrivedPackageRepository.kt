package server.circlehelp.repositories.readonly

import org.springframework.stereotype.Repository
import server.circlehelp.entities.ArrivedPackage

@Repository
interface ReadonlyArrivedPackageRepository : ReadonlyRepository<ArrivedPackage, Long> {

    fun findAllByOrderByDateDescIdDesc() : List<ArrivedPackage>
}