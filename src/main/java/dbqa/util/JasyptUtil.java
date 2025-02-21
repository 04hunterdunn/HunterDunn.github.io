package dbqa.util;

import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:application.properties")
public class JasyptUtil {

    private static Environment environment;

    @Autowired //cannot autowire variable since it is static, so this will have to do
    public void setEnvironment(Environment environment){
        JasyptUtil.environment = environment;
    }

    public static String encrypt(String text) {
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        String jasyptEncryptorPassword = environment.getProperty("jasypt.encryptor.password");
        textEncryptor.setPassword(jasyptEncryptorPassword);
        return textEncryptor.encrypt(text);
    }

    public static String decrypt(String text) {
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        String jasyptEncryptorPassword = environment.getProperty("jasypt.encryptor.password");
        textEncryptor.setPassword(jasyptEncryptorPassword);
        return textEncryptor.decrypt(text);
    }
}
