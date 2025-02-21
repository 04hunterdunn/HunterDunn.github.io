package dbqa.dao;

import dbqa.model.DbqaUser;
import dbqa.model.QueryExample;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Random;

public class QueryExampleDao extends HibernateDao {

    public static QueryExample getQueryExampleByType(String queryType) {
        Session session = dbqaSessionFactory.openSession();
        Transaction tx = null;
        Random r = new Random();

        try {
            tx = session.beginTransaction();
            Query<QueryExample> query = session.createQuery("FROM QueryExample WHERE queryType = :type", QueryExample.class);
            query.setParameter("type", queryType);
            List<QueryExample> queryExamples = query.list();
            tx.commit();
            return queryExamples.get(r.nextInt(queryExamples.size()));
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
}
