package moon.odyssey.webflux.application.auth.repository

import moon.odyssey.webflux.application.auth.entity.Scope
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ScopeRepository : JpaRepository<Scope, String>
