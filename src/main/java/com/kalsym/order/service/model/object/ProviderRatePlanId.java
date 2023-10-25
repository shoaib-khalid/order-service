/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package com.kalsym.order.service.model.object;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 *
 * @author user
 */
@Getter
@Setter
@Embeddable
public class ProviderRatePlanId implements Serializable {
    Integer spId;
    String productCode;
}
