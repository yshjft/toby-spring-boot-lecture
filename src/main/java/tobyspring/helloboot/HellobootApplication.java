package tobyspring.helloboot;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HellobootApplication {

	public static void main(String[] args) {
		// 임베디드 Tomcat(서블릿 컨테이너)을 직접 띄워보자!
		TomcatServletWebServerFactory serverFactory = new TomcatServletWebServerFactory();

		// WebServer : 여러 웹서버를 사용할 수 있도록 추상화함
		// ServletContextInitializer를 통해 컨테이너에 서블릿을 등록하자!
		WebServer webServer = serverFactory.getWebServer(servletContext -> {
			// servlet을 추가
			// HttpServlet: 일종의 adapter 클래스
			// addMapping을 통해 요청이 서블릿과 매핑될 수 있도록 한다.
			servletContext.addServlet("hello", new HttpServlet() {
				@Override
				protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
					// 응답을 만든다.
					resp.setStatus(200);
					resp.setHeader("Content-Type", "text/plain");
					resp.getWriter().print("Hello Servlet");
				}
			}).addMapping("/hello");
		});

		// Tomcat Servlet Container 동작
		webServer.start();
	}
}
