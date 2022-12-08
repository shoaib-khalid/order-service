package com.kalsym.order.service.model;

import com.kalsym.order.service.enums.CartStage;
import com.kalsym.order.service.enums.PaymentStatus;
import com.kalsym.order.service.enums.ServiceType;
import com.kalsym.order.service.model.object.ItemDiscount;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.math.BigInteger;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

/**
 *
 * @author 7cu
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "qrcode_order_group")

/**
 * When a customer leaves an online store without making a purchase it is
 * recorded as an abandoned cart
 */
public class QrcodeOrderGroup implements Serializable {

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    Long id;

    private String qrToken;
    private String invoiceNo;
    private String storeId;
    private PaymentStatus paymentStatus;
    private String tableNo;
    
}
