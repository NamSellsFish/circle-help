package server.circlehelp.api.response

data class InventoryStockItem(
    val productId: Long,
    val name: String,
    val quantity: Int,
    val price: Double
    )