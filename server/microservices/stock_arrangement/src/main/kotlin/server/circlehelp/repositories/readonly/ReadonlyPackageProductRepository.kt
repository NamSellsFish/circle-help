package server.circlehelp.repositories.readonly

import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository
import server.circlehelp.entities.ArrivedPackage
import server.circlehelp.entities.PackageProduct
import server.circlehelp.entities.Product

@Repository
@Primary
interface ReadonlyPackageProductRepository : ReadonlyRepository<PackageProduct, Long> {
    fun findByOrderedPackageAndProduct(orderedPackage: ArrivedPackage,
                                       product: Product
    ) : PackageProduct?

    fun findAllByOrderedPackage(orderedPackage: ArrivedPackage) : List<PackageProduct>
    fun findAllByProduct(product: Product) : List<PackageProduct>

}