package io.baton.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Force {

    private static final String REST_VERSION = "35.0";

    @Bean
    private OAuth2RestTemplate oAuth2RestTemplate(OAuth2ProtectedResourceDetails resource, OAuth2ClientContext context) {
        return new OAuth2RestTemplate(resource, context);
    }

    @Autowired
    private OAuth2RestTemplate restTemplate;

    @SuppressWarnings("unchecked")
    private static String restUrl(OAuth2Authentication principal) {
        HashMap<String, Object> details = (HashMap<String, Object>) principal.getUserAuthentication().getDetails();
        HashMap<String, String> urls = (HashMap<String, String>) details.get("urls");
        return urls.get("rest").replace("{version}", REST_VERSION);
    }

    public List<User> users(OAuth2Authentication principal) {
        String url = restUrl(principal) + "query/?q={q}";

        Map<String, String> params = new HashMap<>();
        params.put("q", "SELECT Id, Name, Type, Industry, Rating FROM User");

        return restTemplate.getForObject(url, QueryResultUser.class, params).records;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        public String userId;
        public String orgId;
        public String userName;
        public String firstName;
        public String lastName;
        public String email;
        public String title;
        public String organization;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class QueryResult<T> {
        public List<T> records;
    }

    private static class QueryResultUser extends QueryResult<User> {}



    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserProvAccount {
        public String Status;
        public String LinkState;
        public String DeletedDate;
        public String ExternalEmail;
        public String ConnectedAppId;
        public String ExternalUserId;
        public String ExternalLastName;
        public String ExternalUsername;
        public String SalesforceUserId;
        public String ExternalFirstName;

    }


}
