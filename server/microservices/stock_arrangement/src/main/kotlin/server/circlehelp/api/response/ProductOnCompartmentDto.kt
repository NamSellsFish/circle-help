package server.circlehelp.api.response

import com.fasterxml.jackson.annotation.JsonProperty

data class ProductOnCompartmentDto(
    val position: CompartmentInfo,
    val statusCode: Int,
    val productDetails: ProductDetails?
) {
    constructor(position: CompartmentInfo, productDetails: ProductDetails?)
            : this(position, 0, productDetails)
}
