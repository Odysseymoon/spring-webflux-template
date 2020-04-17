package moon.odyssey.webflux.application.auth.service;

import moon.odyssey.webflux.application.auth.entity.User;
import reactor.core.publisher.Mono;

public interface UserService {

    Mono<User> findById(String userId);

    Mono<User> addUser(String userId, String password);

}
