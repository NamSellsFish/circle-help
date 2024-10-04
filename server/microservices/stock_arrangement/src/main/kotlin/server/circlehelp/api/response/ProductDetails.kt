package server.circlehelp.api.response

import java.time.LocalDate

data class ProductDetails(
    val id: Long,
    val name: String,
    val price: Double,
    var wholesalePrice: Double,
    var expirationDate: LocalDate
)
