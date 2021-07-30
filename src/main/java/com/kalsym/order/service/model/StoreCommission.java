
package com.kalsym.order.service.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author FaisalHayatJadoon
 */
@Entity
@Table(name = "store_commission")
@Getter
@Setter
@ToString
public class StoreCommission implements Serializable {
    @Id
    private String storeId;
    private Double minChargeAmount;
    private Double rate;
    private int settlementDays;
    private Date startDate;
    private Date endDate;
}