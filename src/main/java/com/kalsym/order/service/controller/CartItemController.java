package com.kalsym.order.service.controller;

import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.model.Cart;
import com.kalsym.order.service.model.ProductInventory;
import com.kalsym.order.service.model.Product;
import com.kalsym.order.service.model.CartItem;
import com.kalsym.order.service.model.CartSubItem;
import com.kalsym.order.service.model.repository.CartItemRepository;
import com.kalsym.order.service.model.repository.CartSubItemRepository;
import com.kalsym.order.service.model.repository.CartRepository;
import com.kalsym.order.service.model.repository.ProductRepository;
import com.kalsym.order.service.utility.HttpResponse;
import com.kalsym.order.service.model.object.ItemDiscount;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.kalsym.order.service.model.repository.ProductInventoryRepository;
import com.kalsym.order.service.service.ProductService;
import com.kalsym.order.service.utility.Logger;

/**
 *
 * @author 7cu
 */
@RestController()
@RequestMapping("/carts/{cartId}/items")
public class CartItemController {
    
    @Autowired
    CartRepository cartRepository;
    
    @Autowired
    CartItemRepository cartItemRepository;
    
    @Autowired
    CartSubItemRepository cartSubItemRepository;
    
    @Autowired
    ProductInventoryRepository productInventoryRepository;
    
    @Autowired
    ProductRepository productRepository;
    
    @Autowired
    ProductService productService;
    
    @GetMapping(path = {""}, name = "cart-items-get")
    @PreAuthorize("hasAnyAuthority('cart-items-get', 'all')")
    public ResponseEntity<HttpResponse> getCartItems(HttpServletRequest request,
            @PathVariable(required = true) String cartId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        
        HttpResponse response = new HttpResponse(request.getRequestURI());
        
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cart-items-get, URL: " + request.getRequestURI(), cartId);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cart-items-get, cartId: " + cartId);
        
        Optional<Cart> cart = cartRepository.findById(cartId);
        
        if (!cart.isPresent()) {
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cart-items-get, cartId, not found. cartId: " + cartId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        Pageable pageable = PageRequest.of(page, pageSize);
        
        response.setSuccessStatus(HttpStatus.OK);
        response.setData(cartItemRepository.findByCartId(cartId, pageable));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    @GetMapping(path = {"/{id}"}, name = "cart-items-get-by-id")
    @PreAuthorize("hasAnyAuthority('cart-items-get-by-id', 'all')")
    public ResponseEntity<HttpResponse> getCartItemsByCartItemId(HttpServletRequest request,
            @PathVariable(required = true) String cartId,
            @PathVariable(required = true) String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        
        HttpResponse response = new HttpResponse(request.getRequestURI());
        
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cart-items-get-by-id, cartItemId: " + id);
        
        Optional<CartItem> cartItem = cartItemRepository.findById(id);
        
        if (!cartItem.isPresent()) {
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cart-items-get-by-id, cartItemId, not found. cartItemId: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        Pageable pageable = PageRequest.of(page, pageSize);
        
        response.setSuccessStatus(HttpStatus.OK);
        response.setData(cartItemRepository.findById(id));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    @PostMapping(path = {""}, name = "cart-items-post")
    @PreAuthorize("hasAnyAuthority('cart-items-post', 'all')")
    public ResponseEntity<HttpResponse> postCartItems(HttpServletRequest request,
            @PathVariable(required = true) String cartId,
            @Valid @RequestBody CartItem bodyCartItem) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        
        HttpResponse response = new HttpResponse(request.getRequestURI());
        
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cart-items-post, cartId: " + cartId);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "bodyCartItem: " + bodyCartItem.toString());
        
        Optional<Cart> savedCart = null;
        
        savedCart = cartRepository.findById(cartId);
        if (savedCart == null) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cart-items-post, cartId not found, cartId: " + cartId);
            response.setErrorStatus(HttpStatus.FAILED_DEPENDENCY);
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(response);
        }
        
        //get product info
        Optional<Product> optProduct = productRepository.findById(bodyCartItem.getProductId());
        if (!optProduct.isPresent()) {
            response.setMessage("Product not found");
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        
        CartItem cartItem;
        try {
            //find product invertory against itemcode to set sku
            //query product-service
            ProductInventory productInventory = productService.getProductInventoryById(savedCart.get().getStoreId(), bodyCartItem.getProductId(), bodyCartItem.getItemCode());
            
            //query db
            //ProductInventory productInventory = productInventoryRepository.findByItemCode(bodyCartItem.getItemCode());
            
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got product inventory details: " + productInventory.toString());
            
            //check if enable product inventory
            if (productInventory.getQuantity()<bodyCartItem.getQuantity() && optProduct.get().isAllowOutOfStockPurchases()==false) {
                //out of stock
                response.setMessage(optProduct.get().getName()+" is out of stock");
                response.setErrorStatus(HttpStatus.CONFLICT);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
                    
            //check for discount
            double itemPrice = 0.00;
            if (productInventory.getItemDiscount()!=null) {
                //got discount
                ItemDiscount discountDetails = productInventory.getItemDiscount();
                itemPrice = discountDetails.discountedPrice;
                bodyCartItem.setDiscountId(discountDetails.discountId);
                bodyCartItem.setNormalPrice((float)discountDetails.normalPrice);
                bodyCartItem.setDiscountLabel(discountDetails.discountLabel);
            } else {
                //no dicount for this item code
                itemPrice = productInventory.getPrice();
            }
            bodyCartItem.setProductPrice((float)itemPrice);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "itemPrice:"+itemPrice);
            //check if product is package            
            boolean isPackage = optProduct.get().getIsPackage();            
            if (isPackage) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Product is package");
                bodyCartItem.setPrice(bodyCartItem.getQuantity() * bodyCartItem.getProductPrice());
                bodyCartItem.setProductName(productInventory.getProduct().getName());
                cartItem = cartItemRepository.save(bodyCartItem);
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Saved cartItem id:"+cartItem.getId());
                //save sub cart item
                if (bodyCartItem.getCartSubItem()!=null) {
                    for (int i=0;i<bodyCartItem.getCartSubItem().size();i++) {
                        CartSubItem subItem = bodyCartItem.getCartSubItem().get(i);
                        subItem.setCartItemId(cartItem.getId());
                        cartSubItemRepository.save(subItem);
                    }
                }
            } else {
                //find item in current cart, increase quantity if already exist
                CartItem existingItem = cartItemRepository.findByCartIdAndItemCodeAndSpecialInstruction(bodyCartItem.getCartId(), bodyCartItem.getItemCode(), bodyCartItem.getSpecialInstruction());
                if (existingItem != null) {
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "item already exist for cartId: " + bodyCartItem.getCartId());
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "item already exist for itemCode: " + bodyCartItem.getItemCode());

                    int newQty = existingItem.getQuantity() + bodyCartItem.getQuantity();
                    existingItem.setQuantity(newQty);
                    existingItem.setPrice(newQty * existingItem.getProductPrice());
                    existingItem.setProductName(productInventory.getProduct().getName());
                    cartItem = cartItemRepository.save(existingItem);
                } else {
                    bodyCartItem.setPrice(bodyCartItem.getQuantity() * bodyCartItem.getProductPrice());
                    bodyCartItem.setProductName(productInventory.getProduct().getName());
                    cartItem = cartItemRepository.save(bodyCartItem);
                } 
            }
            response.setSuccessStatus(HttpStatus.CREATED);
        } catch (Exception exp) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error saving cart item", exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cartItem added in cartId: " + cartId);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cartItem added in cart with cartItemId: " + cartItem.getId());
        
        //refresh and retrieve back the data
        cartItemRepository.refresh(cartItem);
        Optional<CartItem> cartItemData = cartItemRepository.findById(cartItem.getId());
        
        response.setData(cartItemData.get());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @DeleteMapping(path = {"/{id}"}, name = "cart-items-delete-by-id")
    @PreAuthorize("hasAnyAuthority('cart-items-delete-by-id', 'all')")
    public ResponseEntity<HttpResponse> deleteCartItemsById(HttpServletRequest request,
            @PathVariable(required = true) String cartId,
            @PathVariable(required = true) String id) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        
        HttpResponse response = new HttpResponse(request.getRequestURI());
        
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cart-items-delete-by-id, cartId:[" + cartId + "], cartItemId:[" + id +"]");
        
        Optional<Cart> savedCart = null;
        
        savedCart = cartRepository.findById(cartId);
        if (savedCart == null) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cart-items-delete-by-id, cartId not found, cartId: " + cartId);
            response.setErrorStatus(HttpStatus.FAILED_DEPENDENCY);
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(response);
        }
        
        Optional<CartItem> savedItem = cartItemRepository.findById(id);
        if (savedItem == null) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cart-items-delete-by-id, itemId not found, id: " + id);
            response.setErrorStatus(HttpStatus.FAILED_DEPENDENCY);
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(response);
        } 
        
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cart-items-delete-by-id, Item found! Proceed to delete");
        
        try {
            cartItemRepository.delete(savedItem.get());
            response.setSuccessStatus(HttpStatus.OK);
        } catch (Exception exp) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error deleting cart item with id: " + id, exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cartItem deleted with id: " + id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    @PutMapping(path = {"/{id}"}, name = "cart-items-put-by-id")
    @PreAuthorize("hasAnyAuthority('cart-items-put-by-id', 'all')")
    public ResponseEntity<HttpResponse> putCartItemsById(HttpServletRequest request,
            @PathVariable(required = true) String cartId,
            @PathVariable(required = true) String id,
            @Valid @RequestBody CartItem bodyCartItem) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        
        HttpResponse response = new HttpResponse(request.getRequestURI());
        
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cart-items-put-by-id, cartId: " + cartId);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, bodyCartItem.toString(), "");

//        Optional<Cart> optCart = cartRepository.findById(cartId);
//
//        if (!optCart.isPresent()) {
//            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Cart not found with cartId: "+ cartId);
//            response.setErrorStatus(HttpStatus.NOT_FOUND);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//        }
        Optional<CartItem> optCartItem = cartItemRepository.findById(id);
        
        if (!optCartItem.isPresent()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "CartItem not found with cartItemId: " + id);
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cartItem found with cartItemId: " + id);
        CartItem cartItem = optCartItem.get();
        
        ProductInventory productInventory = productInventoryRepository.findByItemCode(cartItem.getItemCode());
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got product inventory details for package: " + productInventory.toString());
            
        //check for discount
        double itemPrice = 0.00;
        if (productInventory.getItemDiscount()!=null) {
            //got discount
            ItemDiscount discountDetails = productInventory.getItemDiscount();
            itemPrice = discountDetails.discountedPrice;
            bodyCartItem.setDiscountId(discountDetails.discountId);
            bodyCartItem.setNormalPrice((float)discountDetails.normalPrice);
            bodyCartItem.setDiscountLabel(discountDetails.discountLabel);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Item got discount. price:"+itemPrice);            
        } else {
            //no dicount for this item code
            itemPrice = productInventory.getPrice();
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Item no discount. price:"+itemPrice);            
        }
        bodyCartItem.setProductPrice((float)itemPrice);
            
        //check if product is package
        Optional<Product> optProduct = productRepository.findById(cartItem.getProductId());
        boolean isPackage=false;
        if (optProduct.isPresent()) {
            isPackage = optProduct.get().getIsPackage();
        }
        if (isPackage) {
            cartItem.update(bodyCartItem, (float)itemPrice);
            //clear sub item for previous item
            cartSubItemRepository.clearCartSubItem(cartItem.getId());
             //save sub cart item
            if (bodyCartItem.getCartSubItem()!=null) {
                for (int i=0;i<bodyCartItem.getCartSubItem().size();i++) {
                    CartSubItem subItem = bodyCartItem.getCartSubItem().get(i);
                    subItem.setCartItemId(cartItem.getId());
                    cartSubItemRepository.save(subItem);
                }
            }
        } else {
            //find product invertory against itemcode to set sku
            cartItem.update(bodyCartItem, (float)itemPrice);
        }
                
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cartItem updated for cartItemId: " + id);
        response.setSuccessStatus(HttpStatus.ACCEPTED);
        response.setData(cartItemRepository.save(cartItem));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
    
    @DeleteMapping(path = {"/clear"}, name = "cart-items-clear-by-id")
    @PreAuthorize("hasAnyAuthority('cart-items-clear-by-id', 'all')")
    public ResponseEntity<HttpResponse> clearCartItems(HttpServletRequest request,
            @PathVariable(required = true) String cartId) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());
        
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cart-items-delete-by-id, cartId: " + cartId);
        
        Optional<Cart> savedCart = null;
        
        savedCart = cartRepository.findById(cartId);
        if (savedCart == null) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cart-items-clear-by-id, cartId not found, cartId: " + cartId);
            response.setErrorStatus(HttpStatus.FAILED_DEPENDENCY);
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(response);
        }
        
        try {
            cartItemRepository.clearCartItem(cartId);
            response.setSuccessStatus(HttpStatus.OK);
        } catch (Exception exp) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error clear cart item with cartId: " + cartId, exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cartItem deleted with id: " + cartId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    @PostMapping(path = {"/updatequantiy/{id}/{quantityChange}"}, name = "cart-items-updatequantity-by-id")
    @PreAuthorize("hasAnyAuthority('cart-items-updatequantity-by-id', 'all')")
    public ResponseEntity<HttpResponse> updateQuantityCartItemsById(HttpServletRequest request,
            @PathVariable(required = true) String cartId,
            @PathVariable(required = true) String id,
            @PathVariable(required = true) int quantityChange) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());
        
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cart-items-delete-by-id, cartId: " + cartId + ", cartItemId: " + id);
        
        Optional<Cart> savedCart = null;
        
        savedCart = cartRepository.findById(cartId);
        if (savedCart == null) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cart-items-updatequantity-by-id, cartId not found, cartId: " + id);
            response.setErrorStatus(HttpStatus.FAILED_DEPENDENCY);
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(response);
        }

        //find item in current cart, increase quantity if already exist
        Optional<CartItem> existingItem = cartItemRepository.findById(id);
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "item exist for cartId: " + item.getCartId() + " with productId: " + item.getProductId());
            int newQty = item.getQuantity() + quantityChange;
            item.setQuantity(newQty);
            cartItemRepository.save(item);
        } else {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Item not found for id: " + id);
            response.setErrorStatus(HttpStatus.FAILED_DEPENDENCY);
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(response);
        }
        try {
            cartItemRepository.deleteById(id);
            response.setSuccessStatus(HttpStatus.OK);
        } catch (Exception exp) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error deleting cart item with id: " + id, exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cartItem deleted with id: " + id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
}
