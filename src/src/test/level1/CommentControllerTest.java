package src.test.level1;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import src.main.level1.controller.RequestWrapper;
import src.main.model.Comment;
import src.main.model.Post;

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
	private String url = "/api/level1/comment";

	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(documentationConfiguration(this.restDocumentation)
				.operationPreprocessors().withRequestDefaults(prettyPrint()).withResponseDefaults(prettyPrint()))
				.build();

		objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	@Test
	public void testCreatePost() throws Exception {

		final Post post = new Post();
		post.setId(1L);

		final Comment comment = new Comment();
		comment.setContent("Test Comment 123");
		comment.setPost(post);
		final RequestWrapper<Comment> requestWrapper = new RequestWrapper<>();
		requestWrapper.setCommand("create");
		requestWrapper.setData(comment);

		mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestWrapper))).andExpect(status().isOk())
				.andExpect(jsonPath("$.status", is("SUCCESS"))).andExpect(jsonPath("$.data.id", notNullValue()))
				.andExpect(jsonPath("$.data.content", is("Test Comment 123")))
				.andExpect(jsonPath("$.data.createdAt", notNullValue())).andExpect(jsonPath("$.data.post.id", is(1)))
				.andDo(document("level1/comment/create",
						requestFields(
								fieldWithPath("command").type(JsonFieldType.STRING)
										.description("must be 'createComment'"),
								subsectionWithPath("data").type("Comment").description("comment")),
						responseFields(fieldWithPath("status").type(JsonFieldType.STRING).description("result status"),
								fieldWithPath("message").type(JsonFieldType.STRING).optional()
										.description("error message if failed"),
								subsectionWithPath("data").type("Comment").description("comment"))));
	}
}
