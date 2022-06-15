package com.isedol_clip_backend.util;

import com.isedol_clip_backend.exception.RequestException;
import com.isedol_clip_backend.web.model.request.ClipRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class CallTwitchAPI {

    // https://dev.twitch.tv/docs/api/reference#get-users
    public JSONObject requestUser(String[] id, String[] name) throws IOException, RequestException {
        log.info("requestUser()");
        URL url = makeRequestUsersUrl(id, name);

        log.info("URL: {}", url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + LoadSecret.twitchAccessToken);
        conn.setRequestProperty("Client-Id", LoadSecret.twitchClientId);

        JSONObject jsonObject = getResponse(conn);
        log.info("Response twitch user: " + jsonObject);

        return jsonObject;
    }

    public JSONObject requestUserByToken(String token) throws IOException, RequestException {
        URL url = new URL("https://api.twitch.tv/helix/users");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Client-Id", LoadSecret.twitchClientId);

        JSONObject jsonObject = getResponse(conn);
        log.info("Response twitch token: " + jsonObject);

        return jsonObject;
    }

    //https://dev.twitch.tv/docs/api/reference#get-clips
    public JSONObject requestClips(ClipRequestDto dto) throws IOException, RequestException {
        URL url = makeRequestClipsUrl(dto);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + LoadSecret.twitchAccessToken);
        conn.setRequestProperty("Client-Id", LoadSecret.twitchClientId);

        JSONObject jsonObject = getResponse(conn);
        log.info("Response twitch clips: " + jsonObject);

        return jsonObject;
    }

    // https://dev.twitch.tv/docs/authentication/getting-tokens-oauth#authorization-code-grant-flow
    public JSONObject requestOauth(String code) throws IOException, RequestException {
        URL url = new URL("https://id.twitch.tv/oauth2/token");

        String parameters = "client_id="+LoadSecret.twitchClientId +
                "&client_secret="+LoadSecret.twitchSecret +
                "&code="+ code +
                "&grant_type=authorization_code" +
                "&redirect_uri=http://localhost:8080/isedol-clip/after-login";

        byte[] postData = parameters.getBytes(StandardCharsets.UTF_8);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        requestPostMethod(conn, postData);
//        conn.setDoOutput(true);
//        conn.setInstanceFollowRedirects(false);
//        conn.setRequestMethod("POST");
//        conn.setRequestProperty("charset", "utf-8");
//        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//        conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
//        conn.setUseCaches(false);
//
//        DataOutputStream output = new DataOutputStream(conn.getOutputStream());
//        output.write(postData);
//        output.flush();
//        output.close();

//        checkResponseStatus(conn);
        JSONObject jsonObject = getResponse(conn);
        log.info("Response twitch token: " + jsonObject);

        return jsonObject;
    }

    // App access token. 즉, 서버측의 토큰이지 사용자의 토큰 아님
    // https://dev.twitch.tv/docs/authentication/validate-tokens
    public boolean checkAccessTokenValidation() throws IOException {
        URL url = new URL("https://id.twitch.tv/oauth2/validate");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + LoadSecret.twitchAccessToken);

        int ResponseCode = conn.getResponseCode();
        if(ResponseCode == 200) {
            log.info("App Access Token Validation: {}, Access token is valid", ResponseCode);
            return true;
        } else {
            log.info("App Access Token Validation: {}, Access token is invalid. Need Reissue", ResponseCode);
            return false;
        }
    }

    // https://dev.twitch.tv/docs/authentication/getting-tokens-oauth/#client-credentials-grant-flow
    public boolean requestAccessToken() throws IOException, RequestException {
        URL url = new URL("https://id.twitch.tv/oauth2/token");

        String parameters = "client_id=" + LoadSecret.twitchClientId +
                "&client_secret=" + LoadSecret.twitchSecret +
                "grant_type=client_credentials";

        byte[] postData = parameters.getBytes(StandardCharsets.UTF_8);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        requestPostMethod(conn, postData);


//        conn.setDoOutput(true);
//        conn.setInstanceFollowRedirects(false);
//        conn.setRequestMethod("POST");
//        conn.setRequestProperty("charset", "utf-8");
//        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//        conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
//        conn.setUseCaches(false);
//
//        DataOutputStream output = new DataOutputStream(conn.getOutputStream());
//        output.write(postData);
//        output.flush();
//        output.close();

        JSONObject jsonObject = getResponse(conn);
        log.info("Response twitch access token: {}", jsonObject);

        return true;
    }

    private void requestPostMethod(HttpURLConnection conn, byte[] postData) throws IOException {
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("charset", "utf-8");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
        conn.setUseCaches(false);

        DataOutputStream output = new DataOutputStream(conn.getOutputStream());
        output.write(postData);
        output.flush();
        output.close();
    }

    // Twitch Api가 인증 관련 오류가 아니면 무조건 200을 응답한다.
    // 예를들어 존재하지 않는 id로 계정 정보를 요청해도 200을 응답한다.
    // 때문에 Http status 체크와, 빈 데이터 체크 둘 다 있어야 한다.
    private JSONObject getResponse(HttpURLConnection conn) throws IOException, RequestException {
        int status = conn.getResponseCode();
        System.out.println(status);
        JSONObject jsonObject;

        if(status == 200) {
            jsonObject = convertResponseToJson(conn.getInputStream());

            if(jsonObject.getJSONArray("data").length() < 1) {
                throw new RequestException("No Content", HttpStatus.resolve(status));
            }

        } else {
            jsonObject = convertResponseToJson(conn.getErrorStream());
            throw new RequestException(jsonObject.getString("message"), HttpStatus.resolve(status));
        }

        return jsonObject;
    }

    // 정상 응답을 json으로 변경하여 리턴
    private JSONObject convertResponseToJson(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String line;
        while((line = br.readLine()) != null) {
            sb.append(line);
        }

        return new JSONObject(sb.toString());
    }

    private URL makeRequestClipsUrl(ClipRequestDto dto) throws MalformedURLException {
        StringBuilder sb = new StringBuilder("https://api.twitch.tv/helix/clips?");
        sb.append("broadcaster_id=").append(dto.getBroadcasterId()).append("&");

        if(dto.getAfter() != null) {
            sb.append("after=").append(dto.getAfter()).append("&");
        } else if(dto.getStartedAt() != null) {
            sb.append("started_at=").append(dto.getStartedAt()).append("&");
        } else if(dto.getEndedAt() != null) {
            sb.append("ended_at=").append(dto.getEndedAt()).append("&");
        } else if(dto.getFirst() != null) {
            sb.append("first=").append(dto.getFirst()).append("&");
        }

        sb.deleteCharAt(sb.length() - 1);
        log.info("url: " + sb);

        return new URL(sb.toString());
    }

    private URL makeRequestUsersUrl(String[] id, String[] name) throws MalformedURLException {
        StringBuilder sb = new StringBuilder();

        if(id != null) {
            for(String s : id) {
                sb.append("id=").append(s).append("&");
            }
        }

        if(name != null) {
            for(String s : name) {
                sb.append("login=").append(s).append("&");
            }
        }

        sb.deleteCharAt(sb.length() - 1);

        return new URL("https://api.twitch.tv/helix/users?" + sb);
    }

}
