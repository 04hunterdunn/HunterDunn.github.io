package dbqa.model;

public class Query {

	private String selectClause;
	private String fromClause;
	private String whereClause;
	private String orderByClause;
	private String havingClause;
	private String groupByClause;

	public Query() {
		selectClause = "select * ";
		fromClause = "";
		whereClause = "";
		orderByClause = "";
		havingClause = "";
		groupByClause = "";
	}

	public void setSelectClause(String clause) {
		this.selectClause = clause;
	}

	public void setFromClause(String clause) {
		this.fromClause = clause;
	}

	public void setWhereClause(String clause) {
		this.whereClause = clause;
	}

	public void setOrderByClause(String clause) {
		this.orderByClause = clause;
	}

	public void setHavingClause(String clause) {
		this.havingClause = clause;
	}

	public void setGroupByClause(String clause) {
		this.groupByClause = clause;
	}
	
	public String getSelectClause() {
		return this.selectClause;
	}
	
	public String getFromClause() {
		return this.fromClause;
	}
	
	public String getWhereClause() {
		return this.whereClause;
	}
	
	public String getOrderByClause() {
		return this.orderByClause;
	}
	
	public String getHavingClause() {
		return this.havingClause;
	}
	
	public String getGroupByClause() {
		return this.groupByClause;
	}

}
