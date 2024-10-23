package server.circlehelp.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import server.circlehelp.api.response.InventoryStockItem
import server.circlehelp.repositories.InventoryRepository
import server.circlehelp.utilities.Logic
import java.util.stream.Collectors.groupingBy
import java.util.stream.Collectors.summingInt

@Controller
//@RequestMapping("/")
class InventoryController(@Autowired private val inventoryRepository: InventoryRepository,
                          private val logic: Logic) {

    @GetMapping("/api/inventory")
    @ResponseBody
    fun getInventory(@RequestParam(defaultValue = "") searchTerm: String,
                     @RequestParam(defaultValue = "0") minQuantity: Int,
                     @RequestParam(defaultValue = "32767") maxQuantity: Int,
                     @RequestParam(defaultValue = "0") minPrice: Double,
                     @RequestParam(defaultValue = "32767") maxPrice: Double,
                     @RequestParam(defaultValue = "") sortColumn: String,
                     @RequestParam(defaultValue = "False") ascending: Boolean): MutableCollection<InventoryStockItem> {
        var sorter = Sort.by(sortColumn)

        if (!ascending) sorter.descending().also { sorter = it }

        val result = inventoryRepository
            .findAll(sorter)
            .stream()
            .filter {
                (searchTerm.isEmpty() || it.packageProduct.product.name.contains(searchTerm, true))
                        &&
                        (it.inventoryQuantity in minQuantity..maxQuantity)
                        &&
                        (it.packageProduct.product.price in minPrice..maxPrice)
            }
            .collect(groupingBy {it.packageProduct.product.id})
            .mapValuesTo(HashMap()) {
                val items = it.value
                val item = items[0].packageProduct.product
                InventoryStockItem(
                    item.id!!,
                    item.name,
                    items.stream().collect(summingInt {
                        if (logic.isExpiring(it.packageProduct))
                            0
                        else
                            it.inventoryQuantity
                    }),
                    item.price
                )
            }

        return result.values
    }

}