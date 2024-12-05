package server.circlehelp.repositories.readonly

import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository
import server.circlehelp.auth.Admin
import server.circlehelp.auth.Employee
import server.circlehelp.auth.User
import server.circlehelp.entities.Appraisable

@Repository
interface ReadonlyAppraisableRepository : ReadonlyRepository<Appraisable, Long> {

    fun findAllBySubmitter(submitter: User, sort: Sort = Sort.unsorted()): List<Appraisable>

    fun findAllByApprovalDataAppraiser(appraiser: User, sort: Sort = Sort.unsorted()): List<Appraisable>
}