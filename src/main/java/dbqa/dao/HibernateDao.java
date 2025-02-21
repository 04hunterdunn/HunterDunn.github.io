package dbqa.dao;

import dbqa.util.SessionFactoryUtil;
import org.hibernate.SessionFactory;

public class HibernateDao {
    protected static SessionFactory dbqaSessionFactory = SessionFactoryUtil.getDbqaSessionFactory();
}
