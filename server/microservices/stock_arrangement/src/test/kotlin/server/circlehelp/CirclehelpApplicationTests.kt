package server.circlehelp

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import server.circlehelp.entities.Layer
import server.circlehelp.repositories.RowRepository
import server.circlehelp.repositories.readonly.ReadonlyRowRepository
import org.junit.jupiter.api.Assertions.assertNotNull
import org.springframework.test.annotation.Rollback
import server.circlehelp.annotations.RepeatableReadTransaction

@RepeatableReadTransaction
@SpringBootTest
@Rollback
class CirclehelpApplicationTests {

	@PersistenceContext
	lateinit var entityManager: EntityManager

	@Autowired
	lateinit var readonlyRowRepository: ReadonlyRowRepository

	@Autowired
	lateinit var rowRepository: RowRepository
	@Test
	fun basicTransactionTest() {

		val number = 19

		rowRepository.save(Layer(number))
		val layer = readonlyRowRepository.findByNumber(number)

		assertNotNull(layer)

		rowRepository.delete(layer!!)
		assertNull(readonlyRowRepository.findByNumber(number))

		entityManager.flush()
	}
}
