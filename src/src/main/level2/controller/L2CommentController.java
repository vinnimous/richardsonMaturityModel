package src.main.level2.controller;

import src.main.model.Comment;
import src.main.model.Post;
import src.main.repository.CommentRepository;
import src.main.repository.PostRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/level2/post/{postId}/comment", consumes = {
    MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
public class L2CommentController {

  @Autowired
  private PostRepository postRepository;

  @Autowired
  private CommentRepository commentRepository;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Comment create(@PathVariable("postId") long postId, @RequestBody Comment comment) {
    Post existedPost = postRepository.findById(postId)
        .orElseThrow(() -> new ResourceNotFoundException("Referred post does not exist"));
    comment.setPost(existedPost);
    return commentRepository.save(comment);
  }

  @PutMapping("{id}")
  public Comment update(@PathVariable("postId") long postId, @PathVariable("id") long id,
      @RequestBody Comment comment) {
    Post existedPost = postRepository.findById(postId)
        .orElseThrow(() -> new ResourceNotFoundException("Referred post does not exist"));
    commentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException());
    comment.setId(id);
    comment.setPost(existedPost);
    return commentRepository.save(comment);
  }

  @DeleteMapping("{id}")
  public Comment delete(@PathVariable("postId") long postId, @PathVariable("id") long id) {
    postRepository.findById(postId)
        .orElseThrow(() -> new ResourceNotFoundException("Referred post does not exist"));
    Comment existedOne = commentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException());
    commentRepository.delete(existedOne);
    return existedOne;
  }

  @GetMapping("{id}")
  public Comment getOne(@PathVariable("postId") long postId, @PathVariable("id") long id) {
    postRepository.findById(postId)
        .orElseThrow(() -> new ResourceNotFoundException("Referred post does not exist"));

    return commentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException());
  }

  @GetMapping
  public List<Comment> getAll(@PathVariable("postId") long postId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new ResourceNotFoundException("Referred post does not exist"));

    return commentRepository.findByPost(post);
  }


}
