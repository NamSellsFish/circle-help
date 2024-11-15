package server.circlehelp.api

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PagedModel
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import server.circlehelp.api.response.InventoryStockItem
import server.circlehelp.repositories.InventoryRepository
import server.circlehelp.repositories.PackageProductRepository
import server.circlehelp.repositories.readonly.ReadonlyImageSourceRepository
import server.circlehelp.repositories.readonly.ReadonlyProductCategorizationRepository
import server.circlehelp.services.Logic
import java.math.BigDecimal
import java.util.Comparator
import kotlin.math.min

const val inventory = "/inventory"
@Controller
@RequestMapping(baseURL)
class InventoryController(private val inventoryRepository: InventoryRepository,
                          private val packageProductRepository: PackageProductRepository,
                          private val imageSourceRepository: ReadonlyImageSourceRepository,
                          private val productCategorizationRepository: ReadonlyProductCategorizationRepository,

                          private val logic: Logic,
                          objectMapperBuilder: Jackson2ObjectMapperBuilder) {

    private val objectMapper = objectMapperBuilder.build<ObjectMapper>()
    private val logger = LoggerFactory.getLogger(InventoryController::class.java)


    @GetMapping(inventory)
    fun getInventory(@RequestParam(defaultValue = "") searchTerm: String,
                     @RequestParam(defaultValue = "0") minQuantity: Int,
                     @RequestParam(defaultValue = Integer.MAX_VALUE.toString()) maxQuantity: Int,
                     @RequestParam(defaultValue = "0") minPrice: BigDecimal,
                     @RequestParam(defaultValue = Integer.MAX_VALUE.toString()) maxPrice: BigDecimal,
                     @RequestParam(defaultValue = "") sortColumn: String,
                     @RequestParam(defaultValue = "") sortOption: String,
                     @RequestParam(defaultValue = "0") page: Int,
                     @RequestParam(defaultValue = Integer.MAX_VALUE.toString()) size: Int): ResponseEntity<String> {

        val inventoryStock = inventoryRepository
            .findAll()
            .stream()
            .filter {
                (searchTerm.isEmpty() || it.packageProduct.product.name.contains(searchTerm, true))
                        &&
                        (it.packageProduct.product.price in minPrice..maxPrice)
                        &&
                        (it.packageProduct.importedQuantity in minQuantity..maxQuantity)
                        &&
                        (logic.isExpiring(it.packageProduct).complement())
            }

        val result = inventoryStock
            .map {
                InventoryStockItem(
                    it.packageProduct.orderedPackage.id!!,
                    it.packageProduct.product.sku,
                    it.packageProduct.product.name,
                    it.packageProduct.product.price,
                    it.packageProduct.wholesalePrice,
                    it.inventoryQuantity,
                    it.packageProduct.expirationDate,
                    imageSourceRepository.findAllByProduct(it.packageProduct.product).firstOrNull()?.url,
                    productCategorizationRepository.findAllByProduct(it.packageProduct.product).map { it.category.name }
                )
            }

        /*
        val result = inventoryStock
            .collect(groupingBy {it.packageProduct.product.sku})
            .mapValuesTo(HashMap()) {
                val items = it.value
                items.sortBy { it.packageProduct.expirationDate ?: LocalDate.MAX }
                val item = items[0].packageProduct.product
                val packageProduct = items[0].packageProduct
                InventoryStockItem(
                    item.sku,
                    item.name,
                    item.price,
                    packageProduct.wholesalePrice,
                    items.stream().collect(summingInt {
                        if (logic.isExpiring(it.packageProduct))
                            0
                        else
                            it.inventoryQuantity
                    }),
                    packageProduct.expirationDate,
                    imageSourceRepository.findAllByProduct(item).firstOrNull()?.url,
                    productCategorizationRepository.findAllByProduct(item).map { it.category.name }
                )
            }
         */

        val comparator = InventoryStockItem.getComparator(sortColumn).let {
            when (sortOption) {
                "desc" -> it.reversed()
                "asc" -> it
                else -> null
            }
        }

        val body = result.filter { (it.quantity in minQuantity..maxQuantity) }
            .let {
                if (comparator != null)
                    it.sorted(comparator)
                else
                    it
            }

        val sort = when(sortColumn.isBlank()) {
            true -> Sort.unsorted()
            false -> Sort.by(sortColumn)
        }

        val pageable = PageRequest.of(page, size, sort)

        val pageObj = toPage(body.toList(), page, size, sort)

        return ResponseEntity.ok(objectMapper.writeValueAsString(pageObj))
    }

    private fun <T> toPage(list: List<T>, pageNo: Int, size: Int, sort: Sort) : PagedModel<T> {
        val pageable = PageRequest.of(min(pageNo, list.size), min(size, list.size), sort)

        return PagedModel(
            PageImpl(
                list.subList(
                    min(pageable.offset.toInt(), list.size),
                    min(pageable.offset.toInt() + pageable.pageSize, list.size)
                ),
                pageable,
                list.size.toLong()
            )
        )
    }
}