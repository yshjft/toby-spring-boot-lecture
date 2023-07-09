package tobyspring.helloboot;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HelloControllerTest {
    @Test
    void helloController() {
        HelloController helloController  = new HelloController(name -> name);

        String test = helloController.hello("Test");

        assertThat(test).isEqualTo("Test");
    }

    @Test
    void failHelloController() {
        HelloController helloController  = new HelloController(name -> name);

        Assertions.assertThatThrownBy(() -> {
            helloController.hello(null);
        }).isInstanceOf(IllegalArgumentException.class);


        Assertions.assertThatThrownBy(() -> {
            helloController.hello("");
        }).isInstanceOf(IllegalArgumentException.class);
    }
}