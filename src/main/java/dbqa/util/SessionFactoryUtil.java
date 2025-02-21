package dbqa.util;

import dbqa.model.Database;
import dbqa.model.DatabaseType;
import dbqa.model.QueryResultSet;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

public class SessionFactoryUtil {

	private static SessionFactory dbqaSessionFactory;
	private static Configuration dbqaConfiguration;
	private static SessionFactory selectedDatabaseSessionFactory;
	private static Configuration selectedDatabaseConfiguration;

	public static SessionFactory getDbqaSessionFactory() {
		if (dbqaSessionFactory == null) {
			if (dbqaConfiguration == null) {
				dbqaConfiguration = new Configuration().configure();
				dbqaSessionFactory = dbqaConfiguration.buildSessionFactory();
			} else {
				dbqaSessionFactory = dbqaConfiguration.buildSessionFactory();
			}
		}
		return dbqaSessionFactory;
	}

	public static SessionFactory getSessionFactory(Database database) {
		if(selectedDatabaseConfiguration == null || hasSelectedDatabaseChanged(database)) {
			selectedDatabaseConfiguration = new Configuration();
			selectedDatabaseConfiguration.setProperty("hibernate.connection.driver_class", database.getDatabaseType().getDriverName());
			selectedDatabaseConfiguration.setProperty("hibernate.connection.url", database.getUrl());
			selectedDatabaseConfiguration.setProperty("hibernate.connection.username", database.getUsername());
			String password = database.getPassword();
			if (password != null && !password.isEmpty()) {
				password = JasyptUtil.decrypt(password);
			}
			selectedDatabaseConfiguration.setProperty("hibernate.connection.password", password);
			selectedDatabaseConfiguration.setProperty("hibernate.dialect", database.getDatabaseType().getDialectName());

			//C3P0 settings
			selectedDatabaseConfiguration.setProperty("hibernate.c3p0.min_size", "5");
			selectedDatabaseConfiguration.setProperty("hibernate.c3p0.max_size", "20");
			selectedDatabaseConfiguration.setProperty("hibernate.c3p0.acquire_increment", "5");
			selectedDatabaseConfiguration.setProperty("hibernate.c3p0.checkoutTimeout", "1000");
			//selectedDatabaseConfiguration.setProperty("hibernate.c3p0.automaticTestTable", "C3P0_TEST_TABLE");
			selectedDatabaseConfiguration.setProperty("hibernate.c3p0.testConnectionOnCheckin", "true");
			selectedDatabaseConfiguration.setProperty("hibernate.c3p0.idleConnectionTestPeriod", "25200");

			selectedDatabaseSessionFactory = selectedDatabaseConfiguration.buildSessionFactory();
		}
		return selectedDatabaseSessionFactory;
	}

	private static boolean hasSelectedDatabaseChanged(Database database) {
		return selectedDatabaseConfiguration == null || !selectedDatabaseConfiguration.getProperty("hibernate.connection.driver_class").equals(database.getDatabaseType().getDriverName()) ||
				!selectedDatabaseConfiguration.getProperty("hibernate.connection.url").equals(database.getUrl()) || !selectedDatabaseConfiguration.getProperty("hibernate.connection.username").equals(database.getUsername());
	}
}
