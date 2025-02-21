package dbqa.controllers;

import dbqa.dao.DatabaseDao;
import dbqa.dao.DbqaUserDao;
import dbqa.model.Database;
import dbqa.model.DatabaseType;
import dbqa.model.DbqaUser;
import dbqa.util.DbqaUtil;
import dbqa.util.JasyptUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class DatabaseController {
    @RequestMapping(value = "/database/add", method = RequestMethod.GET)
    public String getAddPage(ModelMap model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        List<DatabaseType> databaseTypes = DatabaseDao.getAllDatabaseTypes();
        request.setAttribute("databaseTypes", databaseTypes);
        return "add_database";
    }

    @RequestMapping(value = "/database/add", method = RequestMethod.POST)
    public String handleAddDatabase(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        DbqaUser user = DbqaUtil.getSessionUser(request);
        if(user == null) {
            return DbqaUtil.noAccessRedirect(redirectAttributes);
        }

        Integer databaseTypeId = Integer.parseInt(request.getParameter("databaseTypeId"));
        DatabaseType databaseType = DatabaseDao.getDatabaseType(databaseTypeId);
        String name = request.getParameter("name");
        String url = request.getParameter("url");
        String username = request.getParameter("username");
        String password = JasyptUtil.encrypt(request.getParameter("password"));

        Database database = DatabaseDao.insertOrRetrieveDatabase(new Database(databaseType, name, url, username, password));
        if(database != null) {
            if(!user.getDatabases().contains(database)) {
                user.addDatabase(database);
                DbqaUserDao.updateUser(user);
                redirectAttributes.addFlashAttribute("success", "Your database has been added");
            } else {
                redirectAttributes.addFlashAttribute("success", "You already have access to that database");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "An error occurred");
        }
        return "redirect:/query/" + user.getDbqaUserId();
    }

    @RequestMapping(value = "/database/{databaseId}", method = RequestMethod.GET)
    public String changeDatabase(@PathVariable("databaseId") int databaseId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        DbqaUser user = DbqaUtil.getSessionUser(request);
        if(user == null) {
            return DbqaUtil.noAccessRedirect(redirectAttributes);
        }
        if(databaseId == Database.DEFAULT_DATABASE_ID) {
            Database selectedDatabase = DatabaseDao.getDefaultDatabase();
            redirectAttributes.addFlashAttribute("selectedDatabase", selectedDatabase);
            DatabaseDao.addDatabaseSchemaToSession(selectedDatabase, request);
        } else {
            for (Database database : user.getDatabases()) {
                if (database.getDatabaseId().equals(databaseId)) {
                    request.getSession().setAttribute("selectedDatabase", database);
                    DatabaseDao.addDatabaseSchemaToSession(database, request);
                    break;
                }
            }
        }
        return "redirect:/query";
    }

    @RequestMapping(value = "/database/add_type", method = RequestMethod.GET)
    public String getAddDatabaseTypePage(ModelMap model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        DbqaUser user = DbqaUtil.getSessionUser(request);
        if(user == null) {
            return DbqaUtil.noAccessRedirect(redirectAttributes);
        }
        return "add_database_type";
    }

    @RequestMapping(value = "/database/add_type", method = RequestMethod.POST)
    public String handleAddDatabaseType(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        DbqaUser user = DbqaUtil.getSessionUser(request);
        if(user == null) {
            return DbqaUtil.noAccessRedirect(redirectAttributes);
        }

        String name = request.getParameter("name");
        String driver = request.getParameter("driver");
        String dialect = request.getParameter("dialect");
        String catalog = request.getParameter("catalog");
        String schema = request.getParameter("schema");
        boolean usernameAsSchema = request.getParameter("usernameAsSchema") != null;

        if(catalog == null || catalog.isEmpty()) {
            catalog = null;
        }
        if(schema == null || schema.isEmpty()) {
            schema = null;
        }

        DatabaseType databaseType = DatabaseDao.insertOrRetrieveDatabaseType(new DatabaseType(name, driver, dialect, catalog, schema, usernameAsSchema));
        if(databaseType != null) {
            redirectAttributes.addFlashAttribute("success", "Your database type has been added");
        } else {
            redirectAttributes.addFlashAttribute("error", "An error occurred");
        }
        return "redirect:/database/add";
    }
}
