package tobyspring.helloboot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(HelloController.class)
class HelloControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Test
    void testHello() throws Exception{
        mockMvc.perform(get("/hello?name=jerry"))
                .andExpect(content().string("hello jerry"));
    }
}