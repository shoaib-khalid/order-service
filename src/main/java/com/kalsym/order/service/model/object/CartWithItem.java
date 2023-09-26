/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.model.object;

import com.kalsym.order.service.model.CartItem;
import java.util.List;

/**
 *
 * @author taufik
 */
public class CartWithItem {    

    String cartId;
    String deliveryQuotationId;
    String deliveryType;
    String storeVoucherCode;
    List<String> selectedItemId;

    public List<SelectedItem> getSelectedItems() {
        return selectedItems;
    }

    List<SelectedItem> selectedItems;

    public CartWithItem() {
    }

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }
    
    public String getDeliveryQuotationId() {
        return deliveryQuotationId;
    }

    public void setDeliveryQuotationId(String deliveryQuotationId) {
        this.deliveryQuotationId = deliveryQuotationId;
    }
    
    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }
    
    public String getStoreVoucherCode() {
        return storeVoucherCode;
    }

    public void setStoreVoucherCode(String storeVoucherCode) {
        this.storeVoucherCode = storeVoucherCode;
    }

    public List<String> getSelectedItemId() {
        return selectedItemId;
    }

    public void setSelectedItemId(List<String> selectedItemId) {
        this.selectedItemId = selectedItemId;
    }


}
