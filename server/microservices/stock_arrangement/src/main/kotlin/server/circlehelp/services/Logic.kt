package server.circlehelp.services

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import server.circlehelp.api.response.CompartmentPosition
import server.circlehelp.api.response.ErrorResponse
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.PackageProduct
import server.circlehelp.entities.ProductOnCompartment
import server.circlehelp.repositories.CompartmentRepository
import server.circlehelp.repositories.ProductOnCompartmentRepository
import server.circlehelp.repositories.RowRepository
import server.circlehelp.repositories.ShelvesRepository
import java.time.LocalDate

@Service
class Logic(private val shelvesRepository: ShelvesRepository,
            private val rowRepository: RowRepository,
            private val compartmentRepository: CompartmentRepository,
            private val productOnCompartmentRepository: ProductOnCompartmentRepository) {

    fun <T> item(obj: T) : Pair<T?, ErrorResponse?> {
        return Pair(obj, null)
    }

    fun <T> error(errorResponse: ErrorResponse) : Pair<T?, ErrorResponse> {
        return Pair(null, errorResponse)
    }

    fun getCompartment(compartmentPosition: CompartmentPosition) : Pair<Compartment?, ErrorResponse?> {
        val (shelfNumber, rowNumber, compartmentNumber) = compartmentPosition

        val shelf = shelvesRepository.findByNumber(shelfNumber)
            ?: return error(rowNotFoundResponse(rowNumber))

        val row = rowRepository.findByShelfAndNumber(shelf, rowNumber)
            ?: return error(rowNotFoundResponse(rowNumber))

        val compartment = compartmentRepository.findByLayerAndNumber(row, compartmentNumber)
            ?: return error(compartmentNotFoundResponse(compartmentNumber))

        return item(compartment)
    }

    fun isExpiring(packageProduct: PackageProduct) : Boolean {
        return packageProduct.expirationDate != null && LocalDate.now().plusDays(1) >= packageProduct.expirationDate
    }

    fun updateExpiration(productOnCompartment: ProductOnCompartment) : Boolean {

        val expiring = isExpiring(productOnCompartment.packageProduct)

        if (expiring)
            productOnCompartment.status = 2
            productOnCompartmentRepository.save(productOnCompartment)

        return expiring
    }

    fun productNotFoundResponse(sku: String) : ErrorResponse {
        return ErrorResponse("No product with SKU: $sku", HttpStatus.NOT_FOUND)
    }

    fun expiredProductArrangementAttemptResponse(packageProduct: PackageProduct) : ErrorResponse {
        return ErrorResponse(
            "Attempted to arrange expired or expiring product:\n" +
                    "   Product SKU: ${packageProduct.product.sku}\n" +
                    "   Expiration Date: ${packageProduct.expirationDate}",
        )
    }


    fun shelfNotFoundResponse(number: Int) : ErrorResponse {
        return ErrorResponse("No shelf with number: $number", HttpStatus.NOT_FOUND)
    }

    fun rowNotFoundResponse(number: Int) : ErrorResponse {
        return ErrorResponse("No row with number: $number", HttpStatus.NOT_FOUND)
    }

    fun compartmentNotFoundResponse(number: Int) : ErrorResponse {
        return ErrorResponse("No compartment with number: $number", HttpStatus.NOT_FOUND)
    }

    fun notInInventoryResponse(sku: String) : ErrorResponse {
        return ErrorResponse("Product not found in inventory: $sku")
    }
}