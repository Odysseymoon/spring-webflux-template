package moon.odyssey.webflux.application.auth.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size


data class UserParam(
    @field:NotBlank
    @field:Size(min = 4, max = 30)
    val userId: String,
    @field:NotBlank
    @field:Size(min = 8, max = 50)
    val password: String
)
