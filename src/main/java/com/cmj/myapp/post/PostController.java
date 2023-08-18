package com.cmj.myapp.post;

import com.cmj.myapp.auth.Auth;
import com.cmj.myapp.auth.AuthProfile;
import com.cmj.myapp.auth.entity.Profile;
import com.cmj.myapp.auth.entity.ProfileRepository;
import com.cmj.myapp.auth.entity.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping(value = "/posts")
public class PostController {
    Map<String, Post> map = new ConcurrentHashMap<>();
    @Autowired
    PostRepository postRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ProfileRepository profileRepository;

//    @GetMapping
//    public List<Post> getPostList() {
//        List<Post> list = postRepository.findAll(Sort.by("no").ascending());
//        return list;
//    }

    @GetMapping(value = "/paging")
    public Page<Post> getPostsPaging(@RequestParam int page, @RequestParam int size) {
        System.out.println("첫화면: " + page + ", " + size + "/");

        Sort sort = Sort.by("no").descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        // PageRequest.of(페이지 번호, 페이지 크기, 정렬정보).descending(내림차순)/ascending(오름차순)

        return postRepository.findAll(pageRequest);
    }

    @GetMapping(value = "/paging/searchByCreatorName")
    public Page<Post> getPostsPagingSearchCreatorName(
            @RequestParam int page, @RequestParam int size, @RequestParam String creatorName) {
        System.out.println("작성자: " + page + ", " + size + ", " + creatorName+ "/");

        Sort sort = Sort.by("no").descending();
        PageRequest pageRequest =  PageRequest.of(page, size, sort);
        return postRepository.findByCreatorNameContaining(creatorName, pageRequest);
    }

    @GetMapping(value = "/paging/searchByRestaurantName")
    public Page<Post> getPostsPagingSearchRestaurantName(
            @RequestParam int page, @RequestParam int size, @RequestParam String restaurantName) {
        System.out.println("상호명: " + page + ", " + size+ ", " + restaurantName + "/");

        Sort sort = Sort.by("no").descending();
        PageRequest pageRequest =  PageRequest.of(page, size, sort);
        return postRepository.findByRestaurantNameContaining(restaurantName, pageRequest);
    }

    @GetMapping(value = "/paging/searchByLink")
    public Page<Post> getPostsPagingSearchLink(
            @RequestParam int page, @RequestParam int size, @RequestParam String link) {
        System.out.println("주소: " + page + ", " + size + ", " + link+ "/");

        Sort sort = Sort.by("no").descending();
        PageRequest pageRequest =  PageRequest.of(page, size, sort);
        return postRepository.findByLinkContaining(link, pageRequest);
    }

    @Auth
    @PostMapping // 로그인 해야만 글 쓸 수 있게 수정하기
    public ResponseEntity<Map<String, Object>> addPost(@RequestBody Post post, @RequestAttribute("authProfile") AuthProfile authProfile) {
        System.out.println("게시물추가: " + post.getNo());
        System.out.println("게시물추가: " + post.getCreatorName());
        System.out.println("게시물추가: " + post.getRestaurantName());
        System.out.println("게시물추가: " + authProfile);


        if(post.getRestaurantName() == null || post.getRestaurantName().isEmpty()
        || post.getLink() == null || post.getLink().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        post.setCreatedTime(new Date().getTime());
        post.setCreatorName(authProfile.getNickname());
        post.setProfileId(authProfile.getId());

        Post savedPost = postRepository.save(post);

        if(savedPost != null) {
            return  ResponseEntity.status(HttpStatus.CREATED).build();
        }
        return ResponseEntity.ok().build();
    }
}
