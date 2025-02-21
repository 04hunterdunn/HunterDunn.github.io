package dbqa.dao;

import dbqa.model.Database;
import dbqa.model.DatabaseType;
import dbqa.model.QueryResultSet;
import dbqa.util.JasyptUtil;
import dbqa.util.SessionFactoryUtil;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.jdbc.Work;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import javax.servlet.http.HttpServletRequest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@Configuration
@PropertySource("classpath:application.properties")
public class DatabaseDao extends HibernateDao {

    private static Database defaultDatabase;
    private static Environment environment;

    @Autowired //cannot autowire variable since it is static, so this will have to do
    public void setEnvironment(Environment environment){
        DatabaseDao.environment = environment;
    }

    public static Database getDatabase(Integer databaseId) {
        Session session = dbqaSessionFactory.openSession();
        Transaction tx = null;
        Database database = null;

        try {
            tx = session.beginTransaction();
            Query<Database> query = session.createQuery("FROM Database WHERE databaseId = :id", Database.class);
            query.setParameter("id", databaseId);
            List<Database> databases = query.list();
            if(!databases.isEmpty()) {
                database = databases.get(0);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
        return database;
    }

    public static Database getDatabase(String url, String username, String password) {
        Session session = dbqaSessionFactory.openSession();
        Transaction tx = null;
        Database database = null;

        try {
            tx = session.beginTransaction();
            Query<Database> query = session.createQuery("FROM Database WHERE url = :url AND username = :username AND password = :password", Database.class);
            query.setParameter("url", url);
            query.setParameter("username", username);
            query.setParameter("password", password);
            List<Database> databases = query.list();
            if(!databases.isEmpty()) {
                database = databases.get(0);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
        return database;
    }

    public static Database getDefaultDatabase() {
        if(defaultDatabase != null) {
            return defaultDatabase;
        } else {
            String databaseUrl = environment.getProperty("dbqa.datasource.databaseUrl");
            String driverName = environment.getProperty("dbqa.datasource.driverName");
            String dialectName = environment.getProperty("dbqa.datasource.dialectName");
            String username = environment.getProperty("dbqa.datasource.username");
            String password = JasyptUtil.encrypt(environment.getProperty("dbqa.datasource.password"));
            String typeName = environment.getProperty("dbqa.datasource.typeName");
            String schemaPattern = environment.getProperty("dbqa.datasource.schemaPattern");
            String usernameAsSchema = environment.getProperty("dbqa.datasource.usernameAsSchema");

            DatabaseType databaseType = new DatabaseType(typeName, driverName, dialectName, null, schemaPattern, "true".equalsIgnoreCase(usernameAsSchema));
            defaultDatabase = new Database(databaseType, "DBQA", databaseUrl, username, password);
            defaultDatabase.setDatabaseId(Database.DEFAULT_DATABASE_ID);
            return defaultDatabase;
        }
    }

    public static Database insertOrRetrieveDatabase(Database database) {
        Session session = dbqaSessionFactory.openSession();
        Transaction tx = null;
        Database persistedDatabase = null;

        try {
            tx = session.beginTransaction();
            Query<Database> query = session.createQuery("FROM Database WHERE url = :url AND username = :username AND password = :password", Database.class);
            query.setParameter("url", database.getUrl());
            query.setParameter("username", database.getUsername());
            query.setParameter("password", database.getPassword());
            List<Database> databases = query.list();
            if(!databases.isEmpty()) {
                persistedDatabase = databases.get(0);
            } else {
                session.persist(database);
                persistedDatabase = database;
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
        return persistedDatabase;
    }

    public static DatabaseType insertOrRetrieveDatabaseType(DatabaseType databaseType) {
        Session session = dbqaSessionFactory.openSession();
        Transaction tx = null;
        DatabaseType persistedDatabaseType = null;

        try {
            tx = session.beginTransaction();
            Query<DatabaseType> query = session.createQuery("FROM DatabaseType WHERE driverName = :driver AND dialectName = :dialect", DatabaseType.class);
            query.setParameter("driver", databaseType.getDriverName());
            query.setParameter("dialect", databaseType.getDialectName());
            List<DatabaseType> databases = query.list();
            if(!databases.isEmpty()) {
                persistedDatabaseType = databases.get(0);
            } else {
                session.save(databaseType);
                persistedDatabaseType = databaseType;
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
        return persistedDatabaseType;
    }

    public static List<DatabaseType> getAllDatabaseTypes() {
        Session session = dbqaSessionFactory.openSession();
        Transaction tx = null;
        List<DatabaseType> databaseTypes = new ArrayList<>();

        try {
            tx = session.beginTransaction();
            Query<DatabaseType> query = session.createQuery("FROM DatabaseType", DatabaseType.class);
            databaseTypes = query.list();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
        return databaseTypes;
    }

    public static DatabaseType getDatabaseType(Integer databaseTypeId) {
        Session session = dbqaSessionFactory.openSession();
        Transaction tx = null;
        DatabaseType databaseType = null;

        try {
            tx = session.beginTransaction();
            Query<DatabaseType> query = session.createQuery("FROM DatabaseType WHERE databaseTypeId = :id", DatabaseType.class);
            query.setParameter("id", databaseTypeId);
            databaseType = query.getSingleResult();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
        return databaseType;
    }

    public static void addDatabaseSchemaToSession(Database database, HttpServletRequest request) {
        SessionFactory sessionFactory = SessionFactoryUtil.getSessionFactory(database);
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        TreeMap<String, ArrayList<String>> dbSchema = new TreeMap<String, ArrayList<String>>();
        TreeMap<String, QueryResultSet> tableData = new TreeMap<String, QueryResultSet>();

        try {
            transaction = session.beginTransaction();
            session.doWork(new Work() {
                @Override
                public void execute(Connection connection) throws SQLException {
                    DatabaseMetaData metaData = connection.getMetaData();
                    String catalog = database.getDatabaseType().getCatalog();
                    String schemaPattern;
                    if(database.getDatabaseType().isUsernameAsSchema()) {
                        schemaPattern = database.getUsername();
                    } else {
                        schemaPattern = database.getDatabaseType().getSchemaPattern();
                    }
                    String[] tableFilter = {"TABLE"};

                    ResultSet tables = metaData.getTables(catalog, schemaPattern, null, tableFilter);
                    while (tables.next()) {
                        String tableName = tables.getString(3);
                        ResultSet columns = metaData.getColumns(catalog, schemaPattern, tableName, null);
                        ArrayList<String> columnNames = new ArrayList<String>();
                        while (columns.next()) {
                            columnNames.add(columns.getString(4));
                        }
                        ResultSet pkrs = metaData.getPrimaryKeys(catalog, schemaPattern, tableName);
                        while (pkrs.next()) {
                            String primaryKey = pkrs.getString("COLUMN_NAME");
                            for (int i = 0; i < columnNames.size(); i++) {
                                if (columnNames.get(i).equals(primaryKey)) {
                                    String str = columnNames.get(i) + "(PK)";
                                    columnNames.set(i, str);
                                }
                            }
                        }
                        ResultSet fkrs = metaData.getImportedKeys(catalog, schemaPattern, tableName);
                        while (fkrs.next()) {
                            String foreignKey = fkrs.getString("FKCOLUMN_NAME");
                            for (int i = 0; i < columnNames.size(); i++) {
                                if (columnNames.get(i).equals(foreignKey)) {
                                    String str = columnNames.get(i) + "(FK)";
                                    columnNames.set(i, str);
                                }
                            }
                        }
                        dbSchema.put(tableName, columnNames);
                        tableData.put(tableName, getQueryResultSet("SELECT * FROM " + tableName, session));
                    }
                }
            });
            request.getSession().setAttribute("schema", dbSchema);
            request.getSession().setAttribute("tableData", tableData);
        } catch (HibernateException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
    public static QueryResultSet getQueryResultSet(Database database, String generatedQuery) throws SQLException {
        SessionFactory sessionFactory = SessionFactoryUtil.getSessionFactory(database);
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        QueryResultSet qrs = null;
        try {
            transaction = session.beginTransaction();
            qrs = getQueryResultSet(generatedQuery, session);
        } catch (HibernateException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            if(e.getCause() instanceof SQLException) {
                throw new SQLException(e.getCause());
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
        return qrs;
    }

    public static QueryResultSet getQueryResultSet(String generatedQuery, Session session) {
        QueryResultSet queryResultSet = new QueryResultSet();
        session.doWork(new Work() {
            @Override
            public void execute(Connection conn) throws SQLException {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(generatedQuery);
                ResultSetMetaData rsmd = rs.getMetaData();
                ArrayList<String> columnNames = new ArrayList<String>();
                ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();

                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    columnNames.add(rsmd.getColumnName(i));
                }
                queryResultSet.setColumnNames(columnNames);
                while (rs.next()) {
                    ArrayList<String> row = new ArrayList<String>();
                    for (int j = 0; j <= queryResultSet.getColumnNames().size() - 1; j++) {
                        row.add(rs.getString(j + 1));
                    }
                    data.add(row);
                }
                queryResultSet.setData(data);
            }
        });

        return queryResultSet;
    }

    public static boolean isSetOperationValid(Database database, List<String> sqlQueries) throws SQLException {
        SessionFactory sessionFactory = SessionFactoryUtil.getSessionFactory(database);
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        ArrayList<String> priorQueryColumnTypes = null;
        boolean isValid = true;

        try {
            transaction = session.beginTransaction();
            for(String sql: sqlQueries) {
                ArrayList<String> columnTypes = new ArrayList<>();

                //get column types from database
                session.doWork(new Work() {
                    @Override
                    public void execute(Connection conn) throws SQLException {
                        Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery(sql);
                        ResultSetMetaData rsmd = rs.getMetaData();

                        for (int i = 1; i<=rsmd.getColumnCount(); i++) {
                            columnTypes.add(rsmd.getColumnTypeName(i));
                        }
                    }
                });

                if(priorQueryColumnTypes != null) {
                    //ensure same number of columns as in prior query
                    if (priorQueryColumnTypes.size() != priorQueryColumnTypes.size()) {
                        isValid = false;
                        break;
                    }
                    //ensure same column types
                    for (int i = 0; i < columnTypes.size(); i++) {
                        if (!columnTypes.get(i).equals(priorQueryColumnTypes.get(i))) {
                            isValid = false;
                            break;
                        }
                    }
                }
                priorQueryColumnTypes = columnTypes;
            }
        } catch (HibernateException e) {
            isValid = false;
            if (transaction != null) {
                transaction.rollback();
            }
            if(e.getCause() instanceof SQLException) {
                throw new SQLException(e.getCause());
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
        return isValid;
    }
}
