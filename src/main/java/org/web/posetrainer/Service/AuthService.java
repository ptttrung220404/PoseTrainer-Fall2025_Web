package org.web.posetrainer.Service;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.web.posetrainer.DTO.LoginRequest;
import org.web.posetrainer.DTO.LoginResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
@Service
public class AuthService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiKey;
    private final Firestore firestore;

    public AuthService(@Value("${firebase.webApiKey}") String apiKey,
                       Firestore firestore) {
        this.apiKey = apiKey;
        this.firestore = firestore;
    }
    public void sendPasswordResetEmail(String email) throws IOException {
        String url = "https://identitytoolkit.googleapis.com/v1/accounts:sendOobCode?key=" + apiKey;

        Map<String, Object> body = Map.of(
                "requestType", "PASSWORD_RESET",
                "email", email
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, entity, String.class);
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Email không tồn tại hoặc không hợp lệ.");
        }
    }

    public LoginResponse login(LoginRequest req) {
        String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + apiKey;

        Map<String, Object> body = Map.of(
                "email", req.getEmail(),
                "password", req.getPassword(),
                "returnSecureToken", true
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<LoginResponse> respEntity =
                    restTemplate.postForEntity(url, entity, LoginResponse.class);

            LoginResponse resp = respEntity.getBody();
            if (resp == null || resp.getIdToken() == null) {
                throw new RuntimeException("Login failed: empty response");
            }

            // đọc roles từ Firestore (nếu có) và gắn vào response
            try {
                var snap = firestore.collection("users")
                        .document(resp.getLocalId())
                        .get().get();

                if (snap.exists()) {
                    @SuppressWarnings("unchecked")
                    List<String> rolesArr = (List<String>) snap.get("roles");
                    if (rolesArr == null) {
                        String r = snap.getString("role");
                        if (r != null) rolesArr = List.of(r);
                    }
                    resp.setRoles(rolesArr);
                }
            } catch (InterruptedException | ExecutionException e) {
                // không chết app nếu đọc roles lỗi
                Thread.currentThread().interrupt();
            }

            return resp;

        } catch (HttpClientErrorException e) {
            // lỗi do email/pass sai, user disabled, ...
            throw new RuntimeException(e.getResponseBodyAsString(), e);
        }
    }
    public void adminChangeOwnPassword(String uid, String newPassword) throws Exception {
        FirebaseAuth.getInstance().updateUser(
                new UserRecord.UpdateRequest(uid)
                        .setPassword(newPassword)
        );
    }

}
