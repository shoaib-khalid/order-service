
package com.kalsym.order.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author FaisalHayatJadoon
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class Email {
    private String[] to;
    private String subject;
    private String rawBody;
    private Body body;
    private String from;
    private String fromName;
    private String domain;
}
