package dbqa.model;

/*
 * This class identifies the iterations a DBMS goes 
 * through as it parses a given query. The ParserResult 
 * Class maintains a String "generatedQuery" that 
 * represents each result set a given query will produce
 * as the DBMS iterates through the given query.
 * The ResultSet "ResultSet" is a Java.sql object that
 * maintains the result after querying a DBMS. 
 * The String "queryFragment" represents the clause 
 * from the original query that the generatedQuery 
 * refers to. 
 * 
 */


public class ParserResult {
	private String generatedQuery;
	private QueryResultSet resultSet;
	private String queryFragment;
	private int fragmentIndex;
	private int queryNestingLevel;
	private String originalQuery;
	
	public ParserResult(){
		generatedQuery = null;
		resultSet = null;
		queryFragment = null;
		queryNestingLevel = 0;
	}
	
	public ParserResult(String generatedQuery, String queryFragment, int queryNestingLevel) {
		this.generatedQuery = generatedQuery;
		this.queryFragment = queryFragment;
		this.queryNestingLevel = queryNestingLevel;
	}

	public String getGeneratedQuery() {
		return generatedQuery;
	}

	public void setGeneratedQuery(String generatedQuery) {
		this.generatedQuery = generatedQuery;
	}

	public QueryResultSet getQueryResultSet() {
		return resultSet;
	}

	public void setQueryResultSet(QueryResultSet resultSet) {
		this.resultSet = resultSet;
	}

	public String getQueryFragment() {
		return queryFragment;
	}

	public void setQueryFragment(String queryFragment) {
		this.queryFragment = queryFragment;
	}
	
	public int getFragmentIndex() {
		return fragmentIndex;
	}

	public void setFragmentIndex(int fragmentIndex) {
		this.fragmentIndex = fragmentIndex;
	}

	public int getQueryNestingLevel() {
		return queryNestingLevel;
	}
	
	public void setQueryNestingLevel(int queryNestingLevel) {
		this.queryNestingLevel = queryNestingLevel;
	}
	
	public String getOriginalQuery() {
		return this.originalQuery;
	}
	
	public void setOriginalQuery(String originalQuery) {
		this.originalQuery = originalQuery;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((generatedQuery == null) ? 0 : generatedQuery.hashCode());
		result = prime * result + ((queryFragment == null) ? 0 : queryFragment.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ParserResult other = (ParserResult) obj;
		if (generatedQuery == null) {
			if (other.generatedQuery != null)
				return false;
		} else if (!generatedQuery.trim().equalsIgnoreCase(other.generatedQuery.trim()))
			return false;
		if (queryFragment == null) {
			if (other.queryFragment != null)
				return false;
		} else if (!queryFragment.trim().equalsIgnoreCase(other.queryFragment.trim()))
			return false;
		/*if (fragmentIndex != other.fragmentIndex)
			return false;*/
		return true;
	}
}
