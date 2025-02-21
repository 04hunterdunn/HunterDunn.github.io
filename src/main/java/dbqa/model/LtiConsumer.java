package dbqa.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name="lti_consumer")
public class LtiConsumer {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Column(name="lti_consumer_id")
    private Integer ltiConsumerId;

    @Column(name="consumer_key")
    private String consumerKey;

    @Column(name="secret")
    private String secret;

    @OneToOne
    private DbqaUser dbqaUser;

    public Integer getLtiConsumerId() {
        return ltiConsumerId;
    }

    public void setLtiConsumerId(Integer ltiConsumerId) {
        this.ltiConsumerId = ltiConsumerId;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public DbqaUser getDbqaUser() {
        return dbqaUser;
    }

    public void setDbqaUser(DbqaUser dbqaUser) {
        this.dbqaUser = dbqaUser;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LtiConsumer)) return false;
        LtiConsumer that = (LtiConsumer) o;
        return Objects.equals(ltiConsumerId, that.ltiConsumerId) &&
                Objects.equals(consumerKey, that.consumerKey) &&
                Objects.equals(secret, that.secret);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ltiConsumerId, consumerKey, secret);
    }
}
