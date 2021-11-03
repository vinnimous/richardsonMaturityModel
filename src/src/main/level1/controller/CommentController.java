package src.main.level1.controller;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import src.main.model.Comment;
import src.main.model.Post;
import src.main.repository.CommentRepository;
import src.main.repository.PostRepository;

@RestController
@RequestMapping("/api/level1/comment")
public class CommentController {

	private static final Log LOG = LogFactory.getLog(CommentController.class);
	private ObjectMapper objectMapper;

	@Autowired
	private PostRepository postRepository;
	@Autowired
	private CommentRepository commentRepository;

	public CommentController() {
		this.objectMapper = new ObjectMapper();
		this.objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
		this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	@SuppressWarnings("deprecation")
	@PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE }, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseWrapper<Comment> doOperation(HttpServletRequest request) {
		try {
			final String payload = IOUtils.toString(request.getInputStream(), Charset.forName("UTF-8"));

			final RequestWrapper<Comment> requestWrapper = objectMapper.readValue(payload,
					new TypeReference<RequestWrapper<Comment>>() {
					});
			if ("create".equals(requestWrapper.getCommand())) {
				return doCreate(requestWrapper);
			}

			return ResponseWrapper.error(String.format("Unknown command %s", requestWrapper.getCommand()));
		} catch (IOException ex) {
			LOG.error(ex.getMessage(), ex);
			return ResponseWrapper.error(ex.getMessage());
		}
	}

	private ResponseWrapper<Comment> doCreate(RequestWrapper<Comment> requestWrapper) {
		final Comment newOne = requestWrapper.getData();
		Optional<Post> existedPost = postRepository.findById(newOne.getPost().getId());
		if (existedPost.isPresent()) {
			newOne.setPost(existedPost.get());
			Comment savedOne = commentRepository.save(newOne);

			return ResponseWrapper.success(savedOne);
		} else {
			return ResponseWrapper.error("Referred post does not exist");
		}
	}
}
