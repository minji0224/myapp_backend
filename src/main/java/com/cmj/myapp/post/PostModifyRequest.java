package com.cmj.myapp.post;

import jakarta.persistence.Column;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PostModifyRequest {
    private String restaurantName;
    private String link;
    private String content;
    private String image;
}
