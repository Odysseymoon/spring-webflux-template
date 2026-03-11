package moon.odyssey.webflux.utils.exception

import org.springframework.http.HttpStatus
import java.util.function.Supplier

class AppBaseException(
    val status: HttpStatus = HttpStatus.BAD_REQUEST,
    val errorCode: ErrorCode? = null,
    val args: Array<Any?>? = null,
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause), Supplier<RuntimeException> {

    constructor(status: HttpStatus, code: ErrorCode) : this(status, code, null, null, null)

    constructor(status: HttpStatus, code: ErrorCode, msgArgs: Array<Any?>?) : this(status, code, msgArgs, null, null)

    constructor(status: HttpStatus, code: ErrorCode, msgArgs: Array<Any?>?, cause: Throwable) :
        this(status, code, msgArgs, cause.localizedMessage, cause)

    override fun get(): RuntimeException = this
}
