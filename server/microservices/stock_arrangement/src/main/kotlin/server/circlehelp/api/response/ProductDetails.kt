package server.circlehelp.api.response

import java.math.BigDecimal
import java.time.LocalDate

data class ProductDetails(
    val packageID: Long,
    val sku: String,
    val name: String,
    val price: BigDecimal,
    var wholesalePrice: BigDecimal,
    var expirationDate: LocalDate?
)
