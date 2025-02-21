package dbqa.model;

public class LtiData {
    private String oauth_consumer_key;      //identifies sender
    private String lis_outcome_service_url; //identifies url to which a result should be sent
    private String lis_result_sourcedid;    //unique identifier used by consumer to identify interaction
    private String canvasUserId;

    public LtiData(String oauth_consumer_key, String lis_outcome_service_url, String lis_result_sourcedid, String canvasUserId) {
        this.oauth_consumer_key = oauth_consumer_key;
        this. lis_outcome_service_url = lis_outcome_service_url;
        this.lis_result_sourcedid = lis_result_sourcedid;
        this.canvasUserId = canvasUserId;
    }

    public String getOauth_consumer_key() {
        return oauth_consumer_key;
    }

    public void setOauth_consumer_key(String oauth_consumer_key) {
        this.oauth_consumer_key = oauth_consumer_key;
    }

    public String getLis_outcome_service_url() {
        return lis_outcome_service_url;
    }

    public void setLis_outcome_service_url(String lis_outcome_service_url) {
        this.lis_outcome_service_url = lis_outcome_service_url;
    }

    public String getLis_result_sourcedid() {
        return lis_result_sourcedid;
    }

    public void setLis_result_sourcedid(String lis_result_sourcedid) {
        this.lis_result_sourcedid = lis_result_sourcedid;
    }

    public String getCanvasUserId() {
        return canvasUserId;
    }

    public void setCanvasUserId(String canvasUserId) {
        this.canvasUserId = canvasUserId;
    }

    @Override
    public String toString() {
        return "LtiData{" +
                "oauth_consumer_key='" + oauth_consumer_key + '\'' +
                ", lis_outcome_service_url='" + lis_outcome_service_url + '\'' +
                ", lis_result_sourcedid='" + lis_result_sourcedid + '\'' +
                ", canvasUserId='" + canvasUserId + '\'' +
                '}';
    }
}
