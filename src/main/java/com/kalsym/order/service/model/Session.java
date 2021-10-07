package com.kalsym.order.service.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Sarosh
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class Session {

    private String id;
    private String username;
    private Date expiry;
    private String created;
    private String accessToken;
    private String refreshToken;
    private String ownerId;
    
    @JsonCreator
    public Session(@JsonProperty("id") String id,
            @JsonProperty("username") String username,
            @JsonProperty("expiry") Date expiry,
            @JsonProperty("created") String created,
            @JsonProperty("accessToken") String accessToken,
            @JsonProperty("refreshToken") String refreshToken,
            @JsonProperty("ownerId") String ownerId) {
        this.id = id;
        this.username = username;
        this.expiry = expiry;
        this.created = created;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.ownerId = ownerId;
    }

}
