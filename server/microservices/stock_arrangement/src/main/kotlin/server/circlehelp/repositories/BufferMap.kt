package server.circlehelp.repositories

import org.springframework.data.domain.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.query.FluentQuery
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.ProductOnCompartment
import java.util.Optional
import java.util.function.Function

@NoRepositoryBean
class BufferMap<T, ID> : ProductOnCompartmentRepository {
    override fun deleteByCompartment(compartment: Compartment) {
        TODO("Not yet implemented")
    }

    override fun <S : ProductOnCompartment?> save(entity: S & Any): S & Any {
        TODO("Not yet implemented")
    }

    override fun <S : ProductOnCompartment?> saveAll(entities: MutableIterable<S>): MutableList<S> {
        TODO("Not yet implemented")
    }

    override fun <S : ProductOnCompartment?> findAll(example: Example<S>): MutableList<S> {
        TODO("Not yet implemented")
    }

    override fun <S : ProductOnCompartment?> findAll(
        example: Example<S>,
        sort: Sort
    ): MutableList<S> {
        TODO("Not yet implemented")
    }

    override fun findAll(): MutableList<ProductOnCompartment> {
        TODO("Not yet implemented")
    }

    override fun findAll(sort: Sort): MutableList<ProductOnCompartment> {
        TODO("Not yet implemented")
    }

    override fun findAll(pageable: Pageable): Page<ProductOnCompartment> {
        TODO("Not yet implemented")
    }

    override fun <S : ProductOnCompartment?> findAll(
        example: Example<S>,
        pageable: Pageable
    ): Page<S> {
        TODO("Not yet implemented")
    }

    override fun findAllById(ids: MutableIterable<Long>): MutableList<ProductOnCompartment> {
        TODO("Not yet implemented")
    }

    override fun count(): Long {
        TODO("Not yet implemented")
    }

    override fun <S : ProductOnCompartment?> count(example: Example<S>): Long {
        TODO("Not yet implemented")
    }

    override fun delete(entity: ProductOnCompartment) {
        TODO("Not yet implemented")
    }

    override fun deleteAllById(ids: MutableIterable<Long>) {
        TODO("Not yet implemented")
    }

    override fun deleteAll(entities: MutableIterable<ProductOnCompartment>) {
        TODO("Not yet implemented")
    }

    override fun deleteAll() {
        TODO("Not yet implemented")
    }

    override fun <S : ProductOnCompartment?> findOne(example: Example<S>): Optional<S> {
        TODO("Not yet implemented")
    }

    override fun <S : ProductOnCompartment?> exists(example: Example<S>): Boolean {
        TODO("Not yet implemented")
    }

    override fun <S : ProductOnCompartment?, R : Any?> findBy(
        example: Example<S>,
        queryFunction: Function<FluentQuery.FetchableFluentQuery<S>, R>
    ): R & Any {
        TODO("Not yet implemented")
    }

    override fun flush() {
        TODO("Not yet implemented")
    }

    override fun <S : ProductOnCompartment?> saveAndFlush(entity: S & Any): S & Any {
        TODO("Not yet implemented")
    }

    override fun <S : ProductOnCompartment?> saveAllAndFlush(entities: MutableIterable<S>): MutableList<S> {
        TODO("Not yet implemented")
    }

    override fun deleteAllInBatch(entities: MutableIterable<ProductOnCompartment>) {
        TODO("Not yet implemented")
    }

    override fun deleteAllInBatch() {
        TODO("Not yet implemented")
    }

    override fun deleteAllByIdInBatch(ids: MutableIterable<Long>) {
        TODO("Not yet implemented")
    }

    override fun getReferenceById(id: Long): ProductOnCompartment {
        TODO("Not yet implemented")
    }

    override fun getById(id: Long): ProductOnCompartment {
        TODO("Not yet implemented")
    }

    override fun getOne(id: Long): ProductOnCompartment {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: Long) {
        TODO("Not yet implemented")
    }

    override fun existsById(id: Long): Boolean {
        TODO("Not yet implemented")
    }

    override fun findById(id: Long): Optional<ProductOnCompartment> {
        TODO("Not yet implemented")
    }

}