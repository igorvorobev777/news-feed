package com.example.demo.repository;

import com.example.demo.model.Post;
import com.example.demo.model.PostStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
        
        Post post1 = new Post();
        post1.setAuthorId(1L);
        post1.setText("Published post by author 1");
        post1.setStatus(PostStatus.PUBLISHED);
        postRepository.save(post1);

        Post post2 = new Post();
        post2.setAuthorId(1L);
        post2.setText("Created post by author 1");
        post2.setStatus(PostStatus.CREATED);
        postRepository.save(post2);

        Post post3 = new Post();
        post3.setAuthorId(2L);
        post3.setText("Published post by author 2");
        post3.setStatus(PostStatus.PUBLISHED);
        postRepository.save(post3);

        Post post4 = new Post();
        post4.setAuthorId(2L);
        post4.setText("Blocked post by author 2");
        post4.setStatus(PostStatus.BLOCKED);
        postRepository.save(post4);
    }

    @Test
    void findFiltered_shouldReturnPostsByStatusAndAuthor() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Post> result = postRepository.findFiltered(
            PostStatus.PUBLISHED, 1L, null, pageable
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getText()).isEqualTo("Published post by author 1");
    }

    @Test
    void findFiltered_shouldReturnPostsByTextSearch() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Post> result = postRepository.findFiltered(
            null, null, "author 2", pageable
        );

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(p -> p.getText().contains("author 2"));
    }

    @Test
    void findFiltered_shouldReturnEmpty_whenNoMatchingPosts() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Post> result = postRepository.findFiltered(
            PostStatus.BLOCKED, 1L, null, pageable
        );

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findFiltered_shouldApplyAllFilters() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Post> result = postRepository.findFiltered(
            PostStatus.PUBLISHED, 2L, "Published", pageable
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAuthorId()).isEqualTo(2L);
    }

    @Test
    void findFiltered_shouldReturnAll_whenNoFilters() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Post> result = postRepository.findFiltered(null, null, null, pageable);

        assertThat(result.getContent()).hasSize(4);
    }
}