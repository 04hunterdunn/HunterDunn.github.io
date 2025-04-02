package dbqa.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name="dbqa_user")
public class DbqaUser {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Column(name="dbqa_user_id")
    private Integer dbqaUserId;

    @Column(name="requires_authentication")
    private Boolean requiresAuthentication;

    @Column(name="email", nullable=false)
    private String email;

    @Column(name="hash", nullable=false)
    private String hash;

    @Column(name="salt")
    private String salt;

    @ManyToMany
    @JoinTable(
            joinColumns = {@JoinColumn(name = "dbqaUserID")},
            inverseJoinColumns = {@JoinColumn(name = "databaseId")}
    )
    private List<Database> databases;

    public DbqaUser() {
        databases = new ArrayList<Database>();
    }

    public Integer getDbqaUserId() {
        return dbqaUserId;
    }

    public void setDbqaUserId(Integer dbqaUserId) {
        this.dbqaUserId = dbqaUserId;
    }

    public Boolean getRequiresAuthentication() {
        return requiresAuthentication;
    }

    public void setRequiresAuthentication(Boolean requiresAuthentication) {
        this.requiresAuthentication = requiresAuthentication;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public List<Database> getDatabases() {
        return databases;
    }

    public void setDatabases(List<Database> databases) {
        this.databases = databases;
    }

    public void addDatabase(Database database) {
        this.databases.add(database);
    }

    @Override
    public String toString() {
        return this.email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DbqaUser)) return false;
        DbqaUser dbqaUser = (DbqaUser) o;
        return dbqaUserId.equals(dbqaUser.dbqaUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dbqaUserId);
    }
}
