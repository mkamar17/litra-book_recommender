package uk.ac.rhul.cs3821.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.rhul.cs3821.service.AppUserDetailsService;
import uk.ac.rhul.cs3821.service.JwtService;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecommendationController.class)
@AutoConfigureMockMvc(addFilters = false) // disables JWT/security filters
class RecommendationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private JwtService jwtService;

  @MockitoBean
  private AppUserDetailsService userDetailsService;

  // should return the static list
  @Test
  void testGetRecommendations() throws Exception {
    mockMvc.perform(get("/recommendations")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(3))
        .andExpect(jsonPath("$[0]").value("Fourth Wing"))
        .andExpect(jsonPath("$[1]").value("The Women"))
        .andExpect(jsonPath("$[2]").value("Where the Crawdads Sing"));
  }

  // ensure no authentication still works
  @Test
  void testGetRecommendations_NoAuthProvided() throws Exception {
    mockMvc.perform(get("/recommendations"))
        .andExpect(status().isOk());
  }
}