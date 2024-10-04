package server.circlehelp.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import server.circlehelp.repositories.InventoryRepository
import java.util.Collections

@RestController
// @RequestMapping("/")
class InventoryController(private val inventoryRepository: InventoryRepository 
    = InventoryRepository { Collections.emptyList() }) {

    @GetMapping("/api/inventory")
    fun getInventory() = inventoryRepository.getInventory()
}