package dbqa.dao;

import dbqa.model.DbqaUser;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class DbqaUserDao extends HibernateDao {

    public static DbqaUser getUser(Integer id) {
        Session session = dbqaSessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Query<DbqaUser> query = session.createQuery("FROM DbqaUser WHERE dbqaUserId = :id", DbqaUser.class);
            query.setParameter("id", id);
            List<DbqaUser> users = query.list();
            tx.commit();
            if (users.size() == 1) {
                DbqaUser user = users.get(0);
                return user;
            }
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
        return null;
    }

    public static DbqaUser getUser(String email) {
        Session session = dbqaSessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Query<DbqaUser> query = session.createQuery("FROM DbqaUser WHERE email = :email", DbqaUser.class);
            query.setParameter("email", email);
            List<DbqaUser> users = query.list();
            tx.commit();
            if (users.size() == 1) {
                DbqaUser u = users.get(0);
                return u;
            }
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
        return null;
    }

    public static boolean insertUser(DbqaUser user) {
        Session session = dbqaSessionFactory.openSession();
        Transaction tx = null;
        boolean isSuccessful = true;
        try {
            tx = session.beginTransaction();
            session.persist(user);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            isSuccessful = false;
            e.printStackTrace();
        } finally {
            session.close();
        }
        return isSuccessful;
    }

    public static boolean updateUser(DbqaUser user) {
        Session session = dbqaSessionFactory.openSession();
        Transaction tx = null;
        boolean isSuccessful = true;
        try {
            tx = session.beginTransaction();
            session.merge(user);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            isSuccessful = false;
            e.printStackTrace();
        } finally {
            session.close();
        }
        return isSuccessful;
    }
}
