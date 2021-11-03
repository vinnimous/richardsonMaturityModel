package src.test.level0;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"})
public class PostControllerTest {

  @Rule
  public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

  @Autowired
  private WebApplicationContext context;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;
  private String url = "/api/level0/operation";

  @Before
  public void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
        .apply(documentationConfiguration(this.restDocumentation).operationPreprocessors()
            .withRequestDefaults(prettyPrint()).withResponseDefaults(
                prettyPrint())).build();
    objectMapper = new ObjectMapper();
  }

  @Test
  public void testCreatePost() throws Exception {

    final String input = "{\"command\":\"createPost\",\"parameters\":{\"content\":\"Test post 123\"}}";

    mockMvc.perform(
        post(url).contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON)
            .content(input)).andExpect(status().isOk()).andDo(print())
        .andExpect(jsonPath("$.status", is("SUCCESS")))
        .andExpect(jsonPath("$.parameters.id", notNullValue()))
        .andExpect(jsonPath("$.parameters.content", is("Test post 123")))
        .andExpect(jsonPath("$.parameters.createdAt", notNullValue()))
        .andDo(document("level0/create-post", requestFields(
            fieldWithPath("command").type(JsonFieldType.STRING).description("must be 'createPost'"),
            fieldWithPath("parameters.content").type(JsonFieldType.STRING)
                .description("content of new post")), responseFields(
            fieldWithPath("status").type(JsonFieldType.STRING).description("result status"),
            fieldWithPath("message").type(JsonFieldType.STRING)
                .description("error message if failed"),
            fieldWithPath("parameters.id").type(JsonFieldType.NUMBER)
                .description("Id of created post"),
            fieldWithPath("parameters.content").type(JsonFieldType.STRING)
                .description("content of created post"),
            fieldWithPath("parameters.createdAt").type("Date")
                .description("create timestamp of post"))));
  }

  @Test
  public void testHeaderExample() throws Exception {
    final String input = "{\"command\":\"createPost\",\"parameters\":{\"content\":\"Test post 123\"}}";

    mockMvc.perform(
        post(url).contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON)
            .content(input)).andExpect(status().isOk()).andDo(print())
        .andDo(document("level0/headers-example", requestHeaders(
            headerWithName("Content-Type").description("data form of request payload"),
            headerWithName("accept").description("data form which client accept")), responseHeaders(
            headerWithName("Content-Type").description("data form of response payload"))));
  }
}
