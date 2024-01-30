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

            if (httpRequest == null)
                return;

            String[] httpRequestInfo = httpRequest.split(" ");
            String method = httpRequestInfo[0];
            String uri = httpRequestInfo[1];

            // 요구사항 1 : 모든 HTTP 요청정보 출력
            while (!httpRequest.isEmpty()) {
                log.info(httpRequest);
                httpRequest = br.readLine();
            }

            if (method.equals("/user/form.html")) {
                if (uri.contains("?")) {
                    String params = uri.substring(uri.indexOf('?') + 1);
                    Map<String, String> paramMap = HttpRequestUtils.parseQueryString(params);
                    User user = User.builder()
                            .userId(paramMap.get("userId"))
                            .password(paramMap.get("password"))
                            .name(paramMap.get("name"))
                            .email(paramMap.get("email"))
                            .build();
                }
            }

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
