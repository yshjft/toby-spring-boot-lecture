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

### 빈 오브젝의 역할과 구분
* 애플리케이션 빈
  * 개발자가 명시적으로 구성 정보를 제공한 빈
  * 애플리케이션 로직 빈과 인프라스트럭처 빈으로 구분할 수 있다.
* 애플리케이션 로직 빈
  * 비즈니스 로직
  * 컴포넌트 스캔
* 애플리케이션 인프라스트럭처 빈
  * 애플리케이션이 동작하기 위해 필요한 기술 제공
  * 자동 구성 정보(Auto Configuration)
  * by @Configuration
* 컨테이너 인프라스트럭처 빈
  * 컨테이너 자신 또는 컨테이너가 스스로 등록한 빈
 
### 인프라 빈 구성 정보의 분리
* @Import를 이요하여 스캔 대상이 아닌 클래스를 빈으로 등록할 수 있다.
  * 로직과 자동 구성정보를 다른 패키지로 완전 분리
  * @Import 역시 메타 애노테이션

### 동적인 자동 구성 정보 등록
* 동적으로 자동 구성 정보를 다룰 수 있다.
  * 상황을 고려하여 구성 정보의 포함 여부를 고려
  * (현재 강의에서는 EnableMyAutoConfiguration을 고치지 않는 것을 고려)
* ImportSelector라는 인터페이스를 이용하자
  * 스프링 프레임워크에서 지원
  * ImportSelector를 @Import하면 selectImports가 리턴하는 클래스 이름으로 @Configuration 클래스를 찾아서 구성 정보로 사용한다.
  * DefferedImportSelector
    * 유저 구성 정보를 우선적으로 로딩하기 위함이다.

### 자동 구성 정보 파일 분리
* ImportCandidates.load(파일이름, classLoader)
  * META-INF/spring/full-qualified-annotation-name.imports에 있는 내용을 읽어와 String[]로 반환

### 자동 구성 애노테이션 적용
* 인프라 빈 클래스에 @MyAutoConfiguration을 사용한다.
  * @Configuration을 사용한다고 빈 등록이 안되는 것은 아니다. 다만 관례(?)라고 한다.

### @Configuration 클래스 동작 방식
* proxyBeanMethods=true
  * proxy 객체 생성 후 Bean 등록
  * 팩토리 메서드를 통해 매번 새롭게 객체가 만들어지는 것을 방지
  * @Configuration이 붙은 경우 기본적으로 적용
* proxyBeanMethods=false
  * proxy 객체 생성 안함
  * 매번 새로운 객체를 생성하게 된다.
  * @Component와 동일하게 동작
  * Bean 사이 의존관계를 주입할게 아니라면 이 방법을 택하는게 좋음

## 조건부 자동 구성
### 스터디와 Jetty 서버 구성 추가
* AutoConfiguration
  * 애플리케이션이 필요로 하는 빈을 자동으로 만들어 준다.

### @Conditional과 Condition
* @Conditional
  * @Configuration과 @Bean에 사용 가능
  * 만약 @Configuration은 false & @Bean은 true 라면?
    * @Bean까지 고려하지도 않는다. @Configuration이 true여야 @Bean을 고려한다.

### @Conditional 학습테스트
*  metadata.getAnnotationAttributes("애노테이션 이름");
  * 해당 애노테이션의 어트리뷰 값들을 모두 읽어 온다.

### 커스텀 @Conditional
* (참고)  gradle의 의존성에서 원하는 모듈 제거하는 법
  ```
  implementation ('org.springframework.boot:spring-boot-starter-web') {
           include group: 'org.springframework.boot', module: 'spring-boot-starter-tomcat'
  }
  ```

### 자동 구성 정보 대체하기
* 자동 구성으로 등록되는 빈과 동일한 타입의 빈을 @Configuration/@Bean을 이용해서 직접 정의하는 경우 이 빈 구성이 자동 구성을 대체할 수 있다.
* @ConditionalOnMissingBean
  * 동일한 타입의 Bean이 이미 등록되어 있는 경우 Bean을 등록하지 않는다.
  * 강의에서는 사용자 구성 정보로 Bean이 등록되어 있는 경우 자동 구성 정보로 중복된 Bean을 등록하지 않기 위해 사용

### 스프링 부트의 @Conditional
* Class Conditions
  * 지정한 클래스가 프로젝트내 존재하는지 확인해서 포함 여부를 결정한다.
  * 클래스 레벨의 검증 없이 @Bean 메서드에만 적용하면 불필요한 @Configuration 클래스가 빈으로 등록될 수 있으니 클레스 레벨 검증을 먼저 사용해야 한다.
* Bean Conditions
  * 빈의 존재여부를 기준으로 포함 여부를 결정
  * 빈 등록 순서가 중요하다
    * 컨테이너에 등록된 빈 정보를 기준으로 체크한다.
    * 커스텀 빈이 먼저 등록되기 때문에 자동 구성 정보에 대한 빈을 등록할지 안할지 판달할 때 사용은 좋다
    * 다만 반대 상황의 경우 사용을 피해야 한다.
* Property Conditions
  * 프로퍼티 저옵를 이용한다.
    * 지정된 프로퍼티가 존재하고 값이 false가 아니면 포함 대상이다.
* Resource Conditions
  * 파일의 존재를 확인하는 조건
* Web Application Conditions
  * 웹 애플리케이션 여부를 확인
* SpEL Expression Conditions
  * SpEL의 처리 결과를 기주능로 판단
  * 매우 상세한 조건 설정이 가능하다.


## SECTION8
### Environment 추상화와 프로퍼티
* 자동 구성은 왜 필요
  * 스프링 부트가 우리를 대신하여 인프라스트럭처 빈의 Configuration 클래스를 미리 만들어 놓았다.
  * 개발자는 이걸 그냥 사용하면 된다.

### 자동 구성에 Environment 프로퍼티 적용
* ApplicationRunner
  * functional interface
  * 해당 인터페이스를 구현하여 빈으로 등록하면 모든 Spring Boot 초기화 작업 이후 해당 ApplicationRunner을 구현한 오브젝트들을 Run 메서드를 통해 실행
  * 빈이 다 등록되면 특정 기능이 자동으로 수행되게 하기 위해 사용
* 우선순위
  * System Property > 환경 변수 > application.properties 

### @Value와 PropertySourcesPlaceholderConfigurer
* 치환자(${})를 그냥 사용할 수 있는게 아니라 추갖거인 처리(빈 등록)가 필요하다.
  * PropertySourcesPlaceholderConfigurer 타입의 빈 등록 필요

### 프로퍼티 클래스의 원리
* 클래스 필드에 프로퍼티 정의
  * 프로퍼티가 굉장히 많을 수 있는데 Config 클래스가 프로퍼티 값으로 가득 채워야하는 문제
  * 특정 프로퍼티들은 하나 이상의 Config에서 재사용할 수 있어야 하는데 필드에 집어 넣으면 재사용하기 힘들어진다.
* Spring Boot에서는 프로퍼티 값들을 찾아 놓은 클래스들이 있고 이를 자동 구성 클래스에서 주입 받아 사용한다.

### 프로퍼티 빈의 후처리기 도입
* BeanPostProcessor를 만들어서 빈 오브젝트 생성 후에 후처리 작업을 진행시킬 수 있다.
  * 프로퍼티 빈을 만드는 코드를 하나하나 만들 필요가 없다.
* prefix를 이용하여 namespace처럼 사용한다.
* (명심하자 지금 강의에서 만들어보고 있는 것은 Spring Boot에서 이미 만들어져 사용되고 있는 기능들이다.)