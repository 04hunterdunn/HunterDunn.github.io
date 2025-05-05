package dbqa.analyzer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import dbqa.dao.DatabaseDao;
import dbqa.model.Database;
import org.springframework.util.StringUtils;

import dbqa.model.ParserResult;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;

public class QueryAnalyzer {

	private Database database;
	private HttpServletRequest request;
	private int queryNestingLevel = 0;
	//consider replacing with list of operators associated with database type (would also need to update dbqa.js)
	private String[] setOperators = {"UNION ALL", "UNION", "EXCEPT", "INTERSECT"};

	public QueryAnalyzer(Database database) {
		this.database = database;
	}

	public QueryAnalyzer(Database database, HttpServletRequest request) {
		this.database = database;
		this.request = request;
	}

	public Database getDatabase() {
		return this.database;
	}


	//added this method as to not repeat code
	public ArrayList<ParserResult> parseQueryWithSetOperators(int startingIndex, String query, String outerQuery, ArrayList<ParserResult> listOfResults) throws ClassNotFoundException, SQLException{
		ArrayList<String> setOperatorQueries = handleSetOperators(query);
		if(setOperatorQueries == null) {
			throw new SQLException("Set operands do not produce the same number and/or types of columns.");
		}

		String setQuery;
		String setOperator;
		int queryIndex = 0;
		int leftQueryIndex = 0;
		String generatedQuery;
		for(int i=0; i<setOperatorQueries.size(); i++) {
			//process set operator
			if (i % 3 == 2) {
				setOperator = setOperatorQueries.get(i);
				queryIndex = startingIndex + query.indexOf(setOperator, leftQueryIndex);
				generatedQuery = setOperatorQueries.get(i - 2) + setOperator + "\r\n" + setOperatorQueries.get(i - 1);
				createAndAddParserResult(queryIndex, setOperator, generatedQuery, query, listOfResults);
			}
			//process query
			else {
				setQuery = setOperatorQueries.get(i);
				queryIndex = query.indexOf(setQuery, queryIndex);
				//save the index of the query before the set operator so know where to start looking for the set operator when Plighting
				if (i % 3 == 0) {
					leftQueryIndex = queryIndex;
					if (queryNestingLevel > 0) {
						queryNestingLevel--;
					}
				} else {
					queryNestingLevel++;
				}
				if (outerQuery == null) {
					parseQuery(queryIndex, setQuery, query, listOfResults);
				} else {
					parseQuery(queryIndex + outerQuery.length(), setQuery, outerQuery, listOfResults);
				}
				queryIndex += setQuery.length();
			}

		}
		return listOfResults;
	}
	
	public ArrayList<ParserResult> parseQuery(String query, ArrayList<ParserResult> listOfResults) throws ClassNotFoundException, SQLException{
		String outerQuery = query;
		String nestedQuery = getNestedSubQuery(query, 0);
		if(nestedQuery != null) {
			int startInnerQueryIndex = indexOfClause(query, nestedQuery);
			outerQuery = query.substring(0, startInnerQueryIndex);
		}
		if(containsSetOperation(outerQuery)) {
			parseQueryWithSetOperators(0, query, null, listOfResults);
			return listOfResults;
		} else {
			return parseQuery(0, query, null, listOfResults);
		}
	}
	
	public ArrayList<ParserResult> parseQuery(int startingIndex, String query, String outerQuery, ArrayList<ParserResult> listOfResults) throws SQLException, ClassNotFoundException {
		ParserResult lastParserResult = null;
		String subquery = null;
		int subIndex = 0;
		
		// process FROM clause
		String fromClause = findFromClause(query);
		int clauseIndex = startingIndex + indexOfClause(query, "FROM");
		while((subquery = getNestedSubQuery(fromClause, subIndex)) != null) {
			queryNestingLevel++;
			subIndex = fromClause.indexOf(subquery, subIndex);
			int startInnerQueryIndex = indexOfClause(query, subquery);
			outerQuery = query.substring(0, startInnerQueryIndex);
			int outerQueryLength = outerQuery.length();
			if(containsSetOperation(subquery)) {
				parseQueryWithSetOperators(outerQueryLength, subquery, outerQuery, listOfResults);
			} else {
				parseQuery(clauseIndex + subIndex, subquery, query, listOfResults);
			}
			subIndex += subquery.length();
		}
		processFromClause(clauseIndex, fromClause, query, listOfResults);

		//process WHERE clause
		String whereClause = findWhereClause(query);
		if (!whereClause.isEmpty()) {
			lastParserResult = listOfResults.get(listOfResults.size()-1);
			subIndex = 0;
			clauseIndex = startingIndex + indexOfClause(query ,"WHERE");
			while((subquery = getNestedSubQuery(whereClause, subIndex)) != null) {
				queryNestingLevel++;
				subIndex = whereClause.indexOf(subquery, subIndex);
				int startInnerQueryIndex = indexOfClause(query, subquery);
				outerQuery = query.substring(0, startInnerQueryIndex);
				int outerQueryLength = outerQuery.length();
				if(containsSetOperation(subquery)) {
					parseQueryWithSetOperators(outerQueryLength, subquery, outerQuery, listOfResults);
				} else {
					parseQuery(clauseIndex + subIndex, subquery, query, listOfResults);
				}
				subIndex += subquery.length();
			}
			processWhereClause(clauseIndex, whereClause, query, outerQuery, listOfResults, null, lastParserResult.getGeneratedQuery());
		}

		// process SELECT clause
		String selectClause = findSelectClause(query);
		lastParserResult = listOfResults.get(listOfResults.size()-1);
		subIndex = 0;
		clauseIndex = startingIndex + indexOfClause(query, "SELECT");
		while((subquery = getNestedSubQuery(selectClause, subIndex)) != null) {
			queryNestingLevel++;
			subIndex = selectClause.indexOf(subquery, subIndex);
			parseQuery(clauseIndex + subIndex, subquery, query, listOfResults);
			subIndex += subquery.length();
		}
		
		String groupByClause = findGroupByClause(query);
		String originalSelectClause = selectClause;
		if (!groupByClause.isEmpty()) {
			selectClause = stripGroupByStatements(selectClause);
		}
		processSelectClause(clauseIndex, selectClause, query, listOfResults, lastParserResult);
		
		// process GROUP BY clause
		if(!groupByClause.isEmpty()) {
			clauseIndex = startingIndex + indexOfClause(query, "GROUP\\s+BY");
			processGroupByClause(clauseIndex, groupByClause, originalSelectClause, fromClause, whereClause, query, listOfResults, lastParserResult);
		}

		// process HAVING clause
		String havingClause = findHavingClause(query);
		if (!havingClause.isEmpty()) {
			lastParserResult = listOfResults.get(listOfResults.size()-1);
			subIndex = 0;
			clauseIndex = startingIndex + indexOfClause(query, "HAVING");
			while((subquery = getNestedSubQuery(havingClause, subIndex)) != null) {
				queryNestingLevel++;
				subIndex = havingClause.indexOf(subquery, subIndex);
				parseQuery(clauseIndex + subIndex, subquery, query, listOfResults);
				subIndex += subquery.length();
			}
			processHavingClause(clauseIndex, havingClause, listOfResults, null, lastParserResult.getGeneratedQuery(), query);
		}
		
		// process ORDER BY clause
		String orderByClause = findOrderByClause(query);
		if (!orderByClause.isEmpty()) {
			subIndex = 0;
			clauseIndex = startingIndex + indexOfClause(query, "ORDER\\s+BY");
			while((subquery = getNestedSubQuery(orderByClause, subIndex)) != null) {
				queryNestingLevel++;
				subIndex = orderByClause.indexOf(subquery, subIndex);
				parseQuery(clauseIndex + subIndex, subquery, query, listOfResults);
				subIndex += subquery.length();
			}
			processOrderByClause(clauseIndex, orderByClause, originalSelectClause, fromClause, whereClause, groupByClause, havingClause, query, listOfResults, lastParserResult);
		}
		
		if (queryNestingLevel > 0) {
			queryNestingLevel--;
		}

		return listOfResults;
	}
	
	public String findOrderByClause(String query) {
				
		int orderByIndex = indexOfClause(query, "ORDER\\s+BY");
		if (orderByIndex >= 0) {
			String clause = query.substring(orderByIndex).trim();
			if (clause.endsWith(";")) {
				return clause.substring(0, clause.length()-1);
			}
			else {
				return clause;
			}
		}
		else {
			return "";
		}
	}
	
	public String findHavingClause(String query) {
		int havingIndex = indexOfClause(query, "HAVING");
		
		if (havingIndex >= 0) {
			int orderByIndex = indexOfClause(query, "ORDER\\s+BY");
			if (orderByIndex >= 0) {
				return query.substring(havingIndex, orderByIndex).trim();
			}
			
			String clause = query.substring(havingIndex).trim();
			if (clause.endsWith(";")) {
				return clause.substring(0,  clause.length()-1);
			}
			else {
				return clause;
			}
		}
		else {
			return "";
		}
	}
	
	public boolean containsGroupingFunctions(String selectClause) {
		Pattern min = Pattern.compile("(?i:MIN\\()");
		Matcher minMatcher = min.matcher(selectClause);

		Pattern max = Pattern.compile("(?i:MAX\\()");
		Matcher maxMatcher = max.matcher(selectClause);

		Pattern avg = Pattern.compile("(?i:AVG\\()");
		Matcher avgMatcher = avg.matcher(selectClause);
		
		Pattern sum = Pattern.compile("(?i:SUM\\()");
		Matcher sumMatcher = sum.matcher(selectClause);

		Pattern count = Pattern.compile("(?i:COUNT\\()");
		Matcher countMatcher = count.matcher(selectClause);
		
		return minMatcher.find() || maxMatcher.find() || avgMatcher.find() || sumMatcher.find() || countMatcher.find();
	}

	public String stripGroupByStatements(String query) {
		Pattern min = Pattern.compile("(?i:MIN\\()");
		Matcher minMatcher = min.matcher(query);
		query = strip(query, minMatcher);

		Pattern max = Pattern.compile("(?i:MAX\\()");
		Matcher maxMatcher = max.matcher(query);
		query = strip(query, maxMatcher);

		Pattern avg = Pattern.compile("(?i:AVG\\()");
		Matcher avgMatcher = avg.matcher(query);
		query = strip(query, avgMatcher);

		Pattern sum = Pattern.compile("(?i:SUM\\()");
		Matcher sumMatcher = sum.matcher(query);
		query = strip(query, sumMatcher);

		Pattern count = Pattern.compile("(?i:COUNT\\()");
		Matcher countMatcher = count.matcher(query);
		query = strip(query, countMatcher);
		
		return query;
	}

	public String strip(String query, Matcher match) {
		while (match.find()) {
			query = query.substring(0, match.end()) + query.substring(match.end()).replaceFirst("\\)", "");
			query = query.replace(query.substring(match.start(), match.end()), "");
		}
		return query;
	}

	/*
	 * Return the WHERE clause that's not inside a subquery
	 */
	public String findWhereClause(String query) {
		int whereIndex = indexOfClause(query, "WHERE");
		
		if (whereIndex >= 0) {
			int groupByIndex = indexOfClause(query, "GROUP\\s+BY");
			if (groupByIndex >= 0) {
				return query.substring(whereIndex, groupByIndex).trim();
			}
			
			int havingIndex = indexOfClause(query, "HAVING");
			if (havingIndex >= 0) {
				return query.substring(whereIndex, havingIndex).trim();
			}
			
			int orderByIndex = indexOfClause(query, "ORDER\\s+BY");
			if (orderByIndex >= 0) {
				return query.substring(whereIndex, orderByIndex).trim();
			}

			int unionIndex = indexOfClause(query, "UNION");
			if(unionIndex >= 0) {
				return query.substring(whereIndex, unionIndex).trim();
			}

			int unionAllIndex = indexOfClause(query, "UNION\\s+ALL");
			if(unionAllIndex >= 0) {
				return query.substring(whereIndex, unionAllIndex).trim();
			}

			int exceptIndex = indexOfClause(query, "EXCEPT");
			if(exceptIndex >= 0) {
				return query.substring(whereIndex, exceptIndex).trim();
			}

			int intersectIndex = indexOfClause(query, "INTERSECT");
			if(intersectIndex >= 0) {
				return query.substring(whereIndex, intersectIndex).trim();
			}
			
			String clause = query.substring(whereIndex).trim();
			if (clause.endsWith(";")) {
				return clause.substring(0,  clause.length()-1);
			}
			else {
				return clause;
			}
		}
		else {
			return "";
		}
	}

	/*
	 * Return the FROM clause that's not inside a subquery
	 */
	public String findFromClause(String query) {
		int fromIndex = indexOfClause(query, "FROM");
		
		int whereIndex = indexOfClause(query, "WHERE");
		if (whereIndex >= 0) {
			return query.substring(fromIndex, whereIndex).trim();
		}
		
		int groupByIndex = indexOfClause(query, "GROUP\\s+BY");
		if (groupByIndex >= 0) {
			return query.substring(fromIndex, groupByIndex).trim();
		}
		
		int havingIndex = indexOfClause(query, "HAVING");
		if (havingIndex >= 0) {
			return query.substring(fromIndex, havingIndex).trim();
		}
		
		int orderByIndex = indexOfClause(query, "ORDER\\s+BY");
		if (orderByIndex >= 0) {
			return query.substring(fromIndex, orderByIndex).trim();
		}

		int unionIndex = indexOfClause(query, "UNION");
		if(unionIndex >= 0) {
			return query.substring(fromIndex, unionIndex).trim();
		}

		int unionAllIndex = indexOfClause(query, "UNION\\s+ALL");
		if(unionAllIndex >= 0) {
			return query.substring(fromIndex, unionAllIndex).trim();
		}

		int exceptIndex = indexOfClause(query, "EXCEPT");
		if(exceptIndex >= 0) {
			return query.substring(fromIndex, exceptIndex).trim();
		}

		int intersectIndex = indexOfClause(query, "INTERSECT");
		if(intersectIndex >= 0) {
			return query.substring(fromIndex, intersectIndex).trim();
		}
		
		String clause = query.substring(fromIndex).trim();
		if (clause.endsWith(";")) {
			return clause.substring(0,  clause.length()-1);
		}
		else {
			return clause;
		}
		
	}
	
	public String findSelectClause(String query) {
		int fromClauseIndex = indexOfClause(query, "FROM");
		return query.substring(0, fromClauseIndex).trim();
	}

	public String findGroupByClause(String query) {
		
		int groupByIndex = indexOfClause(query, "GROUP\\s+BY");
		if (groupByIndex >= 0) {
			int havingIndex = indexOfClause(query, "HAVING");
			if (havingIndex >= 0) {
				return query.substring(groupByIndex, havingIndex).trim();
			}
			
			int orderByIndex = indexOfClause(query, "ORDER\\s+BY");
			if (orderByIndex >= 0) {
				return query.substring(groupByIndex, orderByIndex).trim();
			}
			
			String clause = query.substring(groupByIndex).trim();
			if (clause.endsWith(";")) {
				return clause.substring(0,  clause.length()-1);
			}
			else {
				return clause;
			}
		}
		else {
			return "";
		}
	}

	public boolean foundNestedQuery(String clause) {
		Pattern selectKeyWord = Pattern.compile("(?i:\\(?SELECT)");
		Matcher selectMatcher = selectKeyWord.matcher(clause);

		if (selectMatcher.find()) {
			return true;
		}
		return false;
	}
	
	public String getNestedSubQuery(String query, int startFromIndex) {//String priorSubquery) {
		Pattern selectKeyWord = Pattern.compile("(?i:\\(\\s*SELECT)");
		Matcher selectMatcher = selectKeyWord.matcher(query);
		String nestedSubQuery = null;

		if (selectMatcher.find(startFromIndex)) {
			int subQueryStartPosition = selectMatcher.start();
			int subQueryEndPosition = getClosingPosition(query.substring(subQueryStartPosition)) + subQueryStartPosition;
			nestedSubQuery = query.substring(subQueryStartPosition+1, subQueryEndPosition);
		}
		//increment the level of nested sub nested query here?
		
		return nestedSubQuery;
	}
	
	public String getParenthesizedExpression(String query) {
		Pattern selectKeyWord = Pattern.compile("[(]");
		Matcher selectMatcher = selectKeyWord.matcher(query);

		String parenthesizedExpression = null;
		if (selectMatcher.find()) {
			int parenthesizedExpressionStartPosition = selectMatcher.start();
			String alteredQuery = query.substring(parenthesizedExpressionStartPosition);
			int parenthesizedExpressionEndPosition = parenthesizedExpressionStartPosition + getClosingPosition(alteredQuery);
			parenthesizedExpression = query.substring(parenthesizedExpressionStartPosition+1, parenthesizedExpressionEndPosition);
		}
		
		//assumption: if the expression doesn't contain an operator, then it is a field name argument to a grouping function
		if(parenthesizedExpression == null || !containsOperator(parenthesizedExpression) || parenthesizedExpression.toUpperCase().startsWith("SELECT ")) {
			return null;
		} else {
			return parenthesizedExpression;
		}
	}

	public int getClosingPosition(String string) {
		int i = 0;
		int counter = 0;
		boolean foundOpeningParenthesis = false;
		for (i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			if (c == '(') {
				foundOpeningParenthesis = true;
				counter++;
			} else if (c == ')') {
				counter--;
			} else if (c == '\'') {
				i = skipToClosingQuote(string, i);
			}
			if (counter == 0 && foundOpeningParenthesis) {
				break;
			}
		}
		return i;
	}

	public int skipToClosingQuote(String string, int counter) {
		return string.indexOf('\'', counter + 1);
	}

	public boolean containsSetOperation(String sql) {
		for(String setOperator: setOperators) {
			if(indexOfClause(sql, setOperator) != -1) {
				return true;
			}
		}
		return false;
	}

	public ArrayList<String> handleSetOperators(String sql) throws SQLException {
		List<String> setOperationStrings = Arrays.asList(setOperators);
		ArrayList<String> queries = new ArrayList<>();
		int indexOfSetOperator = -1;
		String setOperatorPresent = "";

		if (sql.endsWith(";")) {
			sql = sql.substring(0,  sql.length()-1);
		}

		for(String setOperation: setOperationStrings) {
			indexOfSetOperator = sql.toUpperCase().indexOf(setOperation);
			if(indexOfSetOperator != -1) {
				setOperatorPresent = setOperation;
				break;
			}
		}

		String firstQuery = sql.substring(0, indexOfSetOperator);
		queries.add(firstQuery);

		String secondQuery = sql.substring(indexOfSetOperator);
		int indexOfSecondSelect = secondQuery.indexOf("SELECT");
		secondQuery = secondQuery.substring(indexOfSecondSelect);
		queries.add(secondQuery);

		if(DatabaseDao.isSetOperationValid(database, queries)) {
			queries.add(setOperatorPresent);
			return queries;
		} else {
			return null;
		}
	}

	public void processFromClause(int startingIndex, String fromClause, String originalQuery, ArrayList<ParserResult> listOfResults) throws SQLException {
		String generatedQuery = "SELECT * ";
		String queryFragment;
		int startIndex = 0;
		int endIndex;
		int fragIndex;
		String beforeJoin = "";
		Pattern joinKeywordPattern = Pattern.compile("(?i:\\s+JOIN\\s+)");
		Matcher m = joinKeywordPattern.matcher(fromClause);
		
		while(m.find()){
			endIndex = m.start();
			
			while(Character.isWhitespace(fromClause.charAt(endIndex))) {
				endIndex++;
			}
			
			//check for prior "INNER" or "OUTER"
			beforeJoin = fromClause.substring(startIndex, endIndex);
			if(beforeJoin.toUpperCase().trim().endsWith("INNER")) {
				endIndex = beforeJoin.toUpperCase().lastIndexOf("INNER") + startIndex;
				beforeJoin = fromClause.substring(startIndex, endIndex);
			} else if(beforeJoin.toUpperCase().trim().endsWith("OUTER")) {
				endIndex = beforeJoin.toUpperCase().lastIndexOf("OUTER") + startIndex;
				beforeJoin = fromClause.substring(startIndex, endIndex);
			}
			
			//check for prior "LEFT", "RIGHT", or "FULL"
			if(beforeJoin.toUpperCase().trim().endsWith("RIGHT")) {
				endIndex = beforeJoin.toUpperCase().lastIndexOf("RIGHT") + startIndex;
				beforeJoin = fromClause.substring(startIndex, endIndex);
			} else if(beforeJoin.toUpperCase().trim().endsWith("LEFT")) {
				endIndex = beforeJoin.toUpperCase().lastIndexOf("LEFT") + startIndex;
				beforeJoin = fromClause.substring(startIndex, endIndex);
			} else if(beforeJoin.toUpperCase().trim().endsWith("FULL")) {
				endIndex =beforeJoin.toUpperCase().lastIndexOf("FULL") + startIndex;
				beforeJoin = fromClause.substring(startIndex, endIndex);
			}
			
			while(Character.isWhitespace(fromClause.charAt(endIndex))) {
				endIndex--;
			}
			
			queryFragment = fromClause.substring(startIndex, endIndex);
			generatedQuery += queryFragment;
			fragIndex = fromClause.indexOf(queryFragment.trim()) + startingIndex;
			createAndAddParserResult(fragIndex, queryFragment, generatedQuery, originalQuery, listOfResults);
			startIndex = endIndex;
		}
		
		queryFragment = fromClause.substring(startIndex);
		generatedQuery += queryFragment;
		fragIndex = fromClause.indexOf(queryFragment.trim()) + startingIndex;
		createAndAddParserResult(fragIndex, queryFragment, generatedQuery, originalQuery, listOfResults);
	}

	public void processWhereClause(int startingIndex, String whereClause, String query, ArrayList<ParserResult> parserResults, String whereClauseSoFar, String originalGeneratedQuery) throws SQLException {
		processWhereClause(startingIndex, whereClause, query, null, parserResults, whereClauseSoFar, originalGeneratedQuery);
	}
	
	public void processWhereClause(int startingIndex, String whereClause, String query, String outerQuery, ArrayList<ParserResult> parserResults, String whereClauseSoFar, String originalGeneratedQuery) throws SQLException {
		/* INITIAL SETUP */
		//strip WHERE operator since we'll be constructing our own
		String whereStart = "WHERE ";
		
		//Matcher m = Pattern.compile("^WHERE\\s+").matcher(whereClause);
		Pattern keyWordPattern = Pattern.compile("(?i:WHERE\\s+)");
		Matcher m = keyWordPattern.matcher(whereClause);
		
		if (m.find()){
			whereStart = m.group(0);
			whereClause = whereClause.replaceFirst(whereStart, "");
			startingIndex += whereStart.length();
		}

		if(originalGeneratedQuery == null || originalGeneratedQuery.isEmpty()) {
			originalGeneratedQuery = parserResults.get(parserResults.size()-1).getGeneratedQuery().trim();
			whereClauseSoFar = whereStart;
		}
		
		/* END INITIAL SETUP */
		
		//reorder ANDs and ORs at same parenthesis level (ANDs first)
		String reorderedWhereClause = reorderConditions(whereClause);
		String nextCondition;
		String parenthesizedExpression;
		int evaluateFromIndex = 0;
		int evaluateToIndex = -1;
		
		boolean isCorrelatedSubquery = isCorrelatedSubquery(query);
		boolean evaluatedCorrelatedCondition = false;
		
		do {
			//identify the next condition (which could contain a parenthesized expression)
			evaluateToIndex = nextConditionEndIndex(reorderedWhereClause, evaluateToIndex+1);
			nextCondition = reorderedWhereClause.substring(evaluateFromIndex, evaluateToIndex);
			evaluateFromIndex = evaluateToIndex;
			
			//if the next condition contains a parenthesized expression
			if((parenthesizedExpression = getParenthesizedExpression(nextCondition)) != null) {
				if(whereClauseSoFar == null) {
					whereClauseSoFar = whereStart;
				}
				//update whereClauseSoFar to include everything in the next condition before the parenthesized expression
				whereClauseSoFar += nextCondition.substring(0, nextCondition.indexOf("(")+1);
				//process the parenthesized expression
				int parenthStartIndex = whereClause.indexOf(parenthesizedExpression);
				processWhereClause(startingIndex + parenthStartIndex, parenthesizedExpression, query, outerQuery, parserResults, whereClauseSoFar, originalGeneratedQuery);
				//when the parenthesized expression has been processed, update whereClauseSoFar
				whereClauseSoFar +=  parenthesizedExpression + ")";
			} 
			//if the next operand does not contain a parenthesized expression
			else {
				if(whereClauseSoFar == null) {
					whereClauseSoFar = whereStart;
				}
				whereClauseSoFar += nextCondition;
				//ensure the generated query has all parenthesized expressions closed (won't while processing parenthesized expressions)
				String tempHavingClauseSoFar = whereClauseSoFar + generateClosingParenthesis(whereClauseSoFar);
				
				String conditionOnly = nextCondition.trim();
				int fragIndex = indexOfFragment(startingIndex, whereClause, conditionOnly, parserResults);
				if (fragIndex < startingIndex) {
					//fragment only
					String condOnly = conditionOnly;
					int orIndex = nextCondition.toUpperCase().indexOf("OR");
					int andIndex = nextCondition.toUpperCase().indexOf("AND");
					if (orIndex >= 0 && andIndex >= 0) {
						if (orIndex < andIndex) {
							condOnly = nextCondition.replaceFirst("(?i)\\s+OR\\s+", "").trim();
						} else {
							condOnly = nextCondition.replaceFirst("(?i)\\s+AND\\s+","").trim();
						}
					} else if (orIndex >= 0) {
						condOnly = nextCondition.replaceFirst("(?i)\\s+OR\\s+", "").trim();
					} else if (andIndex >= 0) {
						condOnly = nextCondition.replaceFirst("(?i)\\s+AND\\s+", "").trim();
					}
					
					fragIndex = indexOfFragment(startingIndex, whereClause, condOnly, parserResults);
				}
				String generatedQuery = originalGeneratedQuery + " " + tempHavingClauseSoFar;
				boolean isCorrelatedCondition = isCorrelatedSubquery && isCorrelatedCondition(conditionOnly, query);
				
				//not a correlated subquery or isn't a correlated condition and a correlated condition hasn't been seen
				if(!isCorrelatedSubquery || (!isCorrelatedCondition && !evaluatedCorrelatedCondition)) {
					createAndAddParserResult(fragIndex, conditionOnly, generatedQuery, query, parserResults);
				} 
				//the query is a correlated subquery and this is the correlated condition
				else if(isCorrelatedCondition) {
					generateCorrelatedConditionParserResult(conditionOnly, whereClause, query, outerQuery, generatedQuery, fragIndex, parserResults);
					evaluatedCorrelatedCondition = true;
				} 
				//if the query is a correlated subquery and the correlated condition has been seen
				else {
					generatePostCorrelatedConditionParserResult(conditionOnly, whereClause, query, outerQuery, generatedQuery, fragIndex, parserResults);
				}
			}
		} while(evaluateToIndex < reorderedWhereClause.length());
	}
	
	public void processSelectClause(int fragIndex, String selectClause, String originalQuery, ArrayList<ParserResult> listOfResults, ParserResult lastParserResult) throws SQLException {
		String generatedQuery = lastParserResult.getGeneratedQuery();
		
		if(isCorrelatedSubquery(generatedQuery) && containsGroupingFunctions(selectClause)) {
			generateCorrelatedSubqueryParserResultWithGroupBy(selectClause, generatedQuery, fragIndex, originalQuery, listOfResults);
		} else {
			generatedQuery = generatedQuery.replaceFirst("SELECT [*]", selectClause.toLowerCase());
			createAndAddParserResult(fragIndex, selectClause, generatedQuery, originalQuery, listOfResults);
		}
	}
	
	public void processGroupByClause(int fragIndex, String groupByClause, String selectClause, String fromClause, String whereClause, String originalQuery, ArrayList<ParserResult> listOfResults, ParserResult lastParserResult) throws SQLException {
		String generatedQuery = selectClause.trim() + " " + fromClause.trim() + " " + whereClause.trim() + (whereClause.isEmpty()?"":" ") + groupByClause.trim();
		String queryFragment = groupByClause;
		createAndAddParserResult(fragIndex, queryFragment, generatedQuery, originalQuery, listOfResults);
	}
	
	public void processHavingClause(int startingIndex, String havingClause, ArrayList<ParserResult> parserResults, String havingClauseSoFar, String originalGeneratedQuery, String originalQuery) throws SQLException {
		/* INITIAL SETUP */
		//strip WHERE operator since we'll be constructing our own
		String havingStart = "HAVING ";
		
		//Matcher m = Pattern.compile("^HAVING\\s+").matcher(havingClause);
		Pattern keyWordPattern = Pattern.compile("(?i:HAVING\\s+)");
		Matcher m = keyWordPattern.matcher(havingClause);
		
		if (m.find()){
			havingStart = m.group(0);
			havingClause = havingClause.replaceFirst(havingStart, "");
			startingIndex += havingStart.length();
		}
		
		if(originalGeneratedQuery == null || originalGeneratedQuery.isEmpty()) {
			originalGeneratedQuery = parserResults.get(parserResults.size()-1).getGeneratedQuery().trim();
			havingClauseSoFar = havingStart;
		}
		/* END INITIAL SETUP */
		
		//reorder ANDs and ORs at same parenthesis level (ANDs first)
		String reorderedHavingClause = reorderConditions(havingClause);
		String nextCondition;
		String parenthesizedExpression;
		int evaluateFromIndex = 0;
		int evaluateToIndex = -1;
		
		do {
			//identify the next condition (which could contain a parenthesized expression)
			evaluateToIndex = nextConditionEndIndex(reorderedHavingClause, evaluateToIndex+1);
			nextCondition = reorderedHavingClause.substring(evaluateFromIndex, evaluateToIndex);
			evaluateFromIndex = evaluateToIndex;
			
			//if the next condition contains a parenthesized expression
			if((parenthesizedExpression = getParenthesizedExpression(nextCondition)) != null) {
				if(havingClauseSoFar == null) {
					havingClauseSoFar = havingStart;
				}
				//update whereClauseSoFar to include everything in the next condition before the parenthesized expression
				havingClauseSoFar += nextCondition.substring(0, nextCondition.indexOf("(")+1);
				//process the parenthesized expression
				int parenthStartIndex = havingClause.indexOf(parenthesizedExpression);
				processHavingClause(startingIndex + parenthStartIndex, parenthesizedExpression, parserResults, havingClauseSoFar, originalGeneratedQuery, originalQuery);
				//when the parenthesized expression has been processed, update whereClauseSoFar
				havingClauseSoFar +=  parenthesizedExpression + ")";
			} 
			//if the next operand does not contain a parenthesized expression
			else {
				if(havingClauseSoFar == null) {
					havingClauseSoFar = havingStart;
				}
				havingClauseSoFar += nextCondition;
				
				//ensure the generated query has all parenthesized expressions closed (won't while processing parenthesized expressions)
				String tempHavingClauseSoFar = havingClauseSoFar + generateClosingParenthesis(havingClauseSoFar);
				
				String conditionOnly = nextCondition.trim();
				int fragIndex = indexOfFragment(startingIndex, havingClause, conditionOnly, parserResults);
				if (fragIndex < startingIndex) {
					//fragment only
					String condOnly = conditionOnly;
					int orIndex = nextCondition.toUpperCase().indexOf("OR");
					int andIndex = nextCondition.toUpperCase().indexOf("AND");
					if (orIndex >= 0 && andIndex >= 0) {
						if (orIndex < andIndex) {
							condOnly = nextCondition.replaceFirst("(?i)\\s+OR\\s+", "").trim();
						} else {
							condOnly = nextCondition.replaceFirst("(?i)\\s+AND\\s+","").trim();
						}
					} else if (orIndex >= 0) {
						condOnly = nextCondition.replaceFirst("(?i)\\s+OR\\s+", "").trim();
					} else if (andIndex >= 0) {
						condOnly = nextCondition.replaceFirst("(?i)\\s+AND\\s+", "").trim();
					}
					
					fragIndex = indexOfFragment(startingIndex, havingClause, condOnly, parserResults);
				}
				
				createAndAddParserResult(fragIndex, conditionOnly, originalGeneratedQuery + " " + tempHavingClauseSoFar, originalQuery, parserResults);
			}
		} while(evaluateToIndex < reorderedHavingClause.length());
	}
	
	public void processOrderByClause(int fragIndex, String orderByClause, String selectClause, String fromClause, String whereClause, String groupByClause, String havingClause, String originalQuery, ArrayList<ParserResult> listOfResults, ParserResult lastParserResult) throws SQLException {
		String generatedQuery = selectClause.trim() + " " + fromClause.trim() + " " + whereClause.trim() + (whereClause.isEmpty()?"":" ") + groupByClause.trim() + (groupByClause.isEmpty()?"":" ") + havingClause.trim() + (havingClause.isEmpty()?"":" ") + orderByClause.trim();
		String queryFragment = orderByClause;
		createAndAddParserResult(fragIndex, queryFragment, generatedQuery, originalQuery, listOfResults);
	}

	public String reorderConditions(String conditions) {
		String reorderedConditions = "";
		boolean strippedWhere = false;
		
		//strip WHERE (if present)
		if(conditions.startsWith("WHERE ")) {
			strippedWhere = true;
			conditions = conditions.replaceFirst("WHERE ", "");
		}
		
		String[] originalOrOperands = conditions.split("\\s+(?i)OR\\s+");
		ArrayList<String> orOperands = new ArrayList<String>();
		ArrayList<String> andConditions = new ArrayList<String>();
		
		int numUnclosedParenthesis = 0;
		String constructedOrOperand = "";
		
		//look at each OR operand
		for(String originalOrOperand: originalOrOperands) {
			//look for parenthesis within an OR operand
			for(int i=0; i<originalOrOperand.length(); ++i) {
				char currentChar = originalOrOperand.charAt(i);
				if(currentChar == '(') {
					numUnclosedParenthesis++;
				} else if(currentChar == ')') {
					numUnclosedParenthesis--;
				}
			}
			//if the parenthesis were unbalanced, it is part of a parenthesized OR operand
			if(numUnclosedParenthesis > 0) {
				constructedOrOperand = ((constructedOrOperand.isEmpty())?"":(constructedOrOperand + findOrString(conditions.substring(conditions.indexOf(constructedOrOperand)), originalOrOperand, reorderedConditions))) + originalOrOperand;
			} 
			//if the parenthesis are balanced
			else if(numUnclosedParenthesis == 0) {
				//if we are not looking at a component of a parenthesized OR operand
				if(constructedOrOperand.isEmpty()) {
					orOperands.add(originalOrOperand);
				}
				//if we are looking at the last component of a parenthesized OR operand
				else {
					constructedOrOperand = constructedOrOperand + findOrString(conditions.substring(conditions.indexOf(constructedOrOperand)), originalOrOperand, reorderedConditions) + originalOrOperand;
					orOperands.add(constructedOrOperand);
					constructedOrOperand = "";
				}
			}
		}
		
		//if there are OR operators in non-parenthesized conditions
		if(orOperands.size() > 1) {
			for(String orOperand: orOperands) {
				//identify OR operands with ANDs and separate
				Matcher andMatcher = Pattern.compile("\\s+(?i)AND\\s+").matcher(orOperand);
				
				if (orOperand.startsWith("(") || getNestedSubQuery(orOperand, 0) != null) {
					ArrayList<Integer> startIndices = new ArrayList<Integer>();
					ArrayList<Integer> endIndices = new ArrayList<Integer>();
					String parenth = null;
					int index = 0;			
					while ((parenth = getParenthesizedExpression(orOperand.substring(index))) != null) {
						index = orOperand.indexOf(parenth, index);
						startIndices.add(index);
						endIndices.add(index + parenth.length());
					}
					index = 0;
					while ((parenth = getNestedSubQuery(orOperand, index)) != null) {
						index = orOperand.indexOf(parenth, index+1);
						startIndices.add(index);
						index += parenth.length();
						endIndices.add(index);
					}
					
					boolean found = false;
					while (andMatcher.find() && !found) {
						for (int i = 0; i < startIndices.size(); i++) {
							if (andMatcher.start() > startIndices.get(i) && andMatcher.start() < endIndices.get(i)) {
								continue;
							}
							
							andConditions.add(orOperand);
							found = true;
							break;
						}
					}
					if (!found) {
						if(!reorderedConditions.equals("")) {
							reorderedConditions += findOrString(conditions, orOperand, reorderedConditions);
						}
						reorderedConditions += orOperand;
					}
					
					
				} else if (andMatcher.find()) {
					andConditions.add(orOperand);
				} else {
					if(!reorderedConditions.equals("")) {
						reorderedConditions += findOrString(conditions, orOperand, reorderedConditions);
					}
					reorderedConditions += orOperand;
				}

			}
			//add AND conditions at start
			for(int i=andConditions.size()-1; i>=0; --i) {
				if (reorderedConditions.equals("")) {
					reorderedConditions += andConditions.get(i);
					continue;
				}
				
				String rc = reorderedConditions;
				if (i == andConditions.size()-1) {
					rc = "";
				}
				String cond =  "";
				if (i < andConditions.size()-1) {
					cond = reorderedConditions.split("\\s+(?i)OR\\s+")[0];
				}
				reorderedConditions = andConditions.get(i) + findOrString(conditions, cond, rc) + reorderedConditions;
			}
			
		}
		//if there are no OR operators in non-parenthesized conditions
		else {
			reorderedConditions = conditions;
		}
		
		//add WHERE operator if originally present
		if(strippedWhere) {
			reorderedConditions = "WHERE " + reorderedConditions;
		}
		
		return reorderedConditions;
	}
	
	private void createAndAddParserResult(int fragIndex, String queryFragment, String generatedQuery, String originalQuery, ArrayList<ParserResult> parserResults) throws SQLException {
		ParserResult pr = new ParserResult();
		pr.setQueryFragment(queryFragment.trim());
		pr.setFragmentIndex(fragIndex);
		pr.setGeneratedQuery(generatedQuery.trim());
		pr.setOriginalQuery(originalQuery.trim());
		pr.setQueryResultSet(DatabaseDao.getQueryResultSet(database, generatedQuery));
		pr.setQueryNestingLevel(queryNestingLevel);
		
		parserResults.add(pr);
	}
	

	
	//assume that startIndex identifies the start of the next operand (for the first operand) or "AND" or "OR" for all other operands
	//the returned value will be the index of the last character in the string (for the last operand) or "AND" or "OR" for all other operands
	public int nextConditionEndIndex(String text, int startIndex) {
		
		//account for cases where we end in a parenthesized statement and the startIndex is > text length
		if(startIndex >= text.length()) {
			return text.length();
		}
		
		String parenthesizedExpression = getParenthesizedExpression(text.substring(startIndex));
		int nextParenthesizedExpressionIndex = -1;
		if(parenthesizedExpression != null) {
			nextParenthesizedExpressionIndex = text.indexOf(parenthesizedExpression, startIndex);
		}
		
		String subquery = getNestedSubQuery(text.substring(startIndex), 0);
		int nextSubqueryIndex = -1;
		if(subquery != null) {
			nextSubqueryIndex = text.indexOf(subquery, startIndex);
		}
		
		//find index of next AND
		Matcher andMatcher = Pattern.compile("\\s(?i)AND\\s").matcher(text.substring(startIndex));
		int nextAndIndex = -1;
		if (andMatcher.find()){
			nextAndIndex = andMatcher.start() + startIndex;
		}
		
		//find index of next OR
		Matcher orMatcher = Pattern.compile("\\s(?i)OR\\s").matcher(text.substring(startIndex));
		int nextOrIndex = -1;
		if (orMatcher.find()){
			nextOrIndex = orMatcher.start() + startIndex;
		}
		
		//initial case: find the first "AND" or "OR" (after any initial parenthesized expression)
		if(startIndex == 0) {
			//if we start with a parenthesized expression
			if(nextParenthesizedExpressionIndex != -1 && (nextAndIndex == -1 || nextParenthesizedExpressionIndex < nextAndIndex) && (nextOrIndex == -1 || nextParenthesizedExpressionIndex < nextOrIndex)) {
				return parenthesizedExpression.length()+2; 	//+2 for parenthesis
			}
			//if we start with a subquery and that subquery is not the last expression
			else if(nextSubqueryIndex != -1 && (nextAndIndex == -1 || nextSubqueryIndex < nextAndIndex) && (nextOrIndex == -1 || nextSubqueryIndex < nextOrIndex)  && (nextAndIndex != -1 || nextOrIndex != -1)) {
				return nextSubqueryIndex + subquery.length()+1; 	//+2 for parenthesis
			}
			//if we don't start with a parenthesized expression
			else {
				//if an AND is next
				if(nextAndIndex != -1 && (nextOrIndex == -1 || nextAndIndex < nextOrIndex)) {
					return nextAndIndex;
				}
				//if an OR is next
				else if(nextOrIndex != -1 && (nextAndIndex == -1 || nextOrIndex < nextAndIndex)) {
					return nextOrIndex;
				}
				//if we only have one condition
				else {
					return text.length();
				}
			}
		}
		//typical case: skip "AND" or "OR" and look for next "AND", "OR", or end of string
		else {
			//if a parenthesized expression is next
			if(nextParenthesizedExpressionIndex != -1 && (nextAndIndex == -1 || nextParenthesizedExpressionIndex < nextAndIndex) && (nextOrIndex == -1 || nextParenthesizedExpressionIndex < nextOrIndex)) {
			//if(nextParenthesizedExpressionIndex != -1 && (nextAndIndex == -1 || nextParenthesizedExpressionIndex < nextAndIndex) && (nextOrIndex == -1 || nextParenthesizedExpressionIndex < nextOrIndex)) {
				int nextConditionEndIndex = nextParenthesizedExpressionIndex + parenthesizedExpression.length()+1; 	//+2 for parenthesis
				return (nextConditionEndIndex < text.length())?nextConditionEndIndex:text.length();	//the +2 for parenthesis is unnecessary if we've reached the end of the string
			}
			//if a subquery is next and that subquery is not the last expression
			if(nextSubqueryIndex != -1 && (nextAndIndex == -1 || nextSubqueryIndex < nextAndIndex) && (nextOrIndex == -1 || nextSubqueryIndex < nextOrIndex) && (nextAndIndex != -1 || nextOrIndex != -1)) {
			//if(nextSubqueryIndex != -1 && (nextAndIndex == -1 || nextSubqueryIndex < nextAndIndex) && (nextOrIndex == -1 || nextSubqueryIndex < nextOrIndex)) {
				int nextConditionEndIndex = nextSubqueryIndex + subquery.length()+1; 	//+2 for parenthesis
				return (nextConditionEndIndex < text.length())?nextConditionEndIndex:text.length();	//the +2 for parenthesis is unnecessary if we've reached the end of the string
			}
			//if AND is next
			else if(nextAndIndex != -1 && (nextOrIndex == -1 || nextAndIndex < nextOrIndex)) {
				return nextAndIndex;
			}
			//if OR is next
			else if(nextOrIndex != -1 && (nextAndIndex == -1 || nextOrIndex < nextAndIndex)) {
				//repeat beginning from what's after OR
				return nextOrIndex;
			}
		}
		//only reachable if we've finished processing
		return text.length();
	}
	
	private String generateClosingParenthesis(String queryComponent) {
		int numUnclosedParenthesis = 0;
		String closingParenthesis = "";
		for(int i=0; i<queryComponent.length(); ++i) {
			char currentChar = queryComponent.charAt(i);
			if(currentChar == '(') {
				numUnclosedParenthesis++;
			} else if(currentChar == ')') {
				numUnclosedParenthesis--;
			}
		}
		for(int i=0; i<numUnclosedParenthesis; ++i) {
			closingParenthesis += ")";
		}
		return closingParenthesis;
	}
	
	private boolean containsOperator(String expression) {
		return expression.contains("=") || expression.contains("<") || expression.contains(">") || expression.contains(" IN ") || expression.contains(" BETWEEN ");
	}
	
	public int countNumConditions(String queryComponent) {
		int numConditions = 0;
		int i=0;
		while(i != -1) {
			i = queryComponent.toUpperCase().indexOf(" AND ", i+1);
			if(i != -1) {
				numConditions++;
			}
		}
		i=0;
		while(i != -1) {
			i = queryComponent.toUpperCase().indexOf(" OR ", i+1);
			if(i != -1) {
				numConditions++;
			}
		}
		return numConditions+1;
	}
	
	public String toUpperQuery(String query){
		StringBuilder outQuery = new StringBuilder(query.length());
		boolean isLiteral = true;
				
		String[] queryParts = query.split("'", StringUtils.countOccurrencesOf(query, "'"));
		outQuery.append(queryParts[0].toUpperCase());
		for (int i = 1; i < queryParts.length; i++){
			if (!isLiteral){
				outQuery.append("'" + queryParts[i].toUpperCase());
				isLiteral = true;
			}
			else{
				outQuery.append("'" + queryParts[i]);
				// Was this an escaped quote in a literal?
				if (!queryParts[i].endsWith("'")){
					isLiteral = false;
				}
			}
		}		
		return outQuery.toString();
	}
	
	/**
	 * Finds the index of a clause in the supplied query using the supplied keyword.
	 * @param query The query statement.
	 * @param keyword A regex expression string that contains the keyword. Examples: "SELECT", "FROM", "WHERE", "GROUP\\s+BY", etc..
	 * @return The index of the keyword in the query statement.
	 */
	public int indexOfClause(String query, String keyword) {
		
		Pattern keyWordPattern = Pattern.compile("(?i:" + keyword + ")");
		Matcher keyWordMatcher = keyWordPattern.matcher(query);
		String subquery = getNestedSubQuery(query, 0);
		int clauseStartIndex = -1;
		int subqueryStartIndex, subqueryEndIndex;
		
		if (keyWordMatcher.find()) {
			clauseStartIndex = keyWordMatcher.start();
			subqueryStartIndex = 0;
			subqueryEndIndex = 0;
			if(subquery != null) {
				subqueryStartIndex = query.indexOf(subquery);
				subqueryEndIndex = subqueryStartIndex + subquery.length();
				//if clause is within subquery
				if(clauseStartIndex > subqueryStartIndex && clauseStartIndex < subqueryEndIndex) {
					int result = indexOfClause(query.substring(subqueryEndIndex), keyword);
					if (result >= 0) {
						return subqueryEndIndex + result;
					}
					else {
						return result;
					}
				}
			}
		}
		
		return clauseStartIndex;
	}

	public int indexOfFragment(int clauseStartIndex, String clause, String fragment, ArrayList<ParserResult> listOfResults) {
		
		ArrayList<Integer> subqueryStartIndices = new ArrayList<Integer>();
		ArrayList<Integer> subqueryEndIndices = new ArrayList<Integer>();
		String subquery = null;//getNestedSubQuery(clause, null);
		int fragStartIndex = -1;
		
		int index = 0;//-1;
		while((subquery = getNestedSubQuery(clause, index)) != null) {
			index = clause.indexOf(subquery, index);
			subqueryStartIndices.add(index);
			index += subquery.length();
			subqueryEndIndices.add(index);
		}
		
		ArrayList<Integer> parenthStartIndex = new ArrayList<Integer>();
		ArrayList<Integer> parenthEndIndex = new ArrayList<Integer>();
		String parenth = null;
		int pIndex = 0;
		while ((parenth = getParenthesizedExpression(clause.substring(pIndex))) != null) {
			int parIndex = clause.indexOf(parenth, pIndex);
			parenthStartIndex.add(parIndex);
			parenthEndIndex.add(parIndex + parenth.length());
			pIndex = parIndex + parenth.length();
		}
		
		ArrayList<Integer> fragIndices = new ArrayList<Integer>();
		while ((fragStartIndex = clause.indexOf(fragment, fragStartIndex+1)) >= 0) {
			if (!subqueryStartIndices.isEmpty()) {
				for (int i = 0; i < subqueryStartIndices.size(); i ++) {
					if(fragStartIndex > subqueryStartIndices.get(i) && fragStartIndex < subqueryEndIndices.get(i)) {
						continue;
					}
					fragIndices.add(fragStartIndex);
				}
			} 
			else {
				fragIndices.add(fragStartIndex);
			}
		}
		
		if (fragIndices.size() == 1) {
			if (!isFragIndexPresent(clauseStartIndex + fragIndices.get(0), fragment, listOfResults)) {
				return clauseStartIndex + fragIndices.get(0);
			}
			return -1;
		}
		else {
			if (!parenthStartIndex.isEmpty()) {
				for (int f = fragIndices.size() - 1; f >= 0; f--) {
					for (int i = 0; i < parenthStartIndex.size(); i ++) {
						if(fragIndices.get(f) >= parenthStartIndex.get(i) && fragIndices.get(f) < parenthEndIndex.get(i)) {
							fragIndices.remove(f);
						}
					}
				}
			}
			if (fragIndices.size() == 1) {
				if (!isFragIndexPresent(clauseStartIndex + fragIndices.get(0), fragment, listOfResults)) {
					return clauseStartIndex + fragIndices.get(0);
				}
				return -1;
			}
		}
		
		ArrayList<Integer> andStarts = new ArrayList<Integer>();
		ArrayList<Integer> andEnds = new ArrayList<Integer>();
		Matcher andMatch = Pattern.compile("\\s+AND\\s+").matcher(clause);
		while (andMatch.find()) {
			if (!subqueryStartIndices.isEmpty()) {
				for (int i = 0; i < subqueryStartIndices.size(); i++) {
					if(andMatch.start() > subqueryStartIndices.get(i) && andMatch.end() < subqueryEndIndices.get(i)) {
						continue;
					}
					andStarts.add(andMatch.start());
					andEnds.add(andMatch.end());
				}
			} 
			else if (!parenthStartIndex.isEmpty()) {
				for (int i = 0; i < parenthStartIndex.size(); i++) {
					if(andMatch.start() > parenthStartIndex.get(i) && andMatch.end() < parenthEndIndex.get(i)) {
						continue;
					}
					andStarts.add(andMatch.start());
					andEnds.add(andMatch.end());
				}
			}
			else {	
				andStarts.add(andMatch.start());
				andEnds.add(andMatch.end());
			}
		}
		
		if (andStarts.isEmpty()) {
			// no ands
			for(int i: fragIndices) {
				fragStartIndex = i;
				if(!isFragIndexPresent(clauseStartIndex + fragStartIndex, fragment, listOfResults)) {
					break;
				}
			}
		}
		else {

			int bestIndex = -1;
			for (int i = 0; i < fragIndices.size(); i++) {
				if (bestIndex >= 0) {
					break;
				}
				fragStartIndex = fragIndices.get(i);
				for (int x = 0; x < andStarts.size(); x++) {					
					boolean touchesAnd = false;
					// is the fragment an operand of an and?
					if (!touchesAnd && fragStartIndex + fragment.length() == andStarts.get(x)) {
						touchesAnd = true;
					}
					if (!touchesAnd && fragStartIndex == andEnds.get(x)) {
						touchesAnd = true;
					}
					if(touchesAnd && !isFragIndexPresent(clauseStartIndex + fragStartIndex, fragment, listOfResults)) {
						bestIndex = fragStartIndex;
						break;
					}
				}
			}
			
			if (bestIndex < 0) {
				int z = 0;
				while (z < fragIndices.size() && bestIndex < 0) {
					fragStartIndex = fragIndices.get(z);
					if(!isFragIndexPresent(clauseStartIndex + fragStartIndex, fragment, listOfResults)) {
						bestIndex = fragStartIndex;
					}
					z++;
				}
			}
			fragStartIndex = bestIndex;
		}
		
		return clauseStartIndex + fragStartIndex;
	}

	public boolean isFragIndexPresent(int index, String frag, ArrayList<ParserResult> results) {
		boolean isPresent = false;
		int i = 0;
		while (i < results.size() && !isPresent) {
			if (results.get(i).getFragmentIndex() == index) {
				isPresent = true;
			}
			// check if our fragment is part of an existing fragment
			else// if (results.get(i).getQueryFragment().contains(frag)) {
				// check if our index is within an existing fragment
				if (index > results.get(i).getFragmentIndex() && index < results.get(i).getFragmentIndex() + results.get(i).getQueryFragment().length()) {
					isPresent = true;
				//}
			}
			// check if our fragment contains an existing fragment
			else //if (frag.contains(results.get(i).getQueryFragment())) {
				// check if our fragment contains an existing fragmentIndex
				if (results.get(i).getFragmentIndex() > index && results.get(i).getFragmentIndex() < index + frag.length()) {
					if (!foundNestedQuery(frag)) {
						isPresent = true;
					}
				//}
			}
			i++;
		}
		
		return isPresent;
	}
	
	public String findOrString(String conditions) {
		Matcher upperOrMatcher = Pattern.compile("\\s+OR\\s+").matcher(conditions);
		Matcher lowerOrMatcher = Pattern.compile("\\s+or\\s+").matcher(conditions);
		Matcher cappedOrMatcher = Pattern.compile("\\s+Or\\s+").matcher(conditions);
		if (upperOrMatcher.find()){
			return upperOrMatcher.group(0);
		} else if(lowerOrMatcher.find()) {
			return lowerOrMatcher.group(0);
		} else if(cappedOrMatcher.find()) {
			return cappedOrMatcher.group(0);
		} else {
			return null;
		}
	}
	
	public String findOrString(String conditions, String nextCondition, String reorderedConditions) {
		
		String firstOr = null;
		ArrayList<Integer> indices = new ArrayList<Integer>();
		int index = -1;
		if (nextCondition != null && nextCondition.length() > 0) {
			while ((index = conditions.indexOf(nextCondition, ++index)) > -1) {
				indices.add(index);
			}
		}
		int n = StringUtils.countOccurrencesOf(reorderedConditions, nextCondition);
		int condIndex = 0;
		
		Matcher andMatcher = Pattern.compile("(?i:\\s+AND\\s+)").matcher(nextCondition);
		if (n > 0 && andMatcher.find()) {
			n = Math.abs(n - StringUtils.countOccurrencesOf(conditions, nextCondition));
		}
		
		Matcher orMatcher = Pattern.compile("(?i:\\s+OR\\s+)").matcher(conditions);
		while (orMatcher.find()){
			if (firstOr == null) {
				firstOr = orMatcher.group(0);
			}
			
			if (!indices.isEmpty() && condIndex < indices.size()) {
				if (orMatcher.end() == indices.get(condIndex) || orMatcher.start() == indices.get(condIndex) + nextCondition.length()) {
					if (condIndex == n) {
						return orMatcher.group(0);
					}
					condIndex++;
				}
			}
		}
		
		return firstOr;
	}
	
	public boolean isCorrelatedSubquery(String query) {
		//remove any string literals
		Pattern stringLiteralPattern = Pattern.compile("\'.*?\'");
	    Matcher matcher = stringLiteralPattern.matcher(query);
	    query = matcher.replaceAll("");
		
		int dotIndex = -1;
		String tableAlias;
		int i;
		int aliasIndex = -1;
		boolean foundAliasDeclaration;
		boolean priorCharIsWhitespace;
		char followingChar;
		boolean followingCharIsWhitespaceOrComma;
		List<String> aliasesDeclared = new ArrayList<>();
		
		//find each table alias usage (e.g. c.course_id)
		while((dotIndex = query.indexOf(".", dotIndex+1)) != -1) {
			i = dotIndex;
			//get table alias
			while(!Character.isWhitespace(query.charAt(i))) {
				i--;
			}
			tableAlias = query.substring(i+1, dotIndex);
			if(!aliasesDeclared.contains(tableAlias)) {
				foundAliasDeclaration = false;
				aliasIndex = -1;
				//see if table alias is declared in query
				while ((aliasIndex = query.indexOf(tableAlias, aliasIndex + 1)) != -1) {
					priorCharIsWhitespace = aliasIndex > 0 && Character.isWhitespace(query.charAt(aliasIndex - 1));
					followingChar = query.charAt(aliasIndex + tableAlias.length());
					followingCharIsWhitespaceOrComma = (Character.isWhitespace(followingChar) || ',' == followingChar);
					//if characters before and after alias are whitespace (or comma after alias), the alias is declared in the subquery
					if (priorCharIsWhitespace && followingCharIsWhitespaceOrComma) {
						foundAliasDeclaration = true;
						aliasesDeclared.add(tableAlias);
						break;
					}
				}
				if (!foundAliasDeclaration) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isCorrelatedCondition(String condition, String query) {
		String tableAlias;
		int lastAliasIndex = -1;
		int lastDotIndex = -1;
		int i;
		boolean foundAliasDeclaration;
		boolean priorCharIsWhitespace;
		char followingChar;
		boolean followingCharIsWhitespaceOrComma;
		List<String> aliasesDeclared = new ArrayList<>();

		//find each table alias usage (e.g. c.course_id)
		while((lastDotIndex = condition.indexOf(".", lastDotIndex+1)) != -1) {
			i = lastDotIndex;
			//get table alias
			while(i>=0 && !Character.isWhitespace(condition.charAt(i))) {
				i--;
			}
			tableAlias = condition.substring(i+1, lastDotIndex);
			if(!aliasesDeclared.contains(tableAlias)) {
				foundAliasDeclaration = false;
				lastAliasIndex = -1;
				//see if table alias is declared in subquery
				while ((lastAliasIndex = query.indexOf(tableAlias, lastAliasIndex + 1)) != -1) {
					priorCharIsWhitespace = lastAliasIndex > 0 && Character.isWhitespace(query.charAt(lastAliasIndex - 1));
					followingChar = query.charAt(lastAliasIndex + tableAlias.length());
					followingCharIsWhitespaceOrComma = (Character.isWhitespace(followingChar) || ',' == followingChar);
					//if characters before and after alias are whitespace (or comma after alias), the alias is declared in the subquery
					if (priorCharIsWhitespace && followingCharIsWhitespaceOrComma) {
						foundAliasDeclaration = true;
						aliasesDeclared.add(tableAlias);
						break;
					}
				}
				if (!foundAliasDeclaration) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	private String getCorrelatedInnerColumn(String condition) {
		String operator = getConditionOperator(condition);
		
		//strip initial and/or if present
		condition = " " + condition + " ";
		condition = condition.replaceFirst("(?i)\\s+and\\s+", "");
		condition = condition.replaceFirst("(?i)\\s+or\\s+", "");
		
		String[] conditionList = condition.split(operator);
		if(conditionList[0].contains(".")) {
			return conditionList[1].trim();
		}
		else {
			return conditionList[0].trim();
		}
	}
	
	private String getCorrelatedOuterColumn(String condition) {
		String operator = getConditionOperator(condition);
		
		//strip initial and/or if present
		condition = " " + condition + " ";
		condition = condition.replaceFirst("(?i)\\s+and\\s+", "");
		condition = condition.replaceFirst("(?i)\\s+or\\s+", "");
		
		String[] conditionList = condition.split(operator);
		if(conditionList[0].contains(".")) {
			return conditionList[0].trim();
		}
		else {
			return conditionList[1].trim();
		}
	}
	
	private String getConditionOperator(String condition) {
		ArrayList<String> operators = new ArrayList<String>(Arrays.asList("=", "!=", "<>", "<", ">", "<=", ">=", "IN", "NOT", "BETWEEN", "EXISTS", "LIKE"));
		String[] conditionList = condition.split("\\s+");
		String s;
		int j;
		int startInd;
		int endInd;
		
		for(int i=0; i<conditionList.length; ++i) {
			s = conditionList[i];
			if(operators.contains(s.toUpperCase())) {
				if(s.toUpperCase().equals("NOT")) {
					j = i+1;
					while(j<conditionList.length && operators.contains(conditionList[j].toUpperCase())) {
						 j++;
					}
					startInd = condition.toUpperCase().indexOf(conditionList[i]);
					endInd = condition.toUpperCase().indexOf(conditionList[j]);
					s = condition.substring(startInd, endInd).trim();
				}
				return s;
			}
		}
		return null;
	}
	
	private void generateCorrelatedConditionParserResult(String condition, String whereClause, String query, String outerQuery, String generatedQuery, int fragIndex, ArrayList<ParserResult> parserResults) throws SQLException {
		String selectStatement = findSelectClause(query);
		String selectStatementWithoutFunctions = stripGroupByStatements(selectStatement);
		String outerFromClause = findFromClause(outerQuery);
		String correlatedInnerColumn = getCorrelatedInnerColumn(condition);
		String correlatedOuterColumn = getCorrelatedOuterColumn(condition);
		String operator = condition.replace(correlatedOuterColumn, "").replace(correlatedInnerColumn, "").toUpperCase().trim();
		String updatedCondition;
		
		//if subquery has a grouping function in SELECT
		if(!selectStatement.equals(selectStatementWithoutFunctions)) {
			//add column to end of SELECT for generated query
			String updatedSelectStatement = selectStatementWithoutFunctions.trim() + ", " + correlatedInnerColumn;
			generatedQuery = generatedQuery.replace(findSelectClause(generatedQuery), updatedSelectStatement);
		}

		//check for presence of negation operator to determine use of IN or NOT IN
		if(operator.equals("!=") || operator.equals("<>") || operator.startsWith("NOT")) {
			//replace condition with "[column] IN (SELECT [outer column] FROM [outer FROM])" for generated query
			updatedCondition = correlatedInnerColumn + " NOT IN (SELECT " + correlatedOuterColumn + " " + outerFromClause + ")";
		} else {
			//replace condition with "[column] IN (SELECT [outer column] FROM [outer FROM])" for generated query
			updatedCondition = correlatedInnerColumn + " IN (SELECT " + correlatedOuterColumn + " " + outerFromClause + ")";
		}
		
		//don't overwrite initial and/or in condition (which would already be in the generated query)
		condition = condition.replaceFirst("(?i)and\\s+", "");
		condition = condition.replaceFirst("(?i)or\\s+", "");
		generatedQuery = generatedQuery.replace(condition, updatedCondition);
		createAndAddParserResult(fragIndex, condition, generatedQuery, query, parserResults);
	}
	
	private void generatePostCorrelatedConditionParserResult(String condition, String whereClause, String query, String outerQuery, String generatedQuery, int fragIndex, ArrayList<ParserResult> parserResults) throws SQLException {
		//update the most recent parser result's generated query
		ParserResult pr = parserResults.get(parserResults.size()-1);
		String previousGeneratedQuery = pr.getGeneratedQuery();
		generatedQuery = previousGeneratedQuery + " " + condition;
		createAndAddParserResult(fragIndex, condition, generatedQuery, query, parserResults);
	}
	
	private void generateCorrelatedSubqueryParserResultWithGroupBy(String selectClause, String generatedQuery, int fragIndex, String originalQuery, ArrayList<ParserResult> parserResults) throws SQLException {
		//add grouping function to previous parser result's SELECT (assuming selectClause column is only grouping function)
		String function = selectClause.toLowerCase().replaceFirst("select", "").trim();
		String functionArgument = function.substring(function.indexOf('(')+1, function.indexOf(')'));
		generatedQuery = generatedQuery.toLowerCase().replaceFirst(functionArgument, function);

		//add group by statement (assumes GROUP BY can be appended to the end)
		String generatedQuerySelect = generatedQuery.replaceFirst("\\s+from\\s+.*", "");
		String otherColumnsString = generatedQuerySelect.replaceFirst("select", "").replace(function, "");
		String[] otherColumns = otherColumnsString.split(",");
		String column;

		generatedQuery += " group by ";
		boolean updated = false;
		for(int i=0; i<otherColumns.length; ++i) {
			column = otherColumns[i];
			if(!column.trim().isEmpty()) {
				if(updated) {
					generatedQuery += ", ";
				}
				generatedQuery += column;
				updated = true;
			}
		}
		createAndAddParserResult(fragIndex, selectClause, generatedQuery, originalQuery, parserResults);
	}
}
