package server.circlehelp.api.request

import server.circlehelp.value_classes.UrlValue
import java.math.BigDecimal
import java.time.LocalDate

data class ImportProduct(
    val packageID: Long,
    val supplier: String,
    val sku: String,
    val name: String,
    val price: BigDecimal,
    val wholesalePrice: BigDecimal,
    val quantity: Int,
    val expirationDate: LocalDate?,
    val imageUrl: String,
    val categories: List<String>,
    val note: String? = null
)
