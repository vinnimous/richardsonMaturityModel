package src.test.level0;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.aspectj.lang.annotation.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = { "spring.datasource.url=jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE" })
@SqlGroup({
		@Sql(scripts = "/src/test/resources/level0/CommentTest.before.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD) })
public class CommentControllerTest {

	@Rule
	public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();
	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;
	private ObjectMapper objectMapper;
	private final String url = "/api/level0/operation";

	@Before(value = "")
	public void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(documentationConfiguration(this.restDocumentation)
				.operationPreprocessors().withRequestDefaults(prettyPrint()).withResponseDefaults(prettyPrint()))
				.build();
		objectMapper = new ObjectMapper();
	}

	@Test
	public void testCreateComment() throws Exception {
		final String input = "{\"command\":\"createComment\",\"parameters\":{\"postId\":1,\"content\":\"comment 1\"}}";
		mockMvc.perform(
				post(url).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(input))
				.andExpect(status().isOk()).andExpect(jsonPath("$.status", is("SUCCESS")))
				.andExpect(jsonPath("$.parameters.postId", is(1)))
				.andExpect(jsonPath("$.parameters.id", notNullValue()))
				.andExpect(jsonPath("$.parameters.content", is("comment 1")))
				.andExpect(jsonPath("$.parameters.createdAt", notNullValue()))
				.andDo(document("level0/create-comment", requestFields(
						fieldWithPath("command").type(JsonFieldType.STRING).description("must be 'createComment'"),
						fieldWithPath("parameters.content").type(JsonFieldType.STRING)
								.description("content of new comment"),
						fieldWithPath("parameters.postId").type(JsonFieldType.NUMBER).description("post's id")),
						responseFields(fieldWithPath("status").type(JsonFieldType.STRING).description("result status"),
								fieldWithPath("message").type(JsonFieldType.STRING)
										.description("error message if failed"),
								fieldWithPath("parameters.id").type(JsonFieldType.NUMBER)
										.description("Id of created comment"),
								fieldWithPath("parameters.content").type(JsonFieldType.STRING)
										.description("content of created comment"),
								fieldWithPath("parameters.createdAt").type("Date")
										.description("create timestamp of comment"),
								fieldWithPath("parameters.postId").type(JsonFieldType.NUMBER)
										.description("post's id"))));
	}
}
