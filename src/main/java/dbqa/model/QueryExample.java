package dbqa.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name="query_example")
public class QueryExample {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Column(name="query_example_id")
    private Integer queryExamplerId;

    @Column(name="query_type")
    private String queryType;

    @Column(name="query")
    private String query;

    public Integer getQueryExamplerId() {
        return queryExamplerId;
    }

    public void setQueryExamplerId(Integer queryExamplerId) {
        this.queryExamplerId = queryExamplerId;
    }

    public String getQueryType() {
        return queryType;
    }

    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QueryExample)) return false;
        QueryExample that = (QueryExample) o;
        return Objects.equals(queryExamplerId, that.queryExamplerId) &&
                Objects.equals(queryType, that.queryType) &&
                Objects.equals(query, that.query);
    }

    @Override
    public int hashCode() {
        return Objects.hash(queryExamplerId, queryType, query);
    }
}
