package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            String filePath = "";
            String str = "";
            while((str = br.readLine()) != null) {
                System.out.println(str);
                String[] tokens = str.split(" ");
                if(tokens[0].equals("GET")) {
                    filePath = tokens[1];
                    if(filePath.contains("?")) {
                        String[] queryStringList = filePath.substring(filePath.indexOf('?')+1).split("&");
                        User user = new User();
                        for(String queryString : queryStringList) {
                            String[] queryStr = queryString.split("=");
                            String key = queryStr[0];
                            String value = queryStr[1];
                            if(key.equals("userId"))
                                user.setUserId(value);
                            else if(key.equals("password"))
                                user.setPassword(value);
                            else if(key.equals("name"))
                                user.setName(value);
                            if(key.equals("email"))
                                user.setEmail(value);
                        }
                        System.out.println(user);
                    }
                    break;
                }
            }

            DataOutputStream dos = new DataOutputStream(out);
            String directoryPath = "./webapp" + filePath;
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
