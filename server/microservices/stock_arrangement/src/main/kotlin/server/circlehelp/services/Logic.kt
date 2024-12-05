package server.circlehelp.services

import ch.obermuhlner.math.big.BigDecimalMath.sqrt
import ch.obermuhlner.math.big.BigDecimalMath.atan2
import ch.obermuhlner.math.big.BigDecimalMath.cos
import ch.obermuhlner.math.big.BigDecimalMath.sin
import ch.obermuhlner.math.big.BigDecimalMath.toRadians
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import server.circlehelp.annotations.RepeatableReadTransaction
import server.circlehelp.api.response.CompartmentPosition
import server.circlehelp.api.response.ErrorsResponse
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.EventProduct
import server.circlehelp.entities.Location
import server.circlehelp.entities.PackageProduct
import server.circlehelp.entities.Product
import server.circlehelp.entities.ProductOnCompartment
import server.circlehelp.repositories.ProductOnCompartmentRepository
import server.circlehelp.repositories.caches.CompartmentCache
import server.circlehelp.repositories.caches.LayerCache
import server.circlehelp.repositories.readonly.ReadonlyEventCompartmentRepository
import server.circlehelp.repositories.readonly.ReadonlyEventProductRepository
import java.math.BigDecimal
import java.math.MathContext
import java.time.Clock
import java.time.LocalDate

@Service
class Logic(private val readonlyRowRepository: LayerCache,
            private val readonlyCompartmentRepository: CompartmentCache,
            private val productOnCompartmentRepository: ProductOnCompartmentRepository,
            private val readonlyEventProductRepository: ReadonlyEventProductRepository,
            private val readonlyEventCompartmentRepository: ReadonlyEventCompartmentRepository,
            private val clock: Clock) {

    fun <T> item(obj: T) : Pair<T?, ErrorsResponse?> {
        return Pair(obj, null)
    }

    fun <T> error(errorsResponse: ErrorsResponse) : Pair<T?, ErrorsResponse> {
        return Pair(null, errorsResponse)
    }

    @RepeatableReadTransaction(readOnly = true)
    fun getCompartment(compartmentPosition: CompartmentPosition) : Pair<Compartment?, ErrorsResponse?> {
        val (rowNumber, compartmentNumber) = compartmentPosition

        val row = readonlyRowRepository.apply { checkTables() }
            .findByNumber(rowNumber)
            ?: return error(rowNotFoundResponse(rowNumber))

        val compartment = readonlyCompartmentRepository.apply { checkTables() }
            .findByLayerAndNumber(row, compartmentNumber)
            ?: return error(compartmentNotFoundResponse(compartmentNumber))

        return item(compartment)
    }

    fun isExpiring(packageProduct: PackageProduct) : Boolean {
        return packageProduct.expirationDate != null && LocalDate.now(clock).plusDays(1) >= packageProduct.expirationDate
    }

    @RepeatableReadTransaction
    fun updateExpiration(productOnCompartment: ProductOnCompartment) : Boolean {

        val expiring = isExpiring(productOnCompartment.packageProduct)

        if (expiring)
            productOnCompartmentRepository.save(productOnCompartment.with(status = 2))

        return expiring
    }

    @RepeatableReadTransaction
    @Deprecated("Use EventStatusArbiter.")
    fun updateEventStatus(productOnCompartment: ProductOnCompartment, activeEventProducts: Set<Product>) : Boolean {

        val event =
            productOnCompartment.statusUpdateAllowed() &&
            activeEventProducts.contains(productOnCompartment.packageProduct.product) &&
            readonlyEventCompartmentRepository.existsById(productOnCompartment.compartment.id!!).not()

        if (event)
            productOnCompartmentRepository.save(
                productOnCompartment.with(status = 4)
            )

        return event
    }

    fun productNotFoundResponse(sku: String) : ErrorsResponse {
        return ErrorsResponse("No product with SKU: $sku", HttpStatus.NOT_FOUND)
    }

    fun expiredProductArrangementAttemptResponse(packageProduct: PackageProduct) : ErrorsResponse {
        return ErrorsResponse(
            "Attempted to arrange expired or expiring product:\n" +
                    "   Product SKU: ${packageProduct.product.sku}\n" +
                    "   Expiration Date: ${packageProduct.expirationDate}",
        )
    }


    fun shelfNotFoundResponse(number: Int) : ErrorsResponse {
        return ErrorsResponse("No shelf with number: $number", HttpStatus.NOT_FOUND)
    }

    fun rowNotFoundResponse(number: Int) : ErrorsResponse {
        return ErrorsResponse("No row with number: $number", HttpStatus.NOT_FOUND)
    }

    fun compartmentNotFoundResponse(number: Int) : ErrorsResponse {
        return ErrorsResponse("No compartment with number: $number", HttpStatus.NOT_FOUND)
    }

    fun notInInventoryResponse(sku: String) : ErrorsResponse {
        return ErrorsResponse("Product not found in inventory: $sku")
    }

    fun notEnoughInInventoryResponse(sku: String, orderID: Long) : ErrorsResponse {
        return ErrorsResponse("Not enough of '$sku' of order $orderID in inventory.")
    }

    @RepeatableReadTransaction(readOnly = true)
    fun activeEventProducts() : List<EventProduct> {
        return readonlyEventProductRepository.findAll().filter {
            it.event.isActive()
        }
    }

    fun haversine(location: Location, otherLocation: Location): BigDecimal {

        val m = mathContext
        
        val dLat = toRadians(otherLocation.latitude - location.latitude, m)
        val dLon = toRadians(otherLocation.latitude - location.latitude, m)
        val startLat = toRadians(location.latitude, m)
        val endLat = toRadians(otherLocation.latitude, m)
        val a =
            sin(dLat / 2.d, m) * sin(dLat / 2.d, m) + cos(startLat, m) * cos(endLat, m) * sin(dLon / 2.d, m) * sin(dLon / 2.d, m)
        val c = 2.d * atan2(sqrt(a, m), sqrt(1.d - a, m), m)
        return earthRadius * c
    }

    companion object {
        /**
         * Conversion to [BigDecimal].
         */
        val Int.d get() = BigDecimal(this)
        val earthRadius = BigDecimal(6371e3)
        val mathContext: MathContext = MathContext.DECIMAL32
    }
}