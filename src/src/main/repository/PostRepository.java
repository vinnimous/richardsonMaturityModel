package src.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import src.main.model.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

}
