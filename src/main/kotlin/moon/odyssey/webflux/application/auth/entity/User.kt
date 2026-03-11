package moon.odyssey.webflux.application.auth.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import java.io.Serializable

@Entity
@Table(name = "user")
class User(
    @Id
    var userId: String = "",
    var password: String = "",
    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinTable(
        name = "user_scope",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "authority")]
    )
    var authorities: MutableSet<Scope> = mutableSetOf()
) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        return userId == other.userId
    }

    override fun hashCode(): Int = userId.hashCode()

    override fun toString(): String {
        return "User(userId=$userId, password=$password, authorities=$authorities)"
    }
}
