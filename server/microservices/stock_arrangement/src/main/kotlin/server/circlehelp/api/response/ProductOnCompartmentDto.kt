package server.circlehelp.api.response

import com.fasterxml.jackson.annotation.JsonProperty

data class ProductOnCompartmentDto(
    val position: CompartmentInfo,
    val statusCode: Int,
    val category: String,
    val productDetails: ProductDetails?
) {
}
