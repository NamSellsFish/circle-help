package server.circlehelp.api.response

import com.fasterxml.jackson.annotation.JsonProperty

data class ProductOnCompartmentDto(
    val position: CompartmentPosition,
    val statusCode: Int,
    val productDetails: ProductDetails?
) {
    constructor(position: CompartmentPosition, productDetails: ProductDetails?)
            : this(position, 0, productDetails)
}
