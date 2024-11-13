package server.circlehelp.repositories.readonly

import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.ProductOnCompartment

@Repository
interface ReadonlyProductOnCompartmentRepository : ReadonlyRepository<ProductOnCompartment, Long> {

    fun existsByCompartment(compartment: Compartment) : Boolean

    fun findByCompartment(compartment: Compartment) : ProductOnCompartment?

    fun findAllByStatus(status: Int) : List<ProductOnCompartment>

    fun findAllByOrderByCompartmentNumberAscCompartmentLayerNumberAsc(): List<ProductOnCompartment>

    fun findAllByOrderByCompartmentLayerNumberAscCompartmentNumberAsc(): List<ProductOnCompartment>

    @Query("select p.id from ProductOnCompartment p")
    fun findAllId() : List<Long>

    @Query("select p.packageProduct.id from ProductOnCompartment p where p.id = :id")
    fun findPackageProductIdById(id: Long) : Long?
}