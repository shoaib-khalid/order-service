package com.kalsym.order.service.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import com.kalsym.order.service.enums.VehicleType;
import com.kalsym.order.service.OrderServiceApplication;
import javax.persistence.Transient;

/**
 *
 * @author 7cu
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "addon_template_item")
public class AddOnTemplateItem {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    private String groupId;
    private String name;
    private Double price;
    private Double dineInPrice;
   
}
