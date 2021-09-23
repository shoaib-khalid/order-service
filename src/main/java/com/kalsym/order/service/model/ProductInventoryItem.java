package com.kalsym.order.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author 7cu
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "product_inventory_item")
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductInventoryItem implements Serializable {

    @Id
    private String itemCode;
    @Id
    private String productVariantAvailableId;
   
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "productVariantAvailableId", referencedColumnName = "id", insertable = false, updatable = false, nullable = true)
    private ProductVariantAvailable productVariantAvailable;
}
