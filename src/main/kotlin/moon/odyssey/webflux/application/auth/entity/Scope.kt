package moon.odyssey.webflux.application.auth.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.io.Serializable

@Entity
@Table(name = "scope")
class Scope(
    @Id
    var authority: String = "",
    var description: String = ""
) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Scope) return false
        return authority == other.authority
    }

    override fun hashCode(): Int = authority.hashCode()

    override fun toString(): String {
        return "Scope(authority=$authority, description=$description)"
    }
}
