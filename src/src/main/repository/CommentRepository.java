package src.main.repository;

import src.main.model.Comment;
import src.main.model.Post;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

  List<Comment> findByPost(final Post post);
}
