package server.login.configuration

import org.springframework.context.annotation.Configuration
import server.login.api.request.RegistrationDto
import server.login.entities.Roles
import server.login.repositories.AccountRepository
import server.login.services.AccountService
import server.login.repositories.readonly.ReadonlyAccountRepository
import server.login.value_classes.Password

@Configuration
class BuiltinAccountConfig(
    accountService: AccountService,
    accountRepository: ReadonlyAccountRepository) {

    init {
        if (accountRepository.count().toInt() == 0) {
            accountService.registerUser(
                RegistrationDto("root@admin.com","admin", Password("admin"), Roles.Admin))
            accountService.registerUser(
                RegistrationDto("employee@email.com", "employee", Password("employee"), Roles.Employee))
        }
    }
}