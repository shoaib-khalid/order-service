/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package com.kalsym.order.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kalsym.order.service.model.object.ProviderRatePlanId;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.io.Serializable;

/**
 *
 * @author user
 */

@Entity
@Table(name = "payment_sp_rate_plan")
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProviderRatePlan implements Serializable {
    @EmbeddedId
    ProviderRatePlanId id;
    String marginType;
    Double margin;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "spId", insertable = false, updatable = false)
//    @Fetch(FetchMode.JOIN)
//    private Provider provider;
}
