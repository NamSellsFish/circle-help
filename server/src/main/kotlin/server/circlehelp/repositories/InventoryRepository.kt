package server.circlehelp.repositories

import server.circlehelp.api.response.InventoryStockItem

fun interface InventoryRepository {
    fun getInventory(): List<InventoryStockItem>
}