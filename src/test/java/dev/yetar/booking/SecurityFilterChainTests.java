package dev.yetar.booking;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
public class SecurityFilterChainTests {

    @Autowired
    private MockMvc mvc;

    @ParameterizedTest
    @ValueSource(strings = { "/actuator/health", "/actuator/health/liveness" })
    void permitActuatorHealthRequests(String endpoint) throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(endpoint))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = { "/", "/actuator", "/actuator/info" })
    void denyNonActuatorHealthRequests(String endpoint) throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(endpoint))
                .andExpect(MockMvcResultMatchers.status().isForbidden()); // Becomes Unauthorized in M2
    }
}
