/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.model;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
@Table(name = "tag_zone")
public class TagZone implements Serializable {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    Integer id;
    
    String zoneName;
        
}
