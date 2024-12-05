package server.circlehelp.repositories.readonly

import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository
import server.circlehelp.entities.Product

@Repository
@Primary
interface ReadonlyProductRepository: ReadonlyRepository<Product, String> {
}