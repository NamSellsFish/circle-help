package server.circlehelp.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import server.circlehelp.entities.InventoryStock
import server.circlehelp.repositories.InventoryRepository
import java.util.Collections

@Controller
//@RequestMapping("/")
class InventoryController(@Autowired private val inventoryRepository: InventoryRepository) {

    @GetMapping("/api/inventory")
    @ResponseBody
    fun getInventory(@RequestParam(defaultValue = "") searchTerm: String,
                     @RequestParam(defaultValue = "0") minQuantity: Int,
                     @RequestParam(defaultValue = "32767") maxQuantity: Int,
                     @RequestParam(defaultValue = "0") minPrice: Double,
                     @RequestParam(defaultValue = "32767") maxPrice: Double,
                     @RequestParam(defaultValue = "") sortColumn: String,
                     @RequestParam(defaultValue = "False") ascending: Boolean): List<InventoryStock> {
        var result = inventoryRepository
            .findAll()
            .filter {
                    i ->
                        (searchTerm.isEmpty() || i.product.name.contains(searchTerm))
                        ||
                        (i.inventoryQuantity in minQuantity..maxQuantity)
                        ||
                        (i.product.price in minPrice..maxPrice)
            }


        return result
    }

}