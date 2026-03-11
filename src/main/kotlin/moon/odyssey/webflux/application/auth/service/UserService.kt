package moon.odyssey.webflux.application.auth.service

import moon.odyssey.webflux.application.auth.entity.User
import reactor.core.publisher.Mono

interface UserService {
    fun findById(userId: String): Mono<User>
    fun addUser(userId: String, password: String): Mono<User>
}
