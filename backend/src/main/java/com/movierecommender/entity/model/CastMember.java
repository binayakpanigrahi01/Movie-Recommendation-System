package com.movierecommender.entity.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CastMember implements Serializable {
    private String name;
    private String character;

    @JsonProperty("photoUrl")
    @JsonAlias({"photo_url", "photoUrl"})
    private String photoUrl;
}
