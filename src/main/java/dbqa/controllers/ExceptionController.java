package dbqa.controllers;


import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import dbqa.dao.DatabaseDao;
import dbqa.model.Database;
import dbqa.util.DbqaUtil;
import org.hibernate.exception.SQLGrammarException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class ExceptionController {
	@SuppressWarnings("serial")
	private HashMap<Integer, String> oracleFriendlyErrors = new HashMap<Integer, String>(){{
		put(900, "Did you include the keyword SELECT?");
		put(901, "The CREATE command was not followed by valid input.");
		put(902, "The CREATE or ALTER TABLE command was not followed by a valid datatype.");
		put(903, "I couldn't find the table you requested. Are you sure you entered a valid name?");
		put(904, "I couldn't find the column name you requested. Are you sure you entered a valid name?");
		put(905, "A required keyword is missing.");
		put(906, "Uh oh, a required left parenthesis is missing. Please make sure all your parentheses are closed.");
		put(907, "Uh oh, a required right parenthesis is missing. Please make sure all your parentheses are closed.");
		put(908, "The keyword NULL was missing from the query.");
		put(909, "A function was passed an invalid number of arguments. Please correct the input.");
		put(910, "No size was specified for a character field or the character size was invalid.");
		put(911, "An invalid special character was placed.");
		put(913, "This statement requires two sets of values equal in number. Check the number of items in each set and change the statement to make them equal.");
		put(914, "The keyword ADD is resulting in invalid input. Specify the keyword ADD in the ALTER statement.");
		put(915, "Network access of dictionary table not currently allowed.");
		put(917, "A required comma has been omitted from a list of columns or values.");
		put(918, "A column name used in a join exists in more than one table and is thus referenced ambiguously.");
		put(919, "An entry was formatted like a function call, but it is not recognizable as an Oracle function.");
		put(920, "A search condition was entered with an invalid or missing relational operator.");
		put(921, "Part of a valid command was entered, but at least one major component was missing.");
		put(922, "An invalid option was specified in defining a column or storage clause.");
		put(923, "Did you include the keyword FROM?");
		put(924, "The keyword BY is missing from your statement.");
		put(925, "An INSERT statement has been entered without the keyword INTO.");
		put(926, "An INSERT statement has been entered without the keyword VALUES or SELECT.");
		put(927, "An equal sign (=) has been omitted from either a SET clause of an UPDATE statement or following a \"!\" character.");
		put(928, "A SELECT subquery must be included in a CREATE VIEW statement.");
		put(929, "Missing period.");
		put(930, "Missing asterisk.");
		put(934, "An aggregate function used in a condition must appear in the HAVING clause.");
		put(937, "If an aggregate function is one of multiple columns in a  SELECT clause, you must include all all columns in the SELECT clause that aren't used as input to an aggregate function in a GROUP BY clause. ");
		put(942, "I couldn't find the table you requested. Are you sure you entered a valid name?");
		put(979, "A GROUP BY clause must include all columns in the SELECT clause that aren't used as input to an aggregate function.");
	}};

	@RequestMapping(value = "/exception", method = RequestMethod.GET)
	public String processException(ModelMap modelMap, HttpServletRequest request, RedirectAttributes redirectAttributes) {
		Exception ex = (Exception) modelMap.get("exception");
		Database database = (Database)request.getSession().getAttribute("selectedDatabase");
		String query = (String) modelMap.get("query");

		if(ex != null) {
			if(ex instanceof SQLException) {
				processSQLException((SQLException)ex, database, query, redirectAttributes);
			} else {
				//check for malformed query
				try {
					DatabaseDao.getQueryResultSet(database, query);
				} catch(SQLException e) {
					processSQLException(e, database, query, redirectAttributes);
					return "redirect:/query";
				}
				//query seems okay, so report general error
				DbqaUtil.exceptionLogger.error(ex.getLocalizedMessage());
				return DbqaUtil.genericErrorRedirect(redirectAttributes);
			}
			return "redirect:/query";
		}
		return DbqaUtil.genericErrorRedirect(redirectAttributes);
	}

	private void processSQLException(SQLException e, Database database, String query, RedirectAttributes redirectAttributes) {
		String errorMessage = generateDatabaseErrorMessage(e, database);
		redirectAttributes.addFlashAttribute("error", errorMessage);
		redirectAttributes.addFlashAttribute("exceptionQuery", query);
	}

	private String generateDatabaseErrorMessage(SQLException e, Database database) {
		StringBuilder sb = new StringBuilder();
		String databaseTypeName = database.getDatabaseType().getTypeName().toUpperCase();
		if(e.getCause() instanceof SQLException) {
			e = (SQLException) e.getCause();
		}
		sb.append("Error ");
		sb.append(e.getErrorCode());
		sb.append(": ");
		if(databaseTypeName.contains("ORACLE")) {
			String friendlyMessage = oracleFriendlyErrors.get(e.getErrorCode());
			sb.append(friendlyMessage==null?e.getMessage():friendlyMessage);
		} else {
			sb.append(e.getMessage());
		}
		return sb.toString();
	}
}
