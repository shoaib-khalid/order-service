/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.model;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author taufik
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tag_product_feature")
public class TagProductFeature implements Serializable {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    Long id;
    
    Long tagId;
    
    String productId;
    
    int sequence;
    
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "productId", insertable = false, updatable = false)
    private Product product;  
        
        
}
