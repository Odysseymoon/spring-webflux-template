package moon.odyssey.webflux.application.auth.service.support

import moon.odyssey.webflux.application.auth.entity.User
import moon.odyssey.webflux.application.auth.repository.ScopeRepository
import moon.odyssey.webflux.application.auth.repository.UserRepository
import moon.odyssey.webflux.application.auth.service.UserService
import moon.odyssey.webflux.utils.reactive.RxUtil
import org.hibernate.Hibernate
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler

@Service
class UserSimpleService(
    private val userRepository: UserRepository,
    private val scopeRepository: ScopeRepository,
    private val cacheManager: CacheManager,
    @Qualifier("jdbcScheduler") private val jdbcScheduler: Scheduler,
    private val transactionTemplate: TransactionTemplate
) : UserService {

    @Transactional(readOnly = true)
    override fun findById(userId: String): Mono<User> {
        val retriever = Mono.defer {
            transactionTemplate.execute {
                userRepository.findById(userId)
                    .map { user ->
                        Hibernate.initialize(user.authorities)
                        user
                    }
                    .map { Mono.just(it) }
                    .orElseGet { Mono.empty() }
            } ?: Mono.empty()
        }.subscribeOn(jdbcScheduler)

        return RxUtil.cacheMono(cacheManager, "user", userId, retriever, User::class.java)
    }

    @Transactional
    override fun addUser(userId: String, password: String): Mono<User> {
        return Mono.defer {
            transactionTemplate.execute {
                val user = userRepository.findById(userId)
                    .orElseGet {
                        val newbie = User()
                        newbie.userId = userId
                        newbie
                    }
                user.password = password
                user.authorities.clear()
                user.authorities.addAll(scopeRepository.findAll())
                Mono.just(userRepository.save(user))
            } ?: Mono.empty()
        }.subscribeOn(jdbcScheduler)
    }
}
