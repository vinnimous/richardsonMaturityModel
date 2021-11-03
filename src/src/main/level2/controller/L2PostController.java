package src.main.level2.controller;

import src.main.model.Post;
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
@RequestMapping(value = "/api/level2/post", consumes = {
    MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
public class L2PostController {

  @Autowired
  private PostRepository postRepository;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Post create(@RequestBody Post post) {
    return postRepository.save(post);
  }

  @PutMapping("{id}")
  public Post update(@PathVariable("id") long id, @RequestBody Post post) {
    if (!postRepository.existsById(id)) {
      throw new ResourceNotFoundException();
    }
    post.setId(id);
    return postRepository.save(post);
  }

  @DeleteMapping("{id}")
  public Post delete(@PathVariable("id") long id) {

    Post existedOne = postRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException());
    postRepository.delete(existedOne);
    return existedOne;
  }

  @GetMapping("{id}")
  public Post getOne(@PathVariable("id") long id) {
    return postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException());
  }

  @GetMapping
  public List<Post> getAll() {
    return postRepository.findAll();
  }
}
