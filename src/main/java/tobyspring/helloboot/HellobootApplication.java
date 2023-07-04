package tobyspring.helloboot;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class HellobootApplication {

	public static void main(String[] args) {
		// 스프링 컨테이너 생성
		GenericWebApplicationContext applicationContext = new GenericWebApplicationContext(){
			// 서블릿 컨테이너가 초기화 되는 과정을 스프링 컨테이너가 초기화 되는 과정 중에 일어나도록 만들자. onRefresh가 호출되는 시점에 서블릿 컨테이너를 초기화힌디.
			@Override
			protected void onRefresh() {
				super.onRefresh();

				// 임베디드 Tomcat(서블릿 컨테이너)을 직접 띄워보자!
				TomcatServletWebServerFactory serverFactory = new TomcatServletWebServerFactory();

				// WebServer : 여러 웹서버를 사용할 수 있도록 추상화함
				// ServletContextInitializer를 통해 컨테이너에 서블릿을 등록하자!
				WebServer webServer = serverFactory.getWebServer(servletContext -> {
					// servlet을 추가
					// HttpServlet: 일종의 adapter 클래스
					// addMapping을 통해 요청이 서블릿과 매핑될 수 있도록 한다.
					servletContext.addServlet("dispatcherServlet",
										new DispatcherServlet(this))
							.addMapping("/*");
				});

				// Tomcat Servlet Container 동작
				webServer.start();
			}
		};

		// bean 등록 (DI는 스프링 컨테이너가 알아서 잘 해줄겁니다 ~^^~)
		applicationContext.registerBean(HelloController.class);
		applicationContext.registerBean(SimpleHelloService.class);
		// 스프링 컨테이너 초기화 작업
		applicationContext.refresh();
	}
}
