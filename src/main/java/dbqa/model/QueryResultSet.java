package dbqa.model;

import java.util.ArrayList;

public class QueryResultSet {
	private ArrayList<String> columnNames;
	private ArrayList<ArrayList<String>> data;
	
	public QueryResultSet() {
		this.columnNames = null;
		this.data = null;
	}
	
	public QueryResultSet(ArrayList<String> columnNames, ArrayList<ArrayList<String>> data) {
		this.columnNames = columnNames;
		this.data = data;
	}

	public ArrayList<String> getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(ArrayList<String> columnNames) {
		this.columnNames = columnNames;
	}

	public ArrayList<ArrayList<String>> getData() {
		return data;
	}

	public void setData(ArrayList<ArrayList<String>> data) {
		this.data = data;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((columnNames == null) ? 0 : columnNames.hashCode());
		result = prime * result + ((data == null) ? 0 : data.hashCode());
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
		QueryResultSet other = (QueryResultSet) obj;
		if (columnNames == null) {
			if (other.columnNames != null)
				return false;
		} else if (!columnNames.equals(other.columnNames))
			return false;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		return true;
	}
	
}
