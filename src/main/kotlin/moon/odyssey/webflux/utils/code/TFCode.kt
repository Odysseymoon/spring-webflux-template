package moon.odyssey.webflux.utils.code

enum class TFCode(override val value: String) : BaseEnumCode<String> {
    TRUE("T"),
    FALSE("F"),
    NULL("")
}
