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
    List<String> selectedItemId;

    public CartWithItem() {
    }

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public List<String> getSelectedItemId() {
        return selectedItemId;
    }

    public void setSelectedItemId(List<String> selectedItemId) {
        this.selectedItemId = selectedItemId;
    }


}
