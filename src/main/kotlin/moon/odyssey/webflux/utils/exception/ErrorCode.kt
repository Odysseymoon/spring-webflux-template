package moon.odyssey.webflux.utils.exception

import moon.odyssey.webflux.utils.code.BaseEnumCode

enum class ErrorCode(override val value: String) : BaseEnumCode<String> {
    BASE_CODE("Error.Base"),
    CODE2("Error.Code2"),
    CODE3("Error.Code3")
}
