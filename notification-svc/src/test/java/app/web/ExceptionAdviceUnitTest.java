package app.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExceptionAdvice.class)
public class ExceptionAdviceUnitTest {

    @Autowired
    private MockMvc mockMvc;

    // Test 1: Test handling of NoResourceFoundException
    @Test
    void handleNotFoundEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.message").value("Not supported application endpoint."));
    }


}
