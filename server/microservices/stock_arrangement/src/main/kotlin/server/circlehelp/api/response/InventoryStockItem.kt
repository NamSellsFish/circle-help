package server.circlehelp.api.response

import com.fasterxml.jackson.annotation.JsonIgnore
import java.math.BigDecimal
import java.time.LocalDate

data class InventoryStockItem(
    val packageID: Long,
    val sku: String,
    val name: String,
    val price: BigDecimal,
    val wholesalePrice: BigDecimal,
    val quantity: Int,
    val expirationDate: LocalDate?,
    val imageUrl: String?,
    val categories: List<String>
) {

    @JsonIgnore
    fun getExpirationDateOrMax(): LocalDate = expirationDate ?: LocalDate.MAX


    companion object {
        fun getComparator(fieldName: String) : Comparator<InventoryStockItem> {
            return compartorsMap[fieldName]?: Comparator { o1, o2 -> o1.toString().compareTo(o2.toString()) }
        }

        val compartorsMap: Map<String, Comparator<InventoryStockItem>> = mapOf(
                    "packageID" to Comparator { o1, o2 -> o1.packageID.compareTo(o2.packageID) },
                    "sku" to Comparator { o1, o2 -> o1.sku.compareTo(o2.sku) },
                    "name" to Comparator { o1, o2 -> o1.name.compareTo(o2.name) },
                    "price" to Comparator { o1, o2 -> o1.price.compareTo(o2.price) },
                    "wholesalePrice" to Comparator { o1, o2 ->
                        o1.wholesalePrice.compareTo(o2.wholesalePrice)
                    },
                    "quantity" to Comparator { o1, o2 -> o1.quantity.compareTo(o2.quantity) },
                    "expirationDate" to Comparator { o1, o2 ->
                        o1.getExpirationDateOrMax()
                            .compareTo(o2.getExpirationDateOrMax())
                    },
                    "imageUrl" to Comparator { o1, o2 ->
                        o1.imageUrl.orEmpty().compareTo(o2.imageUrl.orEmpty())
                    }
                )
    }
}