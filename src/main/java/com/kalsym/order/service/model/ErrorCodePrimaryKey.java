/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package com.kalsym.order.service.model;

import java.io.Serializable;

/**
 *
 * @author taufik
 */
public class ErrorCodePrimaryKey implements Serializable {
    private String errorCode;
    private String modules;
    private String errorCategory;
    
    public ErrorCodePrimaryKey() {
    }

    public ErrorCodePrimaryKey(String errorCode, String modules, String errorCategory) {
        this.errorCode = errorCode;
        this.modules = modules;
        this.errorCategory = errorCategory;
    }
    
}
