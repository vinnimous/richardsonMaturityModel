package src.test.level2;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import src.main.model.Post;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = { "spring.datasource.url=jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE" })
public class L2PostControllerTest {

	@Rule
	public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;
	private ObjectMapper objectMapper;
	private final String url = "/api/level2/post";

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
	public void testCreate() throws Exception {
		final Post newOne = new Post();
		newOne.setContent("New Post content");

		mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(newOne))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", notNullValue())).andExpect(jsonPath("$.content", is("New Post content")))
				.andExpect(jsonPath("$.createdAt", notNullValue())).andDo(document("level2/post/create"));
	}

	@Test
	public void testUpdate() throws Exception {
		final long existedId = insertOne();
		final Post updateOne = getOne(existedId);

		updateOne.setContent("Updated Content");
		mockMvc.perform(put(url + "/{id}", existedId).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateOne)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.content", is("Updated Content"))).andDo(document(
						"level2/post/update", pathParameters(parameterWithName("id").description("post's id"))));
	}

	@Test
	public void testDelete() throws Exception {
		final long existedId = insertOne();
		mockMvc.perform(get(url + "/{id}", existedId).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
		mockMvc.perform(delete(url + "/{id}", existedId).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andDo(document("level2/post/delete",
						pathParameters(parameterWithName("id").description("post's id"))));
		mockMvc.perform(get(url + "/{id}", existedId).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
	}

	@Test
	public void testGet() throws Exception {
		final long existedId = insertOne();
		mockMvc.perform(get(url + "/{id}", existedId).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.id", notNullValue())).andExpect(jsonPath("$.content", notNullValue()))
				.andExpect(jsonPath("$.createdAt", notNullValue()))
				.andDo(document("level2/post/get", pathParameters(parameterWithName("id").description("post's id"))));
	}

	@Test
	public void testGetAll() throws Exception {
		IntStream.range(0, 5).forEach(value -> {
			try {
				insertOne();
				return;
			} catch (Exception ex) {

			}
		});

		mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(5))).andDo(document("level2/post/getAll"));
	}

	private Post getOne(final long id) throws Exception {
		return objectMapper.readValue(mockMvc
				.perform(get(url + "/{id}", id).contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), Post.class);
	}

	private long insertOne() throws Exception {
		final Post one = new Post();
		one.setContent("Original Content");
		final Post existedOne = objectMapper.readValue(mockMvc
				.perform(post(url).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(one)))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), Post.class);
		return existedOne.getId();
	}

}
