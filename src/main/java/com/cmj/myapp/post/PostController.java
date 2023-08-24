package com.cmj.myapp.post;

import com.cmj.myapp.auth.Auth;
import com.cmj.myapp.auth.AuthProfile;
import com.cmj.myapp.auth.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
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

    @Auth
    @GetMapping
    public ResponseEntity<Map<String, Object>> getPostList(@RequestAttribute AuthProfile authProfile) {
        System.out.println(authProfile);
        Optional<List<Post>> postList = postRepository.findPostByCreatorName(authProfile.getNickname());

        if (!postList.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Map<String, Object> responese = new HashMap<>();
        List<Object> posts = new ArrayList<>();
        for (Post post : postList.get()) {
            Map<String, Object> postMap = new HashMap<>();
            postMap.put("no", post.getNo());
            postMap.put("restaurantName", post.getRestaurantName());
            postMap.put("creatorName", post.getCreatorName());
            postMap.put("createdTime", post.getCreatedTime());
            posts.add(postMap);
        }

        responese.put("data", posts);
        return ResponseEntity.ok().body(responese);
    }


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
        System.out.println("작성자: " + page + ", " + size + ", " + creatorName + "/");

        Sort sort = Sort.by("no").descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return postRepository.findByCreatorNameContaining(creatorName, pageRequest);
    }

    @GetMapping(value = "/paging/searchByRestaurantName")
    public Page<Post> getPostsPagingSearchRestaurantName(
            @RequestParam int page, @RequestParam int size, @RequestParam String restaurantName) {
        System.out.println("상호명: " + page + ", " + size + ", " + restaurantName + "/");

        Sort sort = Sort.by("no").descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return postRepository.findByRestaurantNameContaining(restaurantName, pageRequest);
    }

    @GetMapping(value = "/paging/searchByLink")
    public Page<Post> getPostsPagingSearchLink(
            @RequestParam int page, @RequestParam int size, @RequestParam String link) {
        System.out.println("주소: " + page + ", " + size + ", " + link + "/");

        Sort sort = Sort.by("no").descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return postRepository.findByLinkContaining(link, pageRequest);
    }

    @Auth
    @PostMapping
    public ResponseEntity addPost(@RequestBody Post post, @RequestAttribute("authProfile") AuthProfile authProfile) {
        System.out.println("게시물추가 no: " + post.getNo());
        System.out.println("게시물추가 link: " + post.getLink());
        System.out.println("게시물추가 restuarantName: " + post.getRestaurantName());
        System.out.println("게시물추가: " + authProfile);

        if (post.getRestaurantName() == null || post.getRestaurantName().isEmpty()
                || post.getLink() == null || post.getLink().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        post.setCreatedTime(new Date().getTime());
        post.setCreatorName(authProfile.getNickname());

        Optional<Profile> isverityProfile = profileRepository.findByUser_Id(authProfile.getId());
        if (!isverityProfile.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        post.setProfile(isverityProfile.get());
        System.out.println("set한 post객체 " + post);

        Post savedPost = postRepository.save(post);

        if (savedPost != null) {
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }
        return ResponseEntity.ok().build();
    }

    @Auth
    @DeleteMapping
    // 리퀘스트 객체로 받아와서 post no랑 닉네임이랑 비교해서 삭제하는게 낫지 않나?
    // 왜냐믄 지금은 어스프로필을 사용못하고있음.. 나중에 수정하긔
    public ResponseEntity removePost(@RequestParam List<Integer> nos, @RequestAttribute("authProfile") AuthProfile authProfile) {
        System.out.println(authProfile);
        System.out.println(nos);

        for(Integer postNo : nos) {
            if(!postRepository.findPostByNo(Long.valueOf(postNo)).isPresent()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
//            postRepository.deleteById(Long.valueOf(postNo));
            System.out.println(Long.valueOf(postNo));
        }
        return  ResponseEntity.status(HttpStatus.OK).build();

    }
}


