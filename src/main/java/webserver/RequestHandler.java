package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.HttpRequestUtils.Pair;
import util.IOUtils;

// 사용자 요청/응답 처리를 담당하는 클래스
public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        // InputStream : 클라이언트 (웹 브라우저) -> 서버로 요청 보낼 때 전달되는 데이터 담당 스트림
        // OutputStream : 서버 -> 클라이언트에 응답 보낼 때 전달되는 데이터 담당 스트림
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String httpRequest = br.readLine();

            // TODO : 아무 값도 없는 null 요청이 왜 오는지 확인
            if (httpRequest == null)
                return;

            String uri = HttpRequestUtils.getUrl(httpRequest);
            String method = HttpRequestUtils.getMethod(httpRequest);
            int bodyLength = 0;

            // 모든 HTTP 요청정보 출력
            while (!httpRequest.isBlank()) {
                log.info(httpRequest);
                Pair header = HttpRequestUtils.parseHeader(httpRequest);
                if (header != null) {
                    String key = header.getKey();
                    log.debug("Header Key : {}", key);
                    if (key.equals("Content-Length") && method.equals("POST") && bodyLength == 0) {
                        log.debug("Header Value : {}", header.getValue());
                        User user = new User();
                        bodyLength = Integer.parseInt(header.getValue());
                    }
                }
                httpRequest = br.readLine();
            }
            log.debug("Body Length : {}", bodyLength);
            String data = IOUtils.readData(br, bodyLength);

            // 2. GET 방식 회원가입
            if (uri.startsWith("/user/create")) {
//                if(method.equals("GET")){
//                    String params = uri.substring(uri.indexOf('?') + 1);
//                    Map<String, String> paramMap = HttpRequestUtils.parseQueryString(params);
//                    User user = User.builder()
//                            .userId(paramMap.get("userId"))
//                            .password(paramMap.get("password"))
//                            .name(paramMap.get("name"))
//                            .email(paramMap.get("email"))
//                            .build();
//                    log.debug("User : {}", user);
//                    uri = "/index.html";
//                }
                Map<String, String> paramMap = HttpRequestUtils.parseQueryString(data);
                User user = User.builder()
                        .userId(paramMap.get("userId"))
                        .password(paramMap.get("password"))
                        .name(paramMap.get("name"))
                        .email(paramMap.get("email"))
                        .build();
                log.debug("User : {}", user);
                uri = "/index.html";
            }

            // TODO : /user/create REDIRECT 해주는 부분 추가하면 0으로 초기화되는 문제 해결 가능
            DataOutputStream dos = new DataOutputStream(out);
            String directoryPath = "./webapp" + uri;
            Path path = Paths.get(directoryPath);
            byte[] body = Files.readAllBytes(path);
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }



    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
