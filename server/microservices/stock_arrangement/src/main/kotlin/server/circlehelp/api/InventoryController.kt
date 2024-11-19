package server.circlehelp.api

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
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
import server.circlehelp.annotations.RepeatableReadTransaction
import server.circlehelp.api.response.InventoryStockItem
import server.circlehelp.repositories.readonly.ReadonlyImageSourceRepository
import server.circlehelp.repositories.readonly.ReadonlyInventoryRepository
import server.circlehelp.repositories.readonly.ReadonlyProductCategorizationRepository
import server.circlehelp.services.Logic
import java.math.BigDecimal
import kotlin.math.min

const val inventory = "/inventory"
@Controller
@RequestMapping(baseURL)
@RepeatableReadTransaction(readOnly = true)
class InventoryController(private val readonlyInventoryRepository: ReadonlyInventoryRepository,
                          private val imageSourceRepository: ReadonlyImageSourceRepository,
                          private val productCategorizationRepository: ReadonlyProductCategorizationRepository,

                          private val logic: Logic,
                          objectMapperBuilder: Jackson2ObjectMapperBuilder) {

    private val objectMapper = objectMapperBuilder.build<ObjectMapper>()
    private val logger = LoggerFactory.getLogger(InventoryController::class.java)

    @GetMapping(inventory)
    fun getInventory(@RequestParam(defaultValue = "") searchTerm: String,
                     @RequestParam(defaultValue = "0") minQuantity: Int,
                     @RequestParam(defaultValue = Int.MAX_VALUE.toString()) maxQuantity: Int,
                     @RequestParam(defaultValue = "0") minPrice: BigDecimal,
                     @RequestParam(defaultValue = Int.MAX_VALUE.toString()) maxPrice: BigDecimal,
                     @RequestParam(defaultValue = "") sortColumn: String,
                     @RequestParam(defaultValue = "") sortOption: String,
                     @RequestParam(defaultValue = "0") page: Int,
                     @RequestParam(defaultValue = "10") pageSize: Int,
                     @RequestParam(defaultValue = "false") onlyExpired: Boolean): ResponseEntity<String> {
        return getInventoryImpl(
            searchTerm, minQuantity, maxQuantity, minPrice, maxPrice, sortColumn, sortOption, page, pageSize, onlyExpired,
            true || ( searchTerm.isEmpty() &&
            minQuantity == 0 &&
            maxQuantity == Integer.MAX_VALUE &&
            minPrice == BigDecimal.ZERO &&
            maxPrice == BigDecimal(Int.MAX_VALUE) &&
            sortColumn.isEmpty() &&
            sortOption.isEmpty()).not()
        )
    }

    private fun getInventoryImpl(
        searchTerm: String,
        minQuantity: Int,
        maxQuantity: Int,
        minPrice: BigDecimal,
        maxPrice: BigDecimal,
        sortColumn: String,
        sortOption: String,
        page: Int,
        pageSize: Int,
        onlyExpired: Boolean,
        appliedFilterOrSort: Boolean): ResponseEntity<String> {

        val inventoryStock = readonlyInventoryRepository
            .findAllByOrderByPackageProductOrderedPackageDateDescPackageProductOrderedPackageIdDesc()
            .stream()
            .filter {
                (searchTerm.isEmpty() || it.packageProduct.product.name.contains(searchTerm, true))
                        &&
                        (it.packageProduct.product.price in minPrice..maxPrice)
                        &&
                        (it.packageProduct.importedQuantity in minQuantity..maxQuantity)
                        &&
                        (logic.isExpiring(it.packageProduct).complement().xor(onlyExpired))
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

        val sort =
            if (sortColumn.isBlank())
                Sort.unsorted()
            else
                Sort.by(sortColumn)

        //val pageable = PageRequest.of(page, pageSize, sort)
        val list = body.toList()

        val pageObj = toPage(list, page, pageSize, sort, appliedFilterOrSort)

        return ResponseEntity.ok(objectMapper.writeValueAsString(pageObj))
    }

    private class PageReImpl<T>(list: List<T>, pageable: Pageable, val total: Long) : PageImpl<T>(list, pageable, total) {
        override fun getTotalElements(): Long {
            return total
        }
    }

    private fun <T> toPage(list: List<T>, pageNo: Int, size: Int, sort: Sort,
                           includePreviousPages: Boolean) : PagedModel<T> {
        val pageable = PageRequest.of(min(pageNo, list.size), min(size, list.size), sort)

        return PagedModel(
            PageReImpl(
                list.subList(
                    min(if (includePreviousPages) 0 else pageable.offset.toInt(), list.size),
                    min(pageable.offset.toInt() + pageable.pageSize, list.size)
                ),
                pageable,
                list.size.toLong()
            )
        )
    }


}