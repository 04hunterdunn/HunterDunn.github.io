package dbqa.controllers;

import dbqa.dao.DbqaUserDao;
import dbqa.dao.LtiConsumerDao;
import dbqa.model.DbqaUser;
import dbqa.model.LtiConsumer;
import dbqa.model.LtiData;
import dbqa.util.BCrypt;
import dbqa.util.DbqaUtil;
import dbqa.util.JasyptUtil;
import org.imsglobal.lti.launch.LtiOauthVerifier;
import org.imsglobal.lti.launch.LtiVerificationException;
import org.imsglobal.lti.launch.LtiVerificationResult;
import org.imsglobal.lti.launch.LtiVerifier;
import org.json.simple.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;

@Controller
public class DbqaUserController {

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String getLoginPage(RedirectAttributes redirectAttributes) {
        return "/login";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String handleLogin(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        String dest = "";
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        DbqaUser user = DbqaUserDao.getUser(email);

        if (user != null && DbqaUtil.isValidated(user, password)) {
            request.getSession().setAttribute("user", user);
            QueryController.removeQuerySessionData(request);
            //uncomment line below to force enable query step explanations
            //request.getSession().setAttribute("displayStepExplanation", true);
            dest = "redirect:/query";
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid username and/or password");
            dest = "redirect:/login";
        }
        return dest;
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String handleLogout(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        request.getSession().removeAttribute("user");
        request.getSession().removeAttribute("oauth_consumer_key");
        QueryController.removeQuerySessionData(request);
        redirectAttributes.addFlashAttribute("success", "You have been logged out");
        return "redirect:/login";
    }

    @RequestMapping(value = "/user/{userId}/query/{queryType}", method = {RequestMethod.GET, RequestMethod.POST})
    public String loadPageForUser(HttpServletRequest request, RedirectAttributes redirectAttributes, @PathVariable int userId, @PathVariable String queryType) {
        DbqaUser requestedUser = DbqaUserDao.getUser(userId);

        if(requestedUser == null || (requestedUser.getRequiresAuthentication() && !requestedUser.equals(request.getSession().getAttribute("user")))) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to view that page");
            return "redirect:/login";
        } else {
            String key = request.getParameter("oauth_consumer_key");
            //if authenticated via LTI
            if(key != null && !key.isEmpty()) {
                LtiVerifier ltiVerifier = new LtiOauthVerifier();
                LtiConsumer ltiConsumer = LtiConsumerDao.getLtiConsumerByKey(key);
                if (ltiConsumer != null) {
                    String secret = JasyptUtil.decrypt(ltiConsumer.getSecret());
                    try {
                        LtiVerificationResult ltiResult = ltiVerifier.verify(request, secret);
                        if (ltiResult.getSuccess()) {
                            /*
                            String canvasUserId = request.getParameter("user_id");
                            String url = request.getParameter("lis_outcome_service_url");
                            String sourcedid = request.getParameter("lis_result_sourcedid");
                            String displayStepExplanation = request.getParameter("ext_display_step_explanation");
                            LtiData ltiData = new LtiData(key, url, sourcedid, canvasUserId);
                            request.getSession().setAttribute("ltiData", ltiData);
                            if (displayStepExplanation != null && displayStepExplanation.equalsIgnoreCase("true")) {
                                request.getSession().setAttribute("displayStepExplanation", true);
                            } else {
                                request.getSession().setAttribute("displayStepExplanation", false);
                            }

                             */
                            LtiController.consumeLtiData(key, request);
                        }
                    } catch (LtiVerificationException e) {
                        return DbqaUtil.noAccessRedirect(redirectAttributes);
                    }
                }
            }
            QueryController.removeQuerySessionData(request);
            request.getSession().setAttribute("user", requestedUser);
            return "redirect:/query/"+queryType;
        }
    }

    @RequestMapping(value = "/user/add", method = RequestMethod.GET)
    public String getAddPage(RedirectAttributes redirectAttributes) {
        return "add_user";
    }

    @RequestMapping(value = "/user/add", method = RequestMethod.POST)
    public String handleAddUser(HttpServletRequest request, RedirectAttributes redirectAttributes) {

        String email = request.getParameter("email");

        if (DbqaUserDao.getUser(email) != null) {
            redirectAttributes.addFlashAttribute("error", "That email is already present");
            return "redirect:/user/add";
        } else {
            DbqaUser user = new DbqaUser();
            user.setEmail(request.getParameter("email"));

            if(request.getParameter("requireAuthentication") != null) {
                user.setRequiresAuthentication(true);
            } else {
                user.setRequiresAuthentication(false);
            }
            user.setSalt(BCrypt.gensalt(12));
            user.setHash(DbqaUtil.hashPassword(request.getParameter("password"), user.getSalt()));
            DbqaUserDao.insertUser(user);

            request.getSession().setAttribute("user", user);
            QueryController.removeQuerySessionData(request);
            redirectAttributes.addFlashAttribute("success", "Your account has been created");
            return "redirect:/query";
        }
    }
}
