package dbqa.util;

import dbqa.model.DbqaUser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;

@Configuration
@PropertySource("classpath:application.properties")
public class DbqaUtil {

    public static Logger queryLogger = (Logger) LogManager.getLogger("QueryLogger");
    public static Logger exceptionLogger = (Logger) LogManager.getLogger("ExceptionLogger");
    private static Environment environment;

    @Autowired //cannot autowire variable since it is static, so this will have to do
    public void setEnvironment(Environment environment){
        DbqaUtil.environment = environment;
    }

    public static DbqaUser getSessionUser(HttpServletRequest request) {
        return (DbqaUser)request.getSession().getAttribute("user");
    }

    public static String noAccessRedirect(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "You do not have access to that page");
        return "redirect:/login";
    }

    public static String genericErrorRedirect(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "An error occurred");
        return "redirect:/query";
    }

    public static boolean isValidated(DbqaUser user, String postedPassword) {
        boolean verified = false;
        //Get the user's password hash stored in the database
        String userPasswordHash = user.getHash();
        //Quick validation
        if(userPasswordHash == null || postedPassword == null) {
            throw new java.lang.IllegalArgumentException("No user hash or password found");
        }
        //Validated user hash stored against posted password
        return userPasswordHash.equals(hashPassword(postedPassword, user.getSalt()));
    }

    //Simple helper method to hash posted passwords with a strength of 32
    public static String hashPassword(String plainTextPassword, String salt) {
        return BCrypt.hashpw(plainTextPassword, salt);
    }
}
