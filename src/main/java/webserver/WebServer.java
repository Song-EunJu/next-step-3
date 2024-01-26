package webserver;

import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebServer {
    private static final Logger log = LoggerFactory.getLogger(WebServer.class);
    private static final int DEFAULT_PORT = 8080;

    public static void main(String args[]) throws Exception {
        int port = 0;
        if (args == null || args.length == 0) {
            port = DEFAULT_PORT;
        } else {
            port = Integer.parseInt(args[0]);
        }

        // 서버소켓 생성, 웹서버는 기본적으로 8080번 포트를 사용함
        try (ServerSocket listenSocket = new ServerSocket(port)) {
            log.info("Web Application Server started {} port.", port);

            // accept()가 호출되면 프로그램은 여기서 실행을 멈추고 클라이언트 포크가 8080번으로 연결할 때까지 무한 대기
            // 클라이언트가 연결되면 Socket 객체 반환하고,
            // Socket 을 RequestHandler 에 전달하면서 새로운 스레드를 실행하는 방식으로 멀티스레드 프로그래밍 지원
            // 사용자 요청 발생할 때까지 대기 상태에 있도록 지원 + 사용자 요청 들어오면 RequestHandler 클래스에 위임하는 역할 -> ServerSocket
            Socket connection;
            while ((connection = listenSocket.accept()) != null) {
                RequestHandler requestHandler = new RequestHandler(connection);
                requestHandler.start();
            }
        }
    }
}
