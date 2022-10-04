
package com.kalsym.order.service.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author Faisal Hayat
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "delivery_sp")

public class DeliveryServiceProvider {
    
    @Id 
    Integer id;
    String name;  
}
