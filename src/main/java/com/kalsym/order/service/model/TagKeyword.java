/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.model;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
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
@Table(name = "tag_keyword")
public class TagKeyword implements Serializable {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    Long id;
    
    String keyword;
            
    String latitude;
    
    String longitude;
    
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "tagId", insertable = false, updatable = false, nullable = true)
    private List<TagDetails> tagDetails;
    
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "tagId", insertable = false, updatable = false, nullable = true)
    private List<TagConfig> tagConfigs;
    
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "tagId", insertable = false, updatable = false, nullable = true)
    private List<TagProductFeature> productFeatureList;
}
