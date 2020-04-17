package moon.odyssey.webflux.utils.converter;

import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Optional;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class StringEncryptConverter implements AttributeConverter<String, String> {

    private static StringEncryptor stringEncryptor;

    @Autowired
    @Qualifier("jasyptStringEncryptor")
    public void setStringEncryptor(StringEncryptor encryptor) {
        StringEncryptConverter.stringEncryptor = encryptor;
    }

    @Override
    public String convertToDatabaseColumn(String entityString) {

        return Optional.ofNullable(entityString)
                       .filter(s -> !s.isEmpty())
                       .map(StringEncryptConverter.stringEncryptor::encrypt)
                       .orElse("");
    }

    @Override
    public String convertToEntityAttribute(String dbString) {

        return Optional.ofNullable(dbString)
                       .filter(s -> !s.isEmpty())
                       .map(StringEncryptConverter.stringEncryptor::decrypt)
                       .orElse("");
    }
}
