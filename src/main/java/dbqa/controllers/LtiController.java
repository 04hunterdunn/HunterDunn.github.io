package dbqa.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dbqa.dao.LtiConsumerDao;
import dbqa.model.LtiConsumer;
import dbqa.model.LtiData;
import dbqa.util.DbqaUtil;
import dbqa.util.JasyptUtil;
import net.paulgray.lti.JsonUtils;
import net.paulgray.lti.contentitem.*;
import net.paulgray.lti.launch.LtiOauth10aSigner;
import oauth.signpost.exception.OAuthException;
import org.imsglobal.lti.launch.*;
import org.imsglobal.pox.IMSPOXRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.*;
import java.util.*;

@Controller
public class LtiController {

    public LtiController() {
        java.security.Security.addProvider(
                new org.bouncycastle.jce.provider.BouncyCastleProvider()
        );
    }

    @RequestMapping(value = "/lti/1.1/launch", method = RequestMethod.GET)
    public String verifyLti11LaunchRequest(ModelMap model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        String messageType = request.getParameter("lti_message_type");
        //selection request
        if("ContentItemSelectionRequest".equals(messageType)) {
            return processSelectionRequest(model, request, redirectAttributes);
        }
        //launch request
        else {
            return processLaunchRequest(request, redirectAttributes);
        }
    }

    @RequestMapping(value = "/lti/1.1/launch", method = RequestMethod.POST)
    public String verifyLti11LaunchRequest2(ModelMap model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        String messageType = request.getParameter("lti_message_type");
        //selection request
        if ("ContentItemSelectionRequest".equals(messageType)) {
            return processSelectionRequest(model, request, redirectAttributes);
        }
        //launch request
        else {
            return processLaunchRequest(request, redirectAttributes);
        }
    }

    @ResponseStatus(value = HttpStatus.OK)
    @RequestMapping(value = "/lti/1.1/result/send", method = RequestMethod.POST)
    public void sendScore(@RequestParam(value = "consumerKey") String consumerKey, @RequestParam(value = "serviceUrl") String serviceUrl, @RequestParam(value = "sourcedid") String sourcedid, @RequestParam(value = "score") String score) {
        LtiConsumer ltiConsumer = LtiConsumerDao.getLtiConsumerByKey(consumerKey);
        if(ltiConsumer != null) {
            String secret = JasyptUtil.decrypt(ltiConsumer.getSecret());
            try {
                IMSPOXRequest.sendReplaceResult(serviceUrl, consumerKey, secret, sourcedid, score);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (OAuthException e) {
                e.printStackTrace();
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }
        }
    }

    @RequestMapping(value = "/lti/1.1/select/send", method = RequestMethod.POST)
    public String sendSelectedItem(ModelMap model, @RequestParam(value = "lti_consumer_key") String consumerKey, @RequestParam(value = "content_item_return_url") String url, @RequestParam(value = "lti_data") String data, @RequestParam(value = "queryType") String queryType) throws JsonProcessingException, net.paulgray.lti.launch.LtiSigningException {
        LtiConsumer ltiConsumer = LtiConsumerDao.getLtiConsumerByKey(consumerKey);
        if(ltiConsumer != null) {
            String secret = JasyptUtil.decrypt(ltiConsumer.getSecret());
            ContentItemSelection contentItemSelection = generateContentItemSelection(ltiConsumer.getDbqaUser().getDbqaUserId(), queryType);
            ObjectMapper mapper = JsonUtils.getMapper();
            String selectionParam = mapper.writeValueAsString(contentItemSelection);
            //selectionParam = selectionParam.replace("\"@type\"", "\"@id\":\"id\",\"@type\"");
            System.out.println(selectionParam);
            Map<String, String> parameters = new HashMap();
            parameters.put("lti_message_type", "ContentItemSelection");
            parameters.put("content_items", selectionParam);
            LtiOauth10aSigner signer = new LtiOauth10aSigner();
            Map<String, String> signedParams = signer.signParameters(parameters.entrySet(), consumerKey, secret, url, "POST");
            model.addAttribute("signedParams", signedParams);
            model.addAttribute("launchUrl", url);
        }
        return "submit_query_type";
    }

    private String processSelectionRequest(ModelMap model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        LtiVerifier ltiVerifier = new LtiOauthVerifier();
        String key = request.getParameter("oauth_consumer_key");
        LtiConsumer ltiConsumer = LtiConsumerDao.getLtiConsumerByKey(key);
        if (ltiConsumer != null) {
            String secret = JasyptUtil.decrypt(ltiConsumer.getSecret());
            try {
                LtiVerificationResult ltiResult = ltiVerifier.verify(request, secret);
                if (ltiResult.getSuccess()) {
                    String url = request.getParameter("content_item_return_url");
                    String data = request.getParameter("data");
                    model.addAttribute("lti_consumer_key", key);
                    model.addAttribute("lti_content_item_return_url", url);
                    model.addAttribute("lti_data", data);
                    return "select_query_type";
                } else {
                    return DbqaUtil.noAccessRedirect(redirectAttributes);
                }
            } catch (LtiVerificationException e) {
                e.printStackTrace();
            }
        }
        return "redirect:/login";
    }

    private String processLaunchRequest(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        LtiVerifier ltiVerifier = new LtiOauthVerifier();
        String key = request.getParameter("oauth_consumer_key");
        LtiConsumer ltiConsumer = LtiConsumerDao.getLtiConsumerByKey(key);
        if (ltiConsumer != null) {
            String secret = JasyptUtil.decrypt(ltiConsumer.getSecret());
            try {
                LtiVerificationResult ltiResult = ltiVerifier.verify(request, secret);
                if (ltiResult.getSuccess()) {
                    int userId = ltiConsumer.getDbqaUser().getDbqaUserId();
                    String queryType = request.getParameter("queryType");
                    consumeLtiData(key, request);
                    /*
                    String canvasUserId = request.getParameter("user_id");
                    String url = request.getParameter("lis_outcome_service_url");
                    String sourcedid = request.getParameter("lis_result_sourcedid");
                    String displayStepExplanation = request.getParameter("ext_display_step_explanation");
                    LtiData ltiData = new LtiData(key, url, sourcedid, canvasUserId);
                    request.getSession().setAttribute("ltiData", ltiData);
                    if(displayStepExplanation != null && displayStepExplanation.equalsIgnoreCase("true")) {
                        request.getSession().setAttribute("displayStepExplanation", true);
                    } else {
                        request.getSession().setAttribute("displayStepExplanation", false);
                    }
                     */
                    return "redirect:/user/" + userId + "/query/" + queryType;
                } else {
                    return DbqaUtil.noAccessRedirect(redirectAttributes);
                }
            } catch (LtiVerificationException e) {
                e.printStackTrace();
            }
        }
        return "redirect:/login";
    }

    private ContentItemSelection generateContentItemSelection(int userId, String queryType) {
        String url = "https://codesmell.org/dbqa/user/"+userId+"/query/"+queryType;
        ContentItemSelection contentItemSelection = ContentItemSelection.builder()
                .addGraph(
                        LtiLinkItem.builder()
                                .title("DBQA")
                                .text("Example query type - " + queryType)
                                .url(url)
                                .build())
                .build();
        return contentItemSelection;
    }

    public static void consumeLtiData(String key, HttpServletRequest request) {
        String canvasUserId = request.getParameter("user_id");
        String url = request.getParameter("lis_outcome_service_url");
        String sourcedid = request.getParameter("lis_result_sourcedid");
        String displayStepExplanation = request.getParameter("ext_display_step_explanation");
        LtiData ltiData = new LtiData(key, url, sourcedid, canvasUserId);
        request.getSession().setAttribute("ltiData", ltiData);
        if(displayStepExplanation != null && displayStepExplanation.equalsIgnoreCase("true")) {
            request.getSession().setAttribute("displayStepExplanation", true);
        } else {
            request.getSession().setAttribute("displayStepExplanation", false);
        }
    }

    /*
    @RequestMapping(value = "/lti/1.3/launch", method = RequestMethod.POST)
    public String verifyLti13LaunchRequest(HttpServletRequest request) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        //old platform resource link: tool link url: https://lti-ri.imsglobal.org/lti/tools/1081/launches
        KeyFactory kf = KeyFactory.getInstance("RSA");

        //public key from platform
        String publicKeyPEM = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA4SvJTH8m0rqKVmgB+S7DzXjDs0gRo+WSU+1WGS9sGMCZ11O1o5MKZJA8PaC9bSxfXV0rQtlhIrEz5BqQ80LJPYJ3GWfOeaxyH/tfTTYwB9nN3HN8ytK5Rz0mRz1R4XjriFG9trguagTmlbGQi3apOzQ+FyHyorBkY7Mcf+7b04ubTIPozbL1luw7zbxw4SGi5//BAaLp1iBT9uByNqUG+TLVlSQijF10v8kyMdDo5NZm/mJUtNYhwghWgPFl63cYoP7a/6c0T8oQHLKvNcrk701iniO1sD/OZ+gjzy4OX0ibajydcwzdloe6fufQCBv+qAMnRkqwT0cTBHehM91mtwIDAQAB";
        byte[] encodedPublicKey = Base64.getDecoder().decode(publicKeyPEM);
        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(encodedPublicKey);
        RSAPublicKey publicKey = (RSAPublicKey)kf.generatePublic(pubSpec);

        //sample JWT sent from platform with public key above
        String id_token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IjZnbkZsQmZTN1p3cGhRUDVQeS1IWVJ3TTR3REdQbHphTnphZHdPRU5ubVEifQ.eyJodHRwczovL3B1cmwuaW1zZ2xvYmFsLm9yZy9zcGVjL2x0aS9jbGFpbS9tZXNzYWdlX3R5cGUiOiJMdGlSZXNvdXJjZUxpbmtSZXF1ZXN0IiwiZ2l2ZW5fbmFtZSI6IlJhcGhhZWwiLCJmYW1pbHlfbmFtZSI6Ik1EIiwibWlkZGxlX25hbWUiOiJBbHRlbndlcnRoIiwicGljdHVyZSI6Imh0dHA6Ly9leGFtcGxlLm9yZy9SYXBoYWVsLmpwZyIsImVtYWlsIjoiUmFwaGFlbC5NREBleGFtcGxlLm9yZyIsIm5hbWUiOiJSYXBoYWVsIEFsdGVud2VydGggQmVkbmFyIE1EIiwiaHR0cHM6Ly9wdXJsLmltc2dsb2JhbC5vcmcvc3BlYy9sdGkvY2xhaW0vcm9sZXMiOlsiaHR0cDovL3B1cmwuaW1zZ2xvYmFsLm9yZy92b2NhYi9saXMvdjIvbWVtYmVyc2hpcCNMZWFybmVyIiwiaHR0cDovL3B1cmwuaW1zZ2xvYmFsLm9yZy92b2NhYi9saXMvdjIvaW5zdGl0dXRpb24vcGVyc29uI1N0dWRlbnQiLCJodHRwOi8vcHVybC5pbXNnbG9iYWwub3JnL3ZvY2FiL2xpcy92Mi9tZW1iZXJzaGlwI01lbnRvciJdLCJodHRwczovL3B1cmwuaW1zZ2xvYmFsLm9yZy9zcGVjL2x0aS9jbGFpbS9yb2xlX3Njb3BlX21lbnRvciI6WyJhNjJjNTJjMDJiYTI2MjAwM2Y1ZSJdLCJodHRwczovL3B1cmwuaW1zZ2xvYmFsLm9yZy9zcGVjL2x0aS9jbGFpbS9yZXNvdXJjZV9saW5rIjp7ImlkIjoiMTg5ODEiLCJ0aXRsZSI6IkRhdGFiYXNlciBRdWVyeSBBbmFseXplciIsImRlc2NyaXB0aW9uIjoiQSBkYXRhLW9yaWVudGVkIHZpc3VhbGl6YXRpb24gb2YgU1FMIFNFTEVDVCBzdGF0ZW1lbnQgZXhlY3V0aW9uIn0sImh0dHBzOi8vcHVybC5pbXNnbG9iYWwub3JnL3NwZWMvbHRpL2NsYWltL2NvbnRleHQiOnsiaWQiOiI5NzI5IiwibGFiZWwiOiJkYnFhLWNvbnRleHQiLCJ0aXRsZSI6IkRhdGFiYXNlIFF1ZXJ5IEFuYWx5emVyIiwidHlwZSI6WyJzcWwtbG1zIl19LCJodHRwczovL3B1cmwuaW1zZ2xvYmFsLm9yZy9zcGVjL2x0aS9jbGFpbS90b29sX3BsYXRmb3JtIjp7Im5hbWUiOiJkYnFhLXBsYXRmb3JtIiwiY29udGFjdF9lbWFpbCI6IiIsImRlc2NyaXB0aW9uIjoiIiwidXJsIjoiIiwicHJvZHVjdF9mYW1pbHlfY29kZSI6IiIsInZlcnNpb24iOiIxLjAiLCJndWlkIjoxMTQyfSwiaHR0cHM6Ly9wdXJsLmltc2dsb2JhbC5vcmcvc3BlYy9sdGktYWdzL2NsYWltL2VuZHBvaW50Ijp7InNjb3BlIjpbImh0dHBzOi8vcHVybC5pbXNnbG9iYWwub3JnL3NwZWMvbHRpLWFncy9zY29wZS9saW5laXRlbSIsImh0dHBzOi8vcHVybC5pbXNnbG9iYWwub3JnL3NwZWMvbHRpLWFncy9zY29wZS9yZXN1bHQucmVhZG9ubHkiLCJodHRwczovL3B1cmwuaW1zZ2xvYmFsLm9yZy9zcGVjL2x0aS1hZ3Mvc2NvcGUvc2NvcmUiXSwibGluZWl0ZW1zIjoiaHR0cHM6Ly9sdGktcmkuaW1zZ2xvYmFsLm9yZy9wbGF0Zm9ybXMvMTE0Mi9jb250ZXh0cy85NzI5L2xpbmVfaXRlbXMiLCJsaW5laXRlbSI6Imh0dHBzOi8vbHRpLXJpLmltc2dsb2JhbC5vcmcvcGxhdGZvcm1zLzExNDIvY29udGV4dHMvOTcyOS9saW5lX2l0ZW1zLzUwMDUifSwiaHR0cHM6Ly9wdXJsLmltc2dsb2JhbC5vcmcvc3BlYy9sdGktbnJwcy9jbGFpbS9uYW1lc3JvbGVzZXJ2aWNlIjp7ImNvbnRleHRfbWVtYmVyc2hpcHNfdXJsIjoiaHR0cHM6Ly9sdGktcmkuaW1zZ2xvYmFsLm9yZy9wbGF0Zm9ybXMvMTE0Mi9jb250ZXh0cy85NzI5L21lbWJlcnNoaXBzIiwic2VydmljZV92ZXJzaW9ucyI6WyIyLjAiXX0sImh0dHBzOi8vcHVybC5pbXNnbG9iYWwub3JnL3NwZWMvbHRpLWNlcy9jbGFpbS9jYWxpcGVyLWVuZHBvaW50LXNlcnZpY2UiOnsic2NvcGVzIjpbImh0dHBzOi8vcHVybC5pbXNnbG9iYWwub3JnL3NwZWMvbHRpLWNlcy92MXAwL3Njb3BlL3NlbmQiXSwiY2FsaXBlcl9lbmRwb2ludF91cmwiOiJodHRwczovL2x0aS1yaS5pbXNnbG9iYWwub3JnL3BsYXRmb3Jtcy8xMTQyL3NlbnNvcnMiLCJjYWxpcGVyX2ZlZGVyYXRlZF9zZXNzaW9uX2lkIjoidXJuOnV1aWQ6MjdiODEyYWFiNDkxYzAxYzA1MDgifSwiaXNzIjoiaHR0cHM6Ly9sdGktcmkuaW1zZ2xvYmFsLm9yZyIsImF1ZCI6IjI0N2ExOGU4OWUwNmE2ZjlmODc1MjhlMGU4NGYxYzc0IiwiaWF0IjoxNTk0MTM3MjU1LCJleHAiOjE1OTQxMzc1NTUsInN1YiI6IjkwNWUyOTUxN2ZjYjUzMzVmZDE3Iiwibm9uY2UiOiI5Zjg4YjZiOGE4MTMzZmFhMmYyZCIsImh0dHBzOi8vcHVybC5pbXNnbG9iYWwub3JnL3NwZWMvbHRpL2NsYWltL3ZlcnNpb24iOiIxLjMuMCIsImxvY2FsZSI6ImVuLVVTIiwiaHR0cHM6Ly9wdXJsLmltc2dsb2JhbC5vcmcvc3BlYy9sdGkvY2xhaW0vbGF1bmNoX3ByZXNlbnRhdGlvbiI6eyJkb2N1bWVudF90YXJnZXQiOiJpZnJhbWUiLCJoZWlnaHQiOjMyMCwid2lkdGgiOjI0MCwicmV0dXJuX3VybCI6Imh0dHBzOi8vbHRpLXJpLmltc2dsb2JhbC5vcmcvcGxhdGZvcm1zLzExNDIvcmV0dXJucyJ9LCJodHRwczovL3d3dy5leGFtcGxlLmNvbS9leHRlbnNpb24iOnsiY29sb3IiOiJ2aW9sZXQifSwiaHR0cHM6Ly9wdXJsLmltc2dsb2JhbC5vcmcvc3BlYy9sdGkvY2xhaW0vY3VzdG9tIjp7Im15Q3VzdG9tVmFsdWUiOiIxMjMifSwiaHR0cHM6Ly9wdXJsLmltc2dsb2JhbC5vcmcvc3BlYy9sdGkvY2xhaW0vZGVwbG95bWVudF9pZCI6IjEiLCJodHRwczovL3B1cmwuaW1zZ2xvYmFsLm9yZy9zcGVjL2x0aS9jbGFpbS90YXJnZXRfbGlua191cmkiOiJodHRwczovL2NvZGVzbWVsbC5vcmcvZGJxYS9sdGkvbGF1bmNoIn0.2IJ6E-wgAA0FbLj6c82Me7VPWqoQD6bWQZGRuBW5HZg6degDaLMB4L4MhRYbmajo7v63q_RhReHBwOrBzl3PzE0foNbzqPF-AbpIdcvRvwUDfDTXwBw5BKMVQtfdPwVwUGZbiAUfC1aAL_n99SWR5_6yOl67jqCD4yZbTLL4fvRb8DpgJzZuH4nbCVrP80THhLI7S8YfBwNiBICQvhk9OeBRld7XBjnkpFKnpjvbByUiNziVKQknA7Q1JgBskEduZSBeJ1nm3kzR15Kqw2ZLje2CCW6iH74yYA5ulD_s5Fu1IGw72GfP0nWqDRHK1xZMQDK4R8X3FVMqXfP1Po_uBQ";
        Jws<Claims> jws;

        try {
            jws = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(id_token);
            // we can safely trust the JWT
            System.out.println(jws);
        } catch (JwtException ex) {
            ex.printStackTrace();
        }

        return "redirect:/login";
    }
     */
}
