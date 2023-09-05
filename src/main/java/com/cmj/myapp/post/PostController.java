package com.cmj.myapp.post;

import com.cmj.myapp.auth.Auth;
import com.cmj.myapp.auth.AuthProfile;
import com.cmj.myapp.auth.entity.*;
import com.cmj.myapp.auth.repository.ProfileRepository;
import com.cmj.myapp.auth.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Tag(name = "게시물 관리 API")
@RestController
@RequestMapping(value = "/posts")
public class PostController {
    Map<String, Post> map = new ConcurrentHashMap<>();
    @Autowired
    PostRepository postRepository;
//    @Autowired
//    UserRepository userRepository;
    @Autowired
    ProfileRepository profileRepository;

    @Operation(summary = "수정할 해당 게시물 조회", security = { @SecurityRequirement(name = "bearer-key") })
    @Auth
    @GetMapping(value = "/{postNo}")
    public ResponseEntity getPost(@PathVariable long postNo, @RequestAttribute AuthProfile authProfile) {
        System.out.println(postNo);
        Optional<Post> findedPost = postRepository.findPostByNo(postNo);

        if(!findedPost.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if(findedPost.get().getCreatorName().equals(authProfile.getNickname())){
            return ResponseEntity.status(HttpStatus.OK).body(findedPost.get());
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(findedPost.get());
        }
    }

    @Operation(summary = "사용자가 등록한 게시물 조회", security = { @SecurityRequirement(name = "bearer-key") })
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

    @Operation(summary = "게시물 목록 페이징 조회")
    @GetMapping(value = "/paging")
    public Page<Post> getPostsPaging(@RequestParam int page, @RequestParam int size) {
        System.out.println("첫화면: " + page + ", " + size + "/");

        Sort sort = Sort.by("no").descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        // PageRequest.of(페이지 번호, 페이지 크기, 정렬정보).descending(내림차순)/ascending(오름차순)

        return postRepository.findAll(pageRequest);
    }

    @Operation(summary = "작성자로 게시물 조회")
    @GetMapping(value = "/paging/searchByCreatorName")
    public Page<Post> getPostsPagingSearchCreatorName(
            @RequestParam int page, @RequestParam int size, @RequestParam String creatorName) {
        System.out.println("작성자: " + page + ", " + size + ", " + creatorName + "/");

        Sort sort = Sort.by("no").descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return postRepository.findByCreatorNameContaining(creatorName, pageRequest);
    }

    @Operation(summary = "상호명으로 게시물 조회")
    @GetMapping(value = "/paging/searchByRestaurantName")
    public Page<Post> getPostsPagingSearchRestaurantName(
            @RequestParam int page, @RequestParam int size, @RequestParam String restaurantName) {
        System.out.println("상호명: " + page + ", " + size + ", " + restaurantName + "/");

        Sort sort = Sort.by("no").descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return postRepository.findByRestaurantNameContaining(restaurantName, pageRequest);
    }

    @Operation(summary = "식당 주소로 게시물 조회")
    @GetMapping(value = "/paging/searchByLink")
    public Page<Post> getPostsPagingSearchLink(
            @RequestParam int page, @RequestParam int size, @RequestParam String link) {
        System.out.println("주소: " + page + ", " + size + ", " + link + "/");

        Sort sort = Sort.by("no").descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return postRepository.findByLinkContaining(link, pageRequest);
    }
    @Operation(summary = "게시물 등록", security = { @SecurityRequirement(name = "bearer-key") })
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
    @Operation(summary = "게시물 삭제", security = { @SecurityRequirement(name = "bearer-key") })
    @Auth
    @DeleteMapping
    public ResponseEntity removePost(@RequestParam List<Integer> nos, @RequestAttribute("authProfile") AuthProfile authProfile) {
        System.out.println(authProfile);
        System.out.println(nos);
        System.out.println(nos.isEmpty());

        if(nos.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        for(Integer postNo : nos) {

            Optional<Post> postOptional = postRepository.findPostByNo(Long.valueOf(postNo));

            if(!postOptional.isPresent()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            Post post = postOptional.get();
            System.out.println(post.getCreatorName());
            System.out.println(authProfile.getNickname());
            if(post.getCreatorName().equals(authProfile.getNickname())){
                postRepository.deleteById(Long.valueOf(postNo));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }
    @Operation(summary = "게시물 수정", security = { @SecurityRequirement(name = "bearer-key") })
    @Auth
    @PutMapping(value = "/{postNo}")
    public ResponseEntity modifyPost(@PathVariable long postNo, @RequestBody PostModifyRequest postModifyRequest
                                        ,@RequestAttribute("authProfile") AuthProfile authProfile) {
        System.out.println("들어온 포스트넘버: " + postNo);
//        System.out.println("들어온 포스트수정객체: " + postModifyRequest);

        Optional<Post> findedPost = postRepository.findById(postNo);
        System.out.println("해당 넘버 있는지 확인 객체: " + findedPost);

        if(!findedPost.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }


        Post toModifyPost = findedPost.get();
        System.out.println(toModifyPost.getCreatorName());
        System.out.println(authProfile.getNickname());

        if(!toModifyPost.getCreatorName().equals(authProfile.getNickname())){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
//        System.out.println("해당 포스트 겟: " + toModifyPost);

        if(postModifyRequest.getRestaurantName() != null && !postModifyRequest.getRestaurantName().isEmpty()) {
            toModifyPost.setRestaurantName(postModifyRequest.getRestaurantName());
        }
        if(postModifyRequest.getLink() != null && !postModifyRequest.getLink().isEmpty()) {
            toModifyPost.setLink(postModifyRequest.getLink());
        }
        if(postModifyRequest.getContent() != null && !postModifyRequest.getContent().isEmpty()) {
            toModifyPost.setContent(postModifyRequest.getContent());
        }
        if(postModifyRequest.getImage() != null && !postModifyRequest.getImage().isEmpty()) {
            toModifyPost.setImage(postModifyRequest.getImage());
        }

        postRepository.save(toModifyPost);
        return ResponseEntity.ok().build();
    }

}


