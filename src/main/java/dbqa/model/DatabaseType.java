package dbqa.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name="database_type")
public class DatabaseType {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Column(name="database_type_id")
    private Integer databaseTypeId;

    @Column(name="type_name")
    private String typeName;

    @Column(name="driver_class")
    private String driverName;

    @Column(name="dialect_class")
    private String dialectName;

    @Column(name="catalog")
    private String catalog;

    @Column(name="schema_pattern")
    private String schemaPattern;

    @Column(name="username_as_schema")
    private Boolean usernameAsSchema;

    public DatabaseType() {

    }

    public DatabaseType(String typeName, String driverName, String dialectName, String catalog, String schemaPattern, Boolean usernameAsSchema) {
        this.typeName = typeName;
        this.driverName = driverName;
        this.dialectName = dialectName;
        this.catalog = catalog;
        this.schemaPattern = schemaPattern;
        this.usernameAsSchema = usernameAsSchema;
    }

    public Integer getDatabaseTypeId() {
        return databaseTypeId;
    }

    public void setDatabaseTypeId(Integer databaseTypeId) {
        this.databaseTypeId = databaseTypeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDialectName() {
        return dialectName;
    }

    public void setDialectName(String dialectName) {
        this.dialectName = dialectName;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getSchemaPattern() {
        return schemaPattern;
    }

    public void setSchemaPattern(String schemaPattern) {
        this.schemaPattern = schemaPattern;
    }

    public Boolean isUsernameAsSchema() {
        return usernameAsSchema;
    }

    public void setUsernameAsSchema(Boolean usernameAsSchema) {
        this.usernameAsSchema = usernameAsSchema;
    }

    @Override
    public String toString() {
        return "DatabaseType{" +
                "databaseTypeId=" + databaseTypeId +
                ", typeName='" + typeName + '\'' +
                ", driverName='" + driverName + '\'' +
                ", dialectName='" + dialectName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DatabaseType)) return false;
        DatabaseType that = (DatabaseType) o;
        return databaseTypeId.equals(that.databaseTypeId) &&
                typeName.equals(that.typeName) &&
                driverName.equals(that.driverName) &&
                dialectName.equals(that.dialectName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(databaseTypeId, typeName, driverName, dialectName);
    }
}
