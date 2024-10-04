package server.circlehelp.api.response

data class InventoryStockItem(
    val id: Int,
    val name: String,
    val quantity: Int,
    val price: Double
    )