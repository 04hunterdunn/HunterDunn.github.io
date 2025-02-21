package dbqa.dao;

import dbqa.model.LtiConsumer;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Random;

public class LtiConsumerDao extends HibernateDao {

    public static LtiConsumer getLtiConsumerByKey(String key) {
        Session session = dbqaSessionFactory.openSession();
        Transaction tx = null;
        Random r = new Random();

        try {
            tx = session.beginTransaction();
            Query<LtiConsumer> query = session.createQuery("FROM LtiConsumer WHERE consumerKey = :key", LtiConsumer.class);
            query.setParameter("key", key);
            List<LtiConsumer> ltiConsumers = query.list();
            tx.commit();
            if(ltiConsumers != null && !ltiConsumers.isEmpty()) {
                return ltiConsumers.get(0);
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
}
