package moon.odyssey.webflux.utils.exception

data class AppErrorResponse(
    val timestamp: Long,
    val path: String,
    val status: Int,
    val error: String,
    val message: String?,
    val requestId: String
)
