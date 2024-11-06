package server.circlehelp.repositories.readonly

import org.springframework.stereotype.Repository
import server.circlehelp.entities.ImageSource
import server.circlehelp.entities.Product

@Repository
interface ReadonlyImageSourceRepository : ReadonlyRepository<ImageSource, String> {

    fun findAllByProduct(product: Product) : List<ImageSource>
}