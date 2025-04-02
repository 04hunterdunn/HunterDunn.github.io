package dbqa.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Cascade;

import java.util.List;
import java.util.Objects;

@Entity
@Table(name="dbqa_database")
public class Database {
    public static final int DEFAULT_DATABASE_ID = -1;
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Column(name="database_id")
    private Integer databaseId;

    @ManyToOne
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    private DatabaseType databaseType;

    @Column(name="name")
    private String name;

    @Column(name="url")
    private String url;

    @Column(name="username")
    private String username;

    @Column(name="password")
    private String password;

    public Database() {

    }

    public Database(DatabaseType databaseType, String name, String url, String username, String password) {
        this.databaseType = databaseType;
        this.name = name;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public Integer getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(Integer databaseId) {
        this.databaseId = databaseId;
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(DatabaseType databaseType) {
        this.databaseType = databaseType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Database{" +
                "databaseId=" + databaseId +
                ", url='" + url + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Database)) return false;
        Database database = (Database) o;
        return databaseId.equals(database.databaseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(databaseId);
    }
}
