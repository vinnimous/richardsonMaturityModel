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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import src.main.level1.controller.RequestWrapper;
import src.main.model.Post;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = { "spring.datasource.url=jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE" })
public class PostControllerTest {

	@Rule
	public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;
	private ObjectMapper objectMapper;
	private String url = "/api/level1/post";

	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(documentationConfiguration(this.restDocumentation)
				.operationPreprocessors().withRequestDefaults(prettyPrint()).withResponseDefaults(prettyPrint()))
				.build();

		objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		JdbcTestUtils.deleteFromTables(jdbcTemplate, "COMMENT", "POST");
	}

	@Test
	public void testCreatePost() throws Exception {

		final Post post = new Post();
		post.setContent("Test Post 123");
		final RequestWrapper<Post> requestWrapper = new RequestWrapper<>();
		requestWrapper.setCommand("create");
		requestWrapper.setData(post);

		mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestWrapper))).andExpect(status().isOk())
				.andExpect(jsonPath("$.status", is("SUCCESS"))).andExpect(jsonPath("$.data.id", notNullValue()))
				.andExpect(jsonPath("$.data.content", is("Test Post 123")))
				.andExpect(jsonPath("$.data.createdAt", notNullValue()))
				.andDo(document("level1/post/create",
						requestFields(
								fieldWithPath("command").type(JsonFieldType.STRING).description("must be 'createPost'"),
								subsectionWithPath("data").type("Post").description("post")),
						responseFields(fieldWithPath("status").type(JsonFieldType.STRING).description("result status"),
								fieldWithPath("message").type(JsonFieldType.STRING).optional()
										.description("error message if failed"),
								subsectionWithPath("data").type("Post").description("post"))));
	}

}
