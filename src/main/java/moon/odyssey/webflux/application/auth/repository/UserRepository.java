package moon.odyssey.webflux.application.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import moon.odyssey.webflux.application.auth.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
}
