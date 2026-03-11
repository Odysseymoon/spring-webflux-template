package moon.odyssey.webflux.utils.code

import jakarta.persistence.Converter

@Converter(autoApply = true)
class TFCodeConverter : AbstractBaseEnumConverter<TFCode, String>() {
    override fun getValueList(): Array<TFCode> = TFCode.values()
}
