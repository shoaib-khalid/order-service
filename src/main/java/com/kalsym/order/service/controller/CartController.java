package com.kalsym.order.service.controller;

import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.enums.DeliveryType;
import com.kalsym.order.service.model.repository.CartRepository;
import com.kalsym.order.service.model.repository.CartItemRepository;
import com.kalsym.order.service.model.repository.OrderItemRepository;
import com.kalsym.order.service.model.repository.StoreDiscountRepository;
import com.kalsym.order.service.model.repository.StoreDiscountTierRepository;
import com.kalsym.order.service.model.repository.ProductRepository;
import com.kalsym.order.service.utility.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Date;
import java.math.BigDecimal;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import com.kalsym.order.service.model.Cart;
import com.kalsym.order.service.model.CartItem;
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
import com.kalsym.order.service.model.repository.OrderRepository;
import com.kalsym.order.service.utility.Logger;
import com.kalsym.order.service.model.object.Discount;
import com.kalsym.order.service.model.object.SubTotalDiscount;
import com.kalsym.order.service.model.StoreDiscount;
import com.kalsym.order.service.model.StoreDiscountTier;
import com.kalsym.order.service.enums.DiscountType;
import com.kalsym.order.service.enums.DiscountCalculationType;
import com.kalsym.order.service.enums.VehicleType;
import com.kalsym.order.service.service.DeliveryService;
import com.kalsym.order.service.utility.StoreDiscountCalculation;
import com.kalsym.order.service.model.DeliveryQuotation;
import com.kalsym.order.service.model.StoreCommission;
import com.kalsym.order.service.model.StoreDeliveryDetail;
import com.kalsym.order.service.model.StoreWithDetails;
import com.kalsym.order.service.model.object.OrderObject;
import com.kalsym.order.service.model.Product;
import com.kalsym.order.service.service.ProductService;
import com.kalsym.order.service.utility.OrderCalculation;
import static com.kalsym.order.service.utility.OrderCalculation.calculateStoreServiceCharges;
import com.kalsym.order.service.utility.Utilities;

/**
 *
 * @author 7cu
 */
@RestController()
@RequestMapping("/carts")
public class CartController {

    @Autowired
    CartRepository cartRepository;

    @Autowired
    CartItemRepository cartItemRepository;
    
    @Autowired
    OrderItemRepository orderItemRepository;
    
    @Autowired
    OrderRepository orderRepository;
    
    @Autowired
    StoreDiscountRepository storeDiscountRepository;
    
    @Autowired
    StoreDiscountTierRepository storeDiscountTierRepository;
    
    @Autowired
    ProductRepository productRepository;
     
    @Autowired
    DeliveryService deliveryService;
    
    @Autowired
    ProductService productService;
    
    @GetMapping(path = {""}, name = "carts-get", produces = "application/json")
    @PreAuthorize("hasAnyAuthority('carts-get', 'all')")
    public ResponseEntity<HttpResponse> getCarts(HttpServletRequest request,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        String logprefix = request.getRequestURI() + " ";

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "carts-get request...");
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Cart cartMatch = new Cart();
        cartMatch.setCustomerId(customerId);
        cartMatch.setStoreId(storeId);

        ExampleMatcher exampleCartMatcher = ExampleMatcher
                .matchingAll()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.EXACT);
        Example<Cart> cartExample = Example.of(cartMatch, exampleCartMatcher);

        Pageable pageable = PageRequest.of(page, pageSize);

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(cartRepository.findAll(cartExample, pageable));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping(path = {"/{id}"}, name = "carts-get-by-id", produces = "application/json")
    @PreAuthorize("hasAnyAuthority('carts-get-by-id', 'all')")
    public ResponseEntity<HttpResponse> getCartsById(HttpServletRequest request,
            @RequestParam(required = true) String id) {
        String logprefix = request.getRequestURI() + " ";

        HttpResponse response = new HttpResponse(request.getRequestURI());

        Optional<Cart> optCart = cartRepository.findById(id);

        if (!optCart.isPresent()) {
            response.setSuccessStatus(HttpStatus.NOT_FOUND);
            response.setError("cart not foud");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(optCart.get());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(path = {""}, name = "carts-post")
    @PreAuthorize("hasAnyAuthority('carts-post', 'all')")
    public ResponseEntity<HttpResponse> postCarts(HttpServletRequest request,
            @Valid @RequestBody Cart bodyCart) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "carts-post, URL:  " + request.getRequestURI());
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "carts-post, bodyCart: ", bodyCart.toString());

        Cart savedCart = null;
        try {
            bodyCart.setIsOpen(Boolean.TRUE);
            savedCart = cartRepository.save(bodyCart);
            response.setSuccessStatus(HttpStatus.CREATED);
        } catch (Exception exp) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error saving cart", exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cart created with id: " + savedCart.getId());
        response.setData(savedCart);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping(path = {"/{id}"}, name = "carts-delete-by-id")
    @PreAuthorize("hasAnyAuthority('carts-delete-by-id', 'all')")
    public ResponseEntity<HttpResponse> deleteCartsById(HttpServletRequest request, @PathVariable String id) {
        HttpResponse response = new HttpResponse(request.getRequestURI());
        String logprefix = request.getRequestURI() + " ";

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "carts-delete-by-id, cartId: " + id);

        Optional<Cart> optCart = cartRepository.findById(id);

        if (!optCart.isPresent()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cart not found with cartId: " + id);
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cart found", "");
        cartRepository.deleteById(id);

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cart deleted, with id: " + id);
        response.setSuccessStatus(HttpStatus.OK);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     *
     * @param request
     * @param id
     * @param bodyCart
     * @return
     */
    @PutMapping(path = {"/{id}"}, name = "carts-put-by-id")
    @PreAuthorize("hasAnyAuthority('carts-put-by-id', 'all')")
    public ResponseEntity<HttpResponse> putCartsById(HttpServletRequest request, @PathVariable String id,
            @Valid @RequestBody Cart bodyCart) {
        String logprefix = request.getRequestURI() + " ";
        String location = Thread.currentThread().getStackTrace()[1].getMethodName();
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "", "");
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, bodyCart.toString(), "");

        Optional<Cart> optCart = cartRepository.findById(id);

        if (!optCart.isPresent()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Cart not found with cartId: " + id);
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cart found with cartId: " + id);
        Cart cart = optCart.get();
        List<String> errors = new ArrayList<>();

        cart.update(bodyCart);

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cart updated for cartId: " + id);
        response.setSuccessStatus(HttpStatus.ACCEPTED);
        response.setData(cartRepository.save(cart));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * TODO: create endpoint to empty the cart by deleting all the cart-items in
     * the cart
     *
     * @param request
     * @param id
     * @return
     */
    @GetMapping(path = {"/{id}/empty"}, name = "carts-empty-by-id", produces = "application/json")
    @PreAuthorize("hasAnyAuthority('carts-empty-by-id', 'all')")
    public ResponseEntity<HttpResponse> empty(HttpServletRequest request,
            @PathVariable String id) {
        String logprefix = request.getRequestURI() + " ";

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "carts-empty-by-id request...");
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Cart cartMatch = new Cart();

        Optional<Cart> cartOptional = cartRepository.findById(id);

        Cart cart = cartOptional.get();

//        Order newOrder = new Order();
////        newOrder.setCompletionStatus("OnHold");
////        newOrder.setCustomerId(cart.getCustomerId());
////        newOrder.setPaymentStatus("Pending");
//        Order savedOrder = orderRepository.save(newOrder);
        // Closing the cart status since now order is being placed 
        cart.setIsOpen(Boolean.FALSE);
        Iterable<CartItem> cartItems = cartItemRepository.findByCartId(id, null);

        for (CartItem cartItem : cartItems) {
            cartItemRepository.delete(cartItem);
        }

        response.setSuccessStatus(HttpStatus.OK);
        //response.setData(cartRepository.findAll(cartExample, pageable));
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }

    @GetMapping(path = {"/{id}/weight"}, name = "carts-weight-by-id", produces = "application/json")
    @PreAuthorize("hasAnyAuthority('carts-weight-by-id', 'all')")
    public ResponseEntity<HttpResponse> getWeightOfCart(HttpServletRequest request,
            @PathVariable String id) {
        String logprefix = request.getRequestURI() + " ";

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "getWeightOfCart request");
        HttpResponse response = new HttpResponse(request.getRequestURI());

        List<CartItem> cartItems = cartItemRepository.findByCartId(id);

        double totalWeight = 0;
        VehicleType vehicleType = VehicleType.MOTORCYCLE;
        int vehicleSize=1;
        int totalPcs=0;
        if (null != cartItems) {
            for (int i = 0; i < cartItems.size(); i++) {
                CartItem cartItem = cartItems.get(i);
                double singleItemWeight = 0;
                if (null == cartItem.getWeight()) {
                    singleItemWeight = 1;
                }
                double itemWeight = cartItem.getQuantity() * singleItemWeight;
                totalWeight = totalWeight + itemWeight;
                totalPcs = totalPcs + cartItem.getQuantity();
                
                //check if any of item need a bigger vehicle
                Optional<Product> productInfoOpt = productRepository.findById(cartItem.getProductId());
                Product productInfo = productInfoOpt.get();
                if (productInfo.getVehicleType()==VehicleType.CAR && vehicleSize<2) {
                    vehicleType=VehicleType.CAR;
                    vehicleSize=2;
                } else if (productInfo.getVehicleType()==VehicleType.PICKUP && vehicleSize<3) {
                    vehicleType=VehicleType.PICKUP;
                    vehicleSize=3; 
                } else if (productInfo.getVehicleType()==VehicleType.VAN && vehicleSize<4) {
                    vehicleType=VehicleType.VAN;
                    vehicleSize=4; 
                } else if (productInfo.getVehicleType()==VehicleType.LARGEVAN && vehicleSize<5) {
                    vehicleType=VehicleType.LARGEVAN;
                    vehicleSize=5; 
                } else if (productInfo.getVehicleType()==VehicleType.SMALLLORRY && vehicleSize<6) {
                    vehicleType=VehicleType.SMALLLORRY;
                    vehicleSize=6; 
                } else if (productInfo.getVehicleType()==VehicleType.MEDIUMLORRY && vehicleSize<7) {
                    vehicleType=VehicleType.MEDIUMLORRY;
                    vehicleSize=7;
                } else if (productInfo.getVehicleType()==VehicleType.LARGELORRY && vehicleSize<8) {
                    vehicleType=VehicleType.LARGELORRY;
                    vehicleSize=8;
                } 
            }
        }

        class Weight {

            Double totalWeight;
            VehicleType vehicleType;
            Integer totalPcs;
            
            public Weight() {
            }

            public Double getTotalWeight() {
                return totalWeight;
            }

            public void setTotalWeight(Double totalWeight) {
                this.totalWeight = totalWeight;
            }
            
            public VehicleType getVehicleType() {
                return vehicleType;
            }

            public void setVehicleType(VehicleType vehicleType) {
                this.vehicleType = vehicleType;
            }
            
            public Integer getTotalPcs() {
                return totalPcs;
            }

            public void setTotalPcs(Integer totalPcs) {
                this.totalPcs = totalPcs;
            }

        }

        Weight w = new Weight();
        w.setTotalWeight(totalWeight);
        w.setVehicleType(vehicleType);
        w.setTotalPcs(totalPcs);
        response.setSuccessStatus(HttpStatus.OK);
        response.setData(w);
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }
    
    
    @GetMapping(path = {"/{id}/discount"}, name = "carts-discount-by-id", produces = "application/json")
    @PreAuthorize("hasAnyAuthority('carts-discount-by-id', 'all')")
    public ResponseEntity<HttpResponse> getDiscountOfCart(HttpServletRequest request,
            @PathVariable String id,
            @RequestParam(defaultValue = "0") Double deliveryCharge,
            @RequestParam(required = false) String deliveryQuotationId,
            @RequestParam(required = false) String deliveryType
            ) {
        String logprefix = request.getRequestURI() + " ";

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "getDiscountOfCart request...");
        HttpResponse response = new HttpResponse(request.getRequestURI());
        
        Optional<Cart> cartOptional = cartRepository.findById(id);
        if (!cartOptional.isPresent()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Cart not found with cartId: " + id);
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        //get delivery charges from delivery-service
        if (deliveryQuotationId!=null) {
            DeliveryQuotation deliveryQuotation = deliveryService.getDeliveryQuotation(deliveryQuotationId);
            deliveryCharge = deliveryQuotation.getAmount();
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "DeliveryCharge from delivery-service:"+deliveryCharge);
        }
        
        try {
            Cart cart = cartOptional.get(); 

            //getting store details for cart if from product service
            StoreWithDetails storeWithDetials = productService.getStoreById(cart.getStoreId());
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got store details of cartId: " + cart.getId() + ", and storeId: " + cart.getStoreId());

            StoreCommission storeCommission = productService.getStoreCommissionByStoreId(cart.getStoreId());
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got store commission: " + storeCommission);

            
            Discount discount = StoreDiscountCalculation.CalculateStoreDiscount(cart, deliveryCharge, cartItemRepository, storeDiscountRepository, storeDiscountTierRepository, logprefix);        
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cartId:"+id+" deliveryCharge:"+deliveryCharge+" totalSubTotalDiscount:"+discount.getSubTotalDiscount()+" totalShipmentDiscount:"+discount.getDeliveryDiscount());
            
            OrderObject orderTotalObject = OrderCalculation.CalculateOrderTotal(cart, storeWithDetials.getServiceChargesPercentage(), storeCommission, 
                            deliveryCharge, deliveryType, 
                            cartItemRepository, storeDiscountRepository, storeDiscountTierRepository, logprefix);                
            discount.setCartGrandTotal(Utilities.roundDouble(orderTotalObject.getTotal(),2));
            discount.setCartDeliveryCharge(Utilities.roundDouble(deliveryCharge,2));
            discount.setStoreServiceCharge(Utilities.roundDouble(orderTotalObject.getStoreServiceCharge(),2));
            discount.setStoreServiceChargePercentage(Utilities.roundDouble(storeWithDetials.getServiceChargesPercentage(),2));
            
            response.setSuccessStatus(HttpStatus.OK);
            response.setData(discount);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        
        } catch (Exception exp) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error calculate discount", exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }

    }
    
    
    @GetMapping(path = {"/{id}/subtotaldiscount"}, name = "carts-discount-by-id", produces = "application/json")
    @PreAuthorize("hasAnyAuthority('carts-discount-by-id', 'all')")
    public ResponseEntity<HttpResponse> getSubTotalDiscountOfCart(HttpServletRequest request,
            @PathVariable String id
           ) {
        String logprefix = request.getRequestURI() + " ";

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "getSubTotalDiscountOfCart request...");
        HttpResponse response = new HttpResponse(request.getRequestURI());
        
        Optional<Cart> cartOptional = cartRepository.findById(id);
        if (!cartOptional.isPresent()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Cart not found with cartId: " + id);
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }        
        
        try {
            Cart cart = cartOptional.get(); 
            
            StoreWithDetails storeWithDetials = productService.getStoreById(cart.getStoreId());
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got store details of cartId: " + cart.getId() + ", and storeId: " + cart.getStoreId());
            
            StoreCommission storeCommission = productService.getStoreCommissionByStoreId(cart.getStoreId());
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got store commission: " + storeCommission);
            
            Discount discount = StoreDiscountCalculation.CalculateStoreDiscount(cart, 0.00, cartItemRepository, storeDiscountRepository, storeDiscountTierRepository, logprefix);        
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cartId:"+id+" totalSubTotalDiscount:"+discount.getSubTotalDiscount()+" totalShipmentDiscount:"+discount.getDeliveryDiscount());
            
            Double storeSvcChargePercentage = storeWithDetials.getServiceChargesPercentage();
            double serviceCharges = calculateStoreServiceCharges(storeSvcChargePercentage, discount.getCartSubTotal().doubleValue(), discount.getSubTotalDiscount().doubleValue());
        
            SubTotalDiscount subTotalDiscount = new SubTotalDiscount();
            subTotalDiscount.setCartSubTotal(discount.getCartSubTotal());            
            subTotalDiscount.setDiscountType(discount.getDiscountType());
            subTotalDiscount.setDiscountCalculationType(discount.getDiscountCalculationType());
            subTotalDiscount.setDiscountCalculationValue(discount.getDiscountCalculationValue());
            subTotalDiscount.setDiscountId(discount.getDiscountId());
            subTotalDiscount.setSubTotalDiscount(discount.getSubTotalDiscount());
            subTotalDiscount.setSubTotalDiscountDescription(discount.getSubTotalDiscountDescription());
            subTotalDiscount.setStoreServiceCharge(Utilities.roundDouble(serviceCharges,2));
            subTotalDiscount.setStoreServiceChargePercentage(Utilities.roundDouble(storeSvcChargePercentage,2));
            
            response.setSuccessStatus(HttpStatus.OK);
            response.setData(subTotalDiscount);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        
        } catch (Exception exp) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error calculate sub-total discount", exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }

    }
    
    
    
   
}
