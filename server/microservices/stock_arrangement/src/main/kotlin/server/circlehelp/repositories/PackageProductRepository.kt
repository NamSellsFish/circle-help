package server.circlehelp.repositories

import jakarta.persistence.ManyToOne
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import server.circlehelp.entities.ArrivedPackage
import server.circlehelp.entities.PackageProduct
import server.circlehelp.entities.Product

@Repository
interface PackageProductRepository : JpaRepository<PackageProduct, Long> {

    fun findByOrderedPackageAndProduct(orderedPackage: ArrivedPackage,
                           product: Product
    ) : PackageProduct?
}