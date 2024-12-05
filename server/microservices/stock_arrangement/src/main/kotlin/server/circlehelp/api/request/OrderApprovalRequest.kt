package server.circlehelp.api.request

import server.circlehelp.entities.ArrivedPackage
import server.circlehelp.entities.PackageProduct
import server.circlehelp.entities.Product
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class OrderApprovalRequest(
    val dateTime: LocalDateTime = LocalDateTime.now(),
    val supplier: String,
    val packageProducts: Iterable<PackageProductBody>
) {
    data class PackageProductBody(
        val sku: String,
        val wholesalePrice: BigDecimal,
        val quantity: Int,
        val expirationDate: LocalDate? = null
    ) {
        fun toPackageProduct(order: ArrivedPackage, product: Product) = PackageProduct(
            order,
            product,
            quantity,
            wholesalePrice,
            expirationDate
        )
    }

    fun toArrivedPackage() = ArrivedPackage(supplier, dateTime)

    companion object {
        val sample = OrderApprovalRequest(
            LocalDateTime.now(),
            "Reichstumer",
            listOf(
                PackageProductBody(
                    "DK000O",
                    BigDecimal(9),
                    1,
                    LocalDate.of(2025, 12, 31)
                ),
                PackageProductBody(
                    "FD000O",
                    BigDecimal(18),
                    1,
                    LocalDate.of(2025, 12, 31)
                )
            )
        )
    }
}
