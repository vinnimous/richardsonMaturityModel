package src.main.level0.controller;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import src.main.model.Comment;
import src.main.model.Post;
import src.main.repository.CommentRepository;
import src.main.repository.PostRepository;

@Component
public class CreateCommentHandler extends AbstractHandler {

	private static final Log LOG = LogFactory.getLog(CreateCommentHandler.class);
	@Autowired
	private PostRepository postRepository;
	@Autowired
	private CommentRepository commentRepository;

	@Override
	protected String command() {
		return "createComment";
	}

	@Override
	public String handle(String input) {
		try {
			final JsonNode jsonTree = mapper.readTree(input);
			long postId = jsonTree.at("/parameters/postId").asLong(0L);
			String content = jsonTree.at("/parameters/content").asText("");
			@SuppressWarnings("deprecation")
			Post post = postRepository.getOne(postId);
			Comment newOne = new Comment();
			newOne.setContent(content);
			newOne.setPost(post);

			Comment savedOne = commentRepository.save(newOne);

			return String.format(
					"{\"status\":\"SUCCESS\",\"message\":\"\",\"parameters\":{\"id\":%d,\"content\":\"%s\",\"createdAt\":\"%s\",\"postId\":%d}}",
					savedOne.getId(), savedOne.getContent(), savedOne.getCreatedAt().toString(),
					savedOne.getPost().getId());
		} catch (IOException ex) {
			LOG.warn(ex.getMessage(), ex);
			return String.format("{\"status\":\"ERROR\",\"message\":\"%s\"}", ex.getMessage());

		}

	}
}
