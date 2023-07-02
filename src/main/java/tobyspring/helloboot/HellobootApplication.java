package tobyspring.helloboot;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.support.GenericApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

public class HellobootApplication {

	public static void main(String[] args) {
		// 스프링 컨테이너 생성
		GenericApplicationContext applicationContext = new GenericApplicationContext();
		// bean 등록
		applicationContext.registerBean(HelloController.class);
		// 컨테이너 초기화 작업
		applicationContext.refresh();


		// 임베디드 Tomcat(서블릿 컨테이너)을 직접 띄워보자!
		TomcatServletWebServerFactory serverFactory = new TomcatServletWebServerFactory();

		// WebServer : 여러 웹서버를 사용할 수 있도록 추상화함
		// ServletContextInitializer를 통해 컨테이너에 서블릿을 등록하자!
		WebServer webServer = serverFactory.getWebServer(servletContext -> {
			// servlet을 추가
			// HttpServlet: 일종의 adapter 클래스
			// addMapping을 통해 요청이 서블릿과 매핑될 수 있도록 한다.
			servletContext.addServlet("frontcontroller", new HttpServlet() {
				@Override
				protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
					// frontcontroller(1) : 인증, 보안, 다국어, 공통기능을 처리

					// frontcontroller(2) : 요청에 따라 서블릿을 매핑
					if(req.getRequestURI().equals("/hello") && req.getMethod().equals(GET.name())) {
						// 요청 쿼리 파라미터 가져오기
						String name = req.getParameter("name");

						// 서블릿 컨테이너는 HelloController라는 타입의 오브젝트가 어떻게 만들어 졌는지 신경쓰지 않습니다.
						// 빈 가져오기
						HelloController helloController = applicationContext.getBean(HelloController.class);
						// 로직 수행
						String ret = helloController.hello(name);

						// 응답을 만든다.
						resp.setContentType(TEXT_PLAIN_VALUE);
						resp.getWriter().println(ret);
					}else {
						resp.setStatus(NOT_FOUND.value());
					}
				}
			}).addMapping("/*");
		});

		// Tomcat Servlet Container 동작
		webServer.start();
	}
}
