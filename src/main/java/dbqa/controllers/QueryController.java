package dbqa.controllers;

import dbqa.analyzer.QueryAnalyzer;
import dbqa.dao.DatabaseDao;
import dbqa.dao.QueryExampleDao;
import dbqa.model.*;
import dbqa.util.DbqaUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;

@Controller
public class QueryController {

    @RequestMapping(value = "/query/{queryType}", method = RequestMethod.GET)
    public String loadPageForUserWithQuery(ModelMap model, HttpServletRequest request, RedirectAttributes redirectAttributes, @PathVariable String queryType) {
        DbqaUser user = DbqaUtil.getSessionUser(request);
        if(user == null) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to view that page");
            return "redirect:/login";
        }
        //user is identified in session
        ensureDatabaseRegistration(request, redirectAttributes);
        QueryExample queryExample = QueryExampleDao.getQueryExampleByType(queryType);
        String query = queryExample.getQuery();
        model.addAttribute("originalQuery", query);
        return executeQuery(model, request, redirectAttributes, query, null, null);
    }

    @RequestMapping(value = "/query", method = RequestMethod.GET)
    public String loadPage(ModelMap model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        DbqaUser user = DbqaUtil.getSessionUser(request);
        if(user == null) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to view that page");
            return "redirect:/login";
        }
        ensureDatabaseRegistration(request, redirectAttributes);
        return "query";
    }

    @RequestMapping(value = "/query", method = RequestMethod.POST)
    public String submitQuery(ModelMap model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        DbqaUser user = DbqaUtil.getSessionUser(request);
        if(user == null) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to view that page");
            return "redirect:/login";
        }
        ensureDatabaseRegistration(request, redirectAttributes);

        String query = request.getParameter("query");
        String highlightPrevious = request.getParameter("highlightPrevious");
        String limit = request.getParameter("limit");
        return executeQuery(model, request, redirectAttributes, query, highlightPrevious, limit);
    }

    @ResponseStatus(value = HttpStatus.OK)
    @RequestMapping(value = "/query/log", method = RequestMethod.POST)
    public void logInteraction(@RequestParam(value = "canvasUserId", required = false) String canvasUserId, @RequestParam(value = "sourcedid", required = false) String sourcedid, @RequestParam String query, @RequestParam String currentStep, @RequestParam String totalSteps, @RequestParam String interactionType, @RequestParam(value = "displayExplanation", required = false) String displayExplanation, HttpServletRequest request) {
        String user = canvasUserId;
        if(canvasUserId == null || canvasUserId.isEmpty()) {
            DbqaUser sessionUser = DbqaUtil.getSessionUser(request);
            user = sessionUser.getEmail();
        }
        boolean stepExplanationsDisplayed = displayExplanation != null && displayExplanation.equalsIgnoreCase("true");
        DbqaUtil.queryLogger.info("User: " + user + ((sourcedid != null && !sourcedid.isEmpty())?(" with sourcedid: " + sourcedid):"") + " clicked " + interactionType + " to see step " + currentStep + " out of " + totalSteps + ((stepExplanationsDisplayed)?" with":" without") + " step explanation for query: " + query);
    }

    private void ensureDatabaseRegistration(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Database defaultDatabase;
        if(request.getSession().getAttribute("defaultDatabase") == null) {
            defaultDatabase = DatabaseDao.getDefaultDatabase();
            request.getSession().setAttribute("defaultDatabase", defaultDatabase);
        }
        if(request.getSession().getAttribute("selectedDatabase") == null) {
            defaultDatabase = (Database) request.getSession().getAttribute("defaultDatabase");
            request.getSession().setAttribute("selectedDatabase", defaultDatabase);
            DatabaseDao.addDatabaseSchemaToSession(defaultDatabase, request);
        }
    }

    private String executeQuery(ModelMap model, HttpServletRequest request, RedirectAttributes redirectAttributes, String query, String highlightPrevious, String limit) {
        Database database = (Database)request.getSession().getAttribute("selectedDatabase");
        QueryAnalyzer qa = new QueryAnalyzer(database, request);
        ArrayList<ParserResult> parserResults = new ArrayList();
        DbqaUser sessionUser = DbqaUtil.getSessionUser(request);
        String user = sessionUser.getEmail();
        try {
            LtiData ltiData = (LtiData)request.getSession().getAttribute("ltiData");
            String sourcedid = "";
            if(ltiData != null) {
                model.addAttribute("lti_consumer_key", ltiData.getOauth_consumer_key());
                model.addAttribute("lis_outcome_service_url", ltiData.getLis_outcome_service_url());
                sourcedid = ltiData.getLis_result_sourcedid();
                model.addAttribute("lis_result_sourcedid", sourcedid);
                user = ltiData.getCanvasUserId();
                model.addAttribute("canvasUserId", user);
            }
            Object stepExplanationsDisplayed = request.getSession().getAttribute("displayStepExplanation");
            DbqaUtil.queryLogger.info("User: " + user + ((sourcedid != null && !sourcedid.isEmpty())?(" with sourcedid: " + sourcedid):"") + ((stepExplanationsDisplayed != null && stepExplanationsDisplayed.equals(true))?" with":" without") + " step explanation submitted query: " + query);
            parserResults = qa.parseQuery(query, parserResults);
            if(parserResults != null) {
                model.addAttribute("parserResults", parserResults);
                model.addAttribute("currentParserResultIndex", 0);
                model.addAttribute("originalQuery", query);
                if(highlightPrevious != null) {
                    model.addAttribute("highlightPrevious", true);
                }
                model.addAttribute("limit", limit);
            } else {
                DbqaUtil.genericErrorRedirect(redirectAttributes);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("exception", e);
            redirectAttributes.addFlashAttribute("query", query);
            return "redirect:/exception";
        }
        if(request.getParameter("checked") != null) {
            redirectAttributes.addFlashAttribute("showPreviousSteps", true);
        }
        return "query";
    }

    public static void removeQuerySessionData(HttpServletRequest request) {
        request.getSession().removeAttribute("selectedDatabase");
        request.getSession().removeAttribute("schema");
        request.getSession().removeAttribute("tableData");
    }
}
