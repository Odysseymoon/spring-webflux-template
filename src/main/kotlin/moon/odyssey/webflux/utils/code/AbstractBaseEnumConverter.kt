package moon.odyssey.webflux.utils.code

import jakarta.persistence.AttributeConverter

abstract class AbstractBaseEnumConverter<X, Y> : AttributeConverter<X, Y> where X : Enum<X>, X : BaseEnumCode<Y> {

    protected abstract fun getValueList(): Array<X>

    override fun convertToDatabaseColumn(attribute: X): Y {
        return attribute.value
    }

    override fun convertToEntityAttribute(dbData: Y): X {
        return getValueList().firstOrNull { it.value == dbData }
            ?: throw IllegalArgumentException("Unsupported type $dbData.")
    }
}
