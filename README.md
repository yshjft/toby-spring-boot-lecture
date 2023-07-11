# toby-spring-boot-lecture
토비의 스프링 부트 강의 코드 레파지토리

## SECTION 3 독립 실행형 서블릿 애플리케이션

### 프론트 컨트롤러
* 반복적으로 등장하게 되는 공통 작업을 하나의 오브젝트에서 일괄적으로 처리하게 만드는 방식

### 매핑과 바인딩
* 매핑: HTTP 요청을 처리할 핸들러를 결정하고 연동하는 작업
* 바인딩: 웹 요청 정보를 추출하고 의미있는 오브젝트에 담아서 전달하는 작업


## SECTION 4 독립 실행형 스프링 어플리케이션

### 스프링 컨테이너 사용
* 스프링 컨테이너는 POJO와 구성 정보를 런타임에 조합해서 최종 애플리케이션을 만들어 낸다.
* POJO : Business Object, 다른 외부 클래스나 라이브러리에 의존성이 전혀 없다.
* 서블릿 컨테이너는 HelloController라는 타입의 오브젝트(Bean)가 어떻게 만들어 졌는지 신경쓰지 않는다.
  * 스프링 컨테이너는 서블릿 컨테이너와 다르게 직접 객체를 만들어서 넣어줄 필요가 없다.

### 의존 오브젝트 추가
* 기존 서블 컨테이너만 있는 구조에서 스프링 컨테이너가 추가되어서 가지는 이점은 도대체 무엇일까?
  * 스프링 컨테이너가 할 수 있는 일들을 이후에 적용 가능한 기본 구조를 만들어 놓았다는 것이 의미가 있는 것이다.
* 스프링 컨테이너 = 싱글톤 레지스트리
  * 딱 하나의 오브젝트만 만들고 사용되게 만들어준다.
  * 싱글톤 패턴을 사용하지 않고도 마치 싱글톤 패턴을 쓰는 것처럼 해준다.
* controller의 중요 역할 중 하나는 사용자의 요청 검증


### Dependency Injection
* DI에는 어셈블러가 필요하다.
* **어셈블러**
  * 두 개의 오브젝트가 동적으로 의존 관계를 가지는 것을 도와주는 제 3의 존재
  * 스프링 컨테이너가 어셈블러의 역할을 한다.
* bean 등록 (DI는 스프링 컨테이너가 알아서 잘 해줄겁니다 ~^^~)
```java
applicationContext.registerBean(HelloController.class);
applicationContext.registerBean(SimpleHelloService.class);
```

### DispatcherServlet으로 전환
* DispatcherServlet은 매핑 및 바인딩 작업을 직접 구현할 필요가 없게 만들어준다.
* 서블릿 컨테이너가 초기화 되는 과정을 스프링 컨테이너가 초기화 되는 과정 중에 일어나도록 만들자. onRefresh가 호출되는 시점에 서블릿 컨테이너를 초기화힌디.
```java
GenericWebApplicationContext applicationContext = new GenericWebApplicationContext(){
    @Override
    protected void onRefresh() {
        super.onRefresh();
        
        TomcatServletWebServerFactory serverFactory = new TomcatServletWebServerFactory();
        
        WebServer webServer = serverFactory.getWebServer(servletContext -> {
            servletContext.addServlet("dispatcherServlet",new DispatcherServlet(this))
                    .addMapping("/*");
        });
        
        webServer.start();
    `}
};
```

### 자바 코드 구성 정보 사용
* 왜 팩토리 메서드를 사용하나?
  * 팩토리 메서드 방식 = ```@Configuration``` + ```@Bean```
  * 왜 구지 bean 객체를 직접 생성해서 주입하려고 하나.. → bean을 만들고 초기화하는 작업이 복잡한 경우가 있다 → 자바 코드로 간결하게 만들자
  ```java
  @Configuration
  public class HellobootApplication {
    @Bean
    public HelloController helloController(HelloService helloService) {
        return new HelloController(helloService);
    }
    @Bean
    public HelloService helloService() {
        return new SimpleHelloService();
    }
  }
  ```
* Configuration 클래스
  * 구성 정보 클래스
  * 쉽게 말해 빈 팩토리 메서드가 있는 클래스
  * 빈 구성 정보가 담겨 있는 클래스를 의미한다.
* GenericWebApplicationContext는 자바 코드로 만든 configuration 정보를 읽을 수가 없다. 따라서 AnnotationConfigWebApplicationContext로 교체한다.
  * applicationContext에 사용하려는 클래스를 모두 등록하는 대신 Configuration 클래스를 등록한다.
    ```java
    applicationContext.register(HellobootApplication.class);
    ```
* ```@Configuration```이 붙은 클래스가 ApplicationContext에 처음으로 등록된다.


### @Component 스캔
* @ComponentScan + @Component
  * 아주 간단하게 빈을 등록하고 사용할 수 있는 방법
  * 단 빈이 너무 많아지게 되면 어떠한 빈들이 등록되는지 확인하기 어렵다는 단점이 있다. → 다만 극복할 수 있는 단점이기에 거의 표준적인 방식으로 사용되고 있다.
* 메타 애노테이션
  * 애노테이션 위의 애노테이션
  * 쉽게 말해 @Controller, @Service 같은 애노테이션들을 의미한다.
  * 메타 에노테이션은 여러 단계로 중첩되기도 한다. ex) RestController

### Bean 생명주기 메서드
* TomcatServletWebServerFactory와 DispatcherServlet을 Bean으로 등록하자
* DispatcherServlet을 단순히 Bean으로 등록만 해도  스프링 컨테이너가 알아서 applicationContext를 주입한다.
  * DispatcherServlet은 ApplicationContextAware라는 인터페이스를 구현하고 있다.
  * ApplicationContextAware를 구현한 클래스가 Bean으로 등록되면 스프링 컨테이너는 해당 인터페이스의 setter메서드를 통해 ApplicationContext를 주입한다.
  * 해당 메서드는 스프링 컨테이너가 초기화되는 시점에 작업이 일어난다.
* ApplicationContext 오브젝트는 스프링 컨테이너 입장에서는 자기 자신이기도 하지만 Bean으로 취급한다.


## SECTION 5 DI와 테스트, 디자인 패턴

### 단위 테스트
* 빠르고 간결하게 하는 테스트
* 고립시켜서 하는 테스트 
* stub도 일종의 DI로 볼 수 있다.

### DI를 이용한 Decoration 패턴과 Proxy 패턴
* autowiring : Bean 주입시 단일 주입 후보를 찾게 되면 해당 Bean을 주입하게 된다.
* Decorator : 여러가지 기능과 책임을 부가적으로 주는 객체
* @Primary : 단일 주입 후보가 아닌 경우 주입될 Bean의 우선순위 설정. 다만 우선순위를 설정할 Bean이 여러개가 되면 복잡해지므로 자바 코드를 이용하여 명시적으로 정의해주는 것이 더 좋은 방법일 수 있다.
* Proxy
  * Lazy Loading
    * 실제 사용할 객체의 비용이 너무 비싸서 객체의 생성을 최대한 미룰 때 사용할 수 있다.
  * Remote Access
    * 사용자 측에서 API 호출 등의 영향을 받지 않도록 Proxy가 대리자의 역할을 한다.

## SECTION 6 자도 구성 기반 애플리케이션
### 메타 애노테이션
* 애노테이션에 적용한 애노테이션
* ex) @Component
* 동일한 기능을 하면서 추가적인 정보와 기능 제공
* (질문) @Target에 ElementType.ANNOTATION_TYPE이 포함되지 않는데 어떻게 메타 에노테이션으로 사용할 수 있는 걸까? ex) @Component,...

### 합성 애노테이션
* 메타 애노테이션이 하나 이상 적용된 애노테이션
* ex) RestController = ResponseBody + Controller

### 합성 애노테이션 적용
* @Retention
  * default 값은 CLASS
  * 어노테이션 정보가 컴파일된 class 파일까지는 살아 있지만 런타임에는 사라지게 된다.
  * 따라서 Runtime에 정보가 유지돌 수 있도록 RUNTIME으로 변경해야 한다.
* @Target
  * ElementType.TYPE: class, interface, enum