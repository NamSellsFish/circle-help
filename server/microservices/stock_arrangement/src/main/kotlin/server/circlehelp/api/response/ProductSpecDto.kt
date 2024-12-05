package server.circlehelp.api.response

import server.circlehelp.value_classes.UrlValue
import java.math.BigDecimal

data class ProductSpecDto(
    val packageID: Long,
    val sku: String,
    val name: String,
    val price: BigDecimal,
    val wholesalePrice: BigDecimal,
    val quantity: Int,
    val imageUrl: UrlValue?,
    val categories: String
)
