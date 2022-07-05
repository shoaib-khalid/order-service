package com.kalsym.order.service.model;

import com.kalsym.order.service.model.object.ItemDiscount;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.utility.Logger;
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
import javax.persistence.Transient;
import javax.persistence.PostLoad;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author 7cu
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "product_asset")
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductAsset implements Serializable {

    @Id
    private String id;

    private String itemCode;
    private String name;
    private String url;
    private String productId;
    private Boolean isThumbnail;           

}
