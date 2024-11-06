package server.circlehelp.api.response

import java.math.BigDecimal

data class StockItem(
    val id: Long,
    val name: String,
    val price: BigDecimal,
)