package server.circlehelp.api.request

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.contains

data class PackageProductItem(
    val sku: String,
    val packageID: Long,
) {

    companion object {
        fun matchesJsonNode(node: JsonNode) : Boolean {
            return node.contains(PackageProductItem::sku.name)
                    && node.contains(PackageProductItem::packageID.name)
                    // && node.size() == 2
        }
    }
}


