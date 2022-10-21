/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.model;

/**
 *
 * @author taufik
 */
public enum Error {
    
    //ERROR
    RECORD_NOT_FOUND("401"),
    CONNECTION_ERROR("501"),
    
    
    //SUCCESS
    RECORD_CREATED("201"),
    RECORD_UPDATED("202"),
    RECORD_DELETED("203"),
    RECORD_FETCHED("204"),
    ;    
     
    public final String errorCode;
        
    private Error(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getCode() {
        return this.errorCode;
    }
}