package moon.odyssey.webflux.utils.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.jasypt.encryption.StringEncryptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

@Converter
class StringEncryptConverter : AttributeConverter<String, String> {

    companion object {
        private lateinit var stringEncryptor: StringEncryptor
    }

    @Autowired
    @Qualifier("jasyptStringEncryptor")
    fun setStringEncryptor(encryptor: StringEncryptor) {
        stringEncryptor = encryptor
    }

    override fun convertToDatabaseColumn(entityString: String?): String {
        return entityString
            ?.takeIf { it.isNotEmpty() }
            ?.let { stringEncryptor.encrypt(it) }
            ?: ""
    }

    override fun convertToEntityAttribute(dbString: String?): String {
        return dbString
            ?.takeIf { it.isNotEmpty() }
            ?.let { stringEncryptor.decrypt(it) }
            ?: ""
    }
}
