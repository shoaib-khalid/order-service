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
@Table(name = "payment_channel")
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentChannel implements Serializable {

    @Id
    private String channelCode;

    private String channelName;
    private String imageUrl;
    
    public String getImageUrl() {
        if (imageUrl==null)
            return null;
        else
            return OrderServiceApplication.ASSETURL + "/" + imageUrl;
    }

}
