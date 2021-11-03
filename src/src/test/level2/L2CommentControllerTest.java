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

import src.main.model.Comment;
import src.main.model.Post;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = { "spring.datasource.url=jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE" })

public class L2CommentControllerTest {

	@Rule
	public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;
	private ObjectMapper objectMapper;
	private final String url = "/api/level2/post/{postId}/comment";

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
		final long postId = insertOnePost();
		Comment one = new Comment();
		one.setContent("Comment's content");
		one.setPost(getOnePost(postId));

		mockMvc.perform(post(url, postId).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(one))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", notNullValue())).andExpect(jsonPath("$.content", is("Comment's content")))
				.andExpect(jsonPath("$.createdAt", notNullValue())).andExpect(jsonPath("$.post.id", is((int) postId)))
				.andDo(document("level2/comment/create"));
	}

	@Test
	public void testUpdate() throws Exception {
		final long existedPostId = insertOnePost();

		final long existedId = insertOneComment(existedPostId);
		final Comment existedOne = getOneComment(existedPostId, existedId);

		existedOne.setContent("Updated content");
		mockMvc.perform(put(url + "/{commentId}", existedPostId, existedId).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(existedOne)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.content", is("Updated content")))
				.andDo(document("level2/comment/update",
						pathParameters(parameterWithName("postId").description("Post's id"),
								parameterWithName("commentId").description("comment's id"))));
	}

	@Test
	public void testDelete() throws Exception {
		final long existedPostId = insertOnePost();
		final long existedCommentId = insertOneComment(existedPostId);

		mockMvc.perform(get(url + "/{commentId}", existedPostId, existedCommentId)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
		mockMvc.perform(delete(url + "/{commentId}", existedPostId, existedCommentId)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andDo(document("level2/comment/delete",
						pathParameters(parameterWithName("postId").description("Post's id"),
								parameterWithName("commentId").description("comment's id"))));
		mockMvc.perform(get(url + "/{commentId}", existedPostId, existedCommentId)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}

	@Test
	public void testGetOne() throws Exception {
		final long existedPostId = insertOnePost();
		final long existedCommentId = insertOneComment(existedPostId);
		mockMvc.perform(get(url + "/{commentId}", existedPostId, existedCommentId)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andDo(document("level2/comment/get",
						pathParameters(parameterWithName("postId").description("Post's id"),
								parameterWithName("commentId").description("comment's id"))));
	}

	@Test
	public void testGetAll() throws Exception {
		final long existedPostId = insertOnePost();
		IntStream.range(0, 5).forEach(value -> {
			try {
				insertOneComment(existedPostId);
			} catch (Exception ex) {

			}
		});
		mockMvc.perform(
				get(url, existedPostId).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(5))).andDo(document("level2/comment/getAll",
						pathParameters(parameterWithName("postId").description("post's id"))));
	}

	private long insertOneComment(final long postId) throws Exception {
		final Post post = getOnePost(postId);
		final Comment newOne = new Comment();
		newOne.setContent("New Comment's conent");
		newOne.setPost(post);
		final Comment insertedOne = objectMapper.readValue(mockMvc
				.perform(post(url, postId).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(newOne)))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), Comment.class);
		return insertedOne.getId();
	}

	private Comment getOneComment(final long postId, final long commentId) throws Exception {
		return objectMapper.readValue(mockMvc
				.perform(get(url + "/{commentId}", postId, commentId).contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), Comment.class);
	}

	private Post getOnePost(final long id) throws Exception {
		return objectMapper.readValue(mockMvc
				.perform(get("/api/level2/post/{id}", id).contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), Post.class);
	}

	private long insertOnePost() throws Exception {
		final Post one = new Post();
		one.setContent("Original Content");
		final Post existedOne = objectMapper.readValue(mockMvc
				.perform(post("/api/level2/post").contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(one)))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), Post.class);
		return existedOne.getId();
	}

}
