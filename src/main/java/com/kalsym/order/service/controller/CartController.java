package com.kalsym.order.service.controller;

import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.enums.CartStage;
import com.kalsym.order.service.enums.DeliveryType;
import com.kalsym.order.service.enums.ServiceType;
import com.kalsym.order.service.enums.DineInOption;
import com.kalsym.order.service.model.repository.CartRepository;
import com.kalsym.order.service.model.repository.CartWithDetailsRepository;
import com.kalsym.order.service.model.repository.CartItemRepository;
import com.kalsym.order.service.model.repository.OrderItemRepository;
import com.kalsym.order.service.model.repository.StoreDiscountRepository;
import com.kalsym.order.service.model.repository.StoreDiscountTierRepository;
import com.kalsym.order.service.model.repository.ProductRepository;
import com.kalsym.order.service.model.repository.StoreDeliveryDetailRepository;
import com.kalsym.order.service.model.repository.CustomerVoucherRepository;
import com.kalsym.order.service.model.repository.CartSearchSpecs;
import com.kalsym.order.service.model.repository.VoucherRepository;
import com.kalsym.order.service.utility.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import com.kalsym.order.service.model.Cart;
import com.kalsym.order.service.model.CartWithDetails;
import com.kalsym.order.service.model.CartItem;
import com.kalsym.order.service.model.StoreDeliveryDetail;
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
import com.kalsym.order.service.model.object.GroupDiscount;
import com.kalsym.order.service.model.object.SubTotalDiscount;
import com.kalsym.order.service.model.StoreDiscount;
import com.kalsym.order.service.model.StoreDiscountTier;
import com.kalsym.order.service.enums.DiscountType;
import com.kalsym.order.service.enums.DiscountCalculationType;
import com.kalsym.order.service.enums.VehicleType;
import com.kalsym.order.service.model.CustomerVoucher;
import com.kalsym.order.service.service.DeliveryService;
import com.kalsym.order.service.utility.StoreDiscountCalculation;
import com.kalsym.order.service.model.DeliveryQuotation;
import com.kalsym.order.service.model.StoreCommission;
import com.kalsym.order.service.model.StoreDeliveryDetail;
import com.kalsym.order.service.model.StoreWithDetails;
import com.kalsym.order.service.model.object.OrderObject;
import com.kalsym.order.service.model.Product;
import com.kalsym.order.service.model.Voucher;
import com.kalsym.order.service.model.RegionCountry;
import com.kalsym.order.service.model.repository.VoucherSearchSpecs;
import com.kalsym.order.service.model.repository.RegionCountriesRepository;
import com.kalsym.order.service.model.object.CartWithItem;
import com.kalsym.order.service.model.object.CustomPageable;
import com.kalsym.order.service.model.object.StoreSnooze;
import com.kalsym.order.service.service.ProductService;
import com.kalsym.order.service.utility.OrderCalculation;
import static com.kalsym.order.service.utility.OrderCalculation.calculateStoreServiceCharges;
import com.kalsym.order.service.utility.Utilities;
import com.kalsym.order.service.utility.DateTimeUtil;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

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
    CartWithDetailsRepository cartWithDetailsRepository;

    @Autowired
    CartItemRepository cartItemRepository;
    
    @Autowired
    OrderItemRepository orderItemRepository;
    
    @Autowired
    OrderRepository orderRepository;
    
    @Autowired
    StoreDeliveryDetailRepository storeDeliveryDetailRepository;
    
    @Autowired
    StoreDiscountRepository storeDiscountRepository;
    
    @Autowired
    StoreDiscountTierRepository storeDiscountTierRepository;
    
    @Autowired
    CustomerVoucherRepository customerVoucherRepository;
    
    @Autowired
    ProductRepository productRepository;
    
    @Autowired
    RegionCountriesRepository regionCountriesRepository;
    
    @Autowired
    VoucherRepository voucherRepository;
     
    @Autowired
    DeliveryService deliveryService;
    
    @Autowired
    ProductService productService;
    
    @GetMapping(path = {""}, name = "carts-get", produces = "application/json")
    @PreAuthorize("hasAnyAuthority('carts-get', 'all')")
    public ResponseEntity<HttpResponse> getCarts(HttpServletRequest request,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String storeId,
            @RequestParam(required = false) String cartId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        String logprefix = request.getRequestURI() + " ";

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "carts-get request...");
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Cart cartMatch = new Cart();
        cartMatch.setCustomerId(customerId);
        cartMatch.setStoreId(storeId);
        cartMatch.setId(cartId);
        
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
    
    @GetMapping(path = {"/details"}, name = "carts-get", produces = "application/json")
    @PreAuthorize("hasAnyAuthority('carts-get', 'all')")
    public ResponseEntity<HttpResponse> getCartsWithDetails(HttpServletRequest request,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String storeId,
            @RequestParam(required = false) Boolean includeEmptyCart,
            @RequestParam(required = false) List<String> cartIdList,
            @RequestParam(required = false, defaultValue = "updated") String sortByCol,
            @RequestParam(required = false, defaultValue = "DESC") Sort.Direction sortingOrder,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        String logprefix = request.getRequestURI() + " ";

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "carts-get request...");
        HttpResponse response = new HttpResponse(request.getRequestURI());

        CartWithDetails cartMatch = new CartWithDetails();
        cartMatch.setCustomerId(customerId);
        cartMatch.setStoreId(storeId);         
        ExampleMatcher exampleCartMatcher = ExampleMatcher
                .matchingAll()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.EXACT);                
        Example<CartWithDetails> cartExample = Example.of(cartMatch, exampleCartMatcher);
        
        Specification cartSpec = CartSearchSpecs.getEmptyCart(includeEmptyCart, cartExample, cartIdList );
        
        Pageable pageable = null;        
        if (sortingOrder==Sort.Direction.ASC)
            pageable = PageRequest.of(page, pageSize, Sort.by(sortByCol).ascending());
        else if (sortingOrder==Sort.Direction.DESC)
            pageable = PageRequest.of(page, pageSize, Sort.by(sortByCol).descending());
        
        Page<CartWithDetails> cartWithPage = cartWithDetailsRepository.findAll(cartSpec, pageable);
        
        List<CartWithDetails> cartList = cartWithPage.getContent();
        CartWithDetails[] cartDetailsList = new CartWithDetails[cartList.size()];
        
        for (int i=0;i<cartList.size();i++) {
            CartWithDetails cartWithDetails = cartList.get(i);
            StoreSnooze storeSnooze=new StoreSnooze();
            StoreWithDetails store = cartWithDetails.getStore();
            if (store.getSnoozeStartTime()!=null && store.getSnoozeEndTime()!=null) {
                int result = store.getSnoozeEndTime().compareTo(Calendar.getInstance().getTime());
                if (result < 0) {
                    //snooze already expired
                    storeSnooze.isSnooze=false;
                } else {
                    storeSnooze.isSnooze=true;
                    storeSnooze.snoozeReason=store.getSnoozeReason();

                    //convert time to merchant timezone
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Snooze End Time:"+store.getSnoozeEndTime().toString());
                    Optional<RegionCountry> t = regionCountriesRepository.findById(store.getRegionCountryId());
                    if (t.isPresent()) {
                        RegionCountry regionCountry = t.get(); 
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Store timezone:"+regionCountry.getTimezone());
                        LocalDateTime startTime = DateTimeUtil.convertToLocalDateTimeViaInstant(store.getSnoozeStartTime(), ZoneId.of(regionCountry.getTimezone()));
                        LocalDateTime endTime = DateTimeUtil.convertToLocalDateTimeViaInstant(store.getSnoozeEndTime(), ZoneId.of(regionCountry.getTimezone()));
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Snooze End Time in store timezone:"+endTime);
                        storeSnooze.snoozeStartTime = startTime; 
                        storeSnooze.snoozeEndTime = endTime;
                    }
                }
            } else {
                storeSnooze.isSnooze=false;
            }
            cartWithDetails.setStoreSnooze(storeSnooze);
            
            cartDetailsList[i] = cartWithDetails; 
        }
        
         //create custom pageable object with modified content
        CustomPageable customPageable = new CustomPageable();
        customPageable.content = cartDetailsList;
        customPageable.pageable = cartWithPage.getPageable();
        customPageable.totalPages = cartWithPage.getTotalPages();
        customPageable.totalElements = cartWithPage.getTotalElements();
        customPageable.last = cartWithPage.isLast();
        customPageable.size = cartWithPage.getSize();
        customPageable.number = cartWithPage.getNumber();
        customPageable.sort = cartWithPage.getSort();        
        customPageable.numberOfElements = cartWithPage.getNumberOfElements();
        customPageable.first  = cartWithPage.isFirst();
        customPageable.empty = cartWithPage.isEmpty();
        response.setData(customPageable);
        
        response.setSuccessStatus(HttpStatus.OK);        
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
            response.setError("cart not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(optCart.get());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
   
    @GetMapping(path = {"/customer"}, name = "carts-get-by-id", produces = "application/json")
    @PreAuthorize("hasAnyAuthority('carts-get-by-id', 'all')")
    public ResponseEntity<HttpResponse> getCartsByCustomerId(HttpServletRequest request,
            @RequestParam(required = true) String customerId) {
        String logprefix = request.getRequestURI() + " ";

        HttpResponse response = new HttpResponse(request.getRequestURI());

        List<Cart> cartList = cartRepository.findByCustomerId(customerId);
        
        int totalItem=0;
        for (int i=0;i<cartList.size();i++) {
            Cart cart = cartList.get(i);
            List<CartItem> cartItemList = cartItemRepository.findByCartId(cart.getId());
            totalItem = totalItem + cartItemList.size();
        }
        
        class CustomerCart {
            List<Cart> cartList;
            int totalItem;
         
            public CustomerCart() {
            }

            public List<Cart> getCartList() {
                return cartList;
            }

            public void setCartList(List<Cart> cartList) {
                this.cartList = cartList;
            }
            
            public int getTotalItem() {
                return totalItem;
            }

            public void setTotalItem(int totalItem) {
                this.totalItem = totalItem;
            }
        }
        
        CustomerCart customerCart = new CustomerCart();
        customerCart.cartList = cartList;
        customerCart.totalItem = totalItem;
        
        response.setSuccessStatus(HttpStatus.OK);
        response.setData(customerCart);
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
            bodyCart.setStage(CartStage.CREATED);
            if (bodyCart.getServiceType()==null) {
                bodyCart.setServiceType(ServiceType.DELIVERIN);
            } 
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
     *
     * @param request
     * @param customerCartId
     * @param guestCartId
     * @return
     */
    @PutMapping(path = {"/{customerCartId}/{guestCartId}"}, name = "merge-cart")
    @PreAuthorize("hasAnyAuthority('carts-put-by-id', 'all')")
    public ResponseEntity<HttpResponse> mergeCart(HttpServletRequest request, 
            @PathVariable String customerCartId,
            @PathVariable String guestCartId) {
        String logprefix = request.getRequestURI() + " ";
        String location = Thread.currentThread().getStackTrace()[1].getMethodName();
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "", "customerCartId:"+customerCartId+" guestCustomerId:"+guestCartId);
        
        Optional<Cart> optCart = cartRepository.findById(customerCartId);

        if (!optCart.isPresent()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Customer Cart not found with cartId: " + customerCartId);
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        Cart customerCart = optCart.get();

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Customer cart found with cartId: " + customerCartId);
        
        Optional<Cart> optCart2 = cartRepository.findById(guestCartId);
        
        if (!optCart2.isPresent()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Guest Cart not found with cartId: " + guestCartId);
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        Cart guestCart = optCart2.get();

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Guest cart found with cartId: " + guestCartId);
        
        //copy all item from guest cart
        List<CartItem> cartItemList = cartItemRepository.findByCartId(guestCart.getId());
        for (int i=0;i<cartItemList.size();i++) {
            CartItem cartItem = cartItemList.get(i);
            cartItem.setCartId(customerCart.getId());
            cartItemRepository.save(cartItem);
        }
        
        //remove guest cart
        cartRepository.delete(guestCart);
        
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Customer cart updated for cartId: " + customerCartId);
        response.setSuccessStatus(HttpStatus.ACCEPTED);        
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
    
    /**
     *
     * @param request
     * @param customerId
     * @param guestCartIdList
     * @return
     */
    @PutMapping(path = {"/merge/{customerId}"}, name = "merge-cart")
    @PreAuthorize("hasAnyAuthority('carts-put-by-id', 'all')")
    public ResponseEntity<HttpResponse> mergeCartMultiple(HttpServletRequest request, 
            @PathVariable String customerId,
            @Valid @RequestBody List<String> guestCartIdList
        ) {
        String logprefix = "mergeCartMultiple()";
        String location = Thread.currentThread().getStackTrace()[1].getMethodName();
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "", "customerId:"+customerId+" guestCartIdList:"+guestCartIdList.size());
        
        for (int i=0;i<guestCartIdList.size();i++) {
            Optional<Cart> optCart = cartRepository.findById(guestCartIdList.get(i));

            if (optCart.isPresent()) {
                           
                //find customer cart
                Cart guestCart = optCart.get();
                String storeId = guestCart.getStoreId();
                Cart customerCart = null;
                List<Cart> customerCartList = cartRepository.findByCustomerIdAndStoreId(customerId, storeId);
                if (customerCartList.size()>0) {
                    customerCart = customerCartList.get(0);                                
                } else {
                    //create new cart
                    customerCart = new Cart();
                    customerCart.setCustomerId(customerId);
                    customerCart.setStoreId(storeId);
                    customerCart.setIsOpen(Boolean.TRUE);
                    customerCart.setStage(CartStage.CREATED);
                    customerCart = cartRepository.save(customerCart);
                }
            
                //copy all item from guest cart
                List<CartItem> cartItemList = cartItemRepository.findByCartId(guestCart.getId());
                for (int x=0;x<cartItemList.size();x++) {
                    CartItem cartItem = cartItemList.get(x);
                    cartItem.setCartId(customerCart.getId());
                    cartItemRepository.save(cartItem);
                }

                //remove guest cart
                cartRepository.delete(guestCart);
            }
        }
        
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Customer cart updated for customerId: " + customerId);
        response.setSuccessStatus(HttpStatus.ACCEPTED);        
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
            @PathVariable String id,
            @RequestParam(required = false) Boolean isCombinedDelivery
            ) {
        String logprefix = request.getRequestURI() + " ";

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "getWeightOfCart request");
        HttpResponse response = new HttpResponse(request.getRequestURI());

        try {
        
            List<CartItem> cartItems = cartItemRepository.findByCartId(id);
            
            Optional<Cart> cartOpt = cartRepository.findById(id);
            Cart cart  = null;
            if (cartOpt.isPresent()) {
                cart  = cartOpt.get();
                cart.setStage(CartStage.DELIVERY_CALCULATED);
                cartRepository.save(cart);
            }
            
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
                    

                    //check if any of item need a bigger vehicle
                    Optional<Product> productInfoOpt = productRepository.findById(cartItem.getProductId());
                    Product productInfo = productInfoOpt.get();
                    
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Product ["+productInfo.getName()+"] packing size:"+productInfo.getPackingSize());
                    int itemPcs = cartItem.getQuantity();
                    if (productInfo.getPackingSize()!=null && productInfo.getPackingSize().equalsIgnoreCase("XS")) {
                        itemPcs = 1;
                    }
                    totalPcs = totalPcs + itemPcs;
                    
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

            //check if item reach max for motorcycle
            if (vehicleType==VehicleType.MOTORCYCLE) {
                Optional<StoreDeliveryDetail> storeDelivery = storeDeliveryDetailRepository.findByStoreId(cart.getStoreId());
                if (totalPcs > storeDelivery.get().getMaxOrderQuantityForBike()) {
                    //convert to car
                    vehicleType=VehicleType.CAR;
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "TotalPcs:"+totalPcs+" is more than max for bike:"+storeDelivery.get().getMaxOrderQuantityForBike()+". Upgrade Vehicle type to CAR");
                } else {
                    //check if cart is combined delivery, if more than 10, convert to car
                    if (isCombinedDelivery!=null && isCombinedDelivery && totalPcs>10) {
                        //convert to car
                        vehicleType=VehicleType.CAR;
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "TotalPcs:"+totalPcs+" is than 10 for combined delivery. Upgrade Vehicle type to CAR");
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
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "TotalWeight : "+totalWeight+" Pcs:"+totalPcs+" Vehicle:"+vehicleType);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        
        } catch (Exception exp) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error getWeight", exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }

    }
    
    
    @GetMapping(path = {"/{id}/discount"}, name = "carts-discount-by-id", produces = "application/json")
    @PreAuthorize("hasAnyAuthority('carts-discount-by-id', 'all')")
    public ResponseEntity<HttpResponse> getDiscountOfCart(HttpServletRequest request,
            @PathVariable String id,
            @RequestParam(defaultValue = "0") Double deliveryCharge,
            @RequestParam(required = false) String deliveryQuotationId,
            @RequestParam(required = false) String deliveryType,
            @RequestParam(required = false) String voucherCode,  
            @RequestParam(required = false) String storeVoucherCode,  
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String storeId
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
        
        Cart cart  = cartOptional.get();
        cart.setStage(CartStage.CHARGES_CALCULATED);
        cartRepository.save(cart);        
        
        //check platform voucher code if provided
        CustomerVoucher customerPlatformVoucher = null;
        if (voucherCode!=null) {
            //check claimed voucher
            if (customerId!=null) {
                customerPlatformVoucher = customerVoucherRepository.findCustomerPlatformVoucherByCode(customerId, voucherCode, new Date());
            }
            if (customerPlatformVoucher==null) {
                //find guest voucher
                Voucher guestVoucher = customerVoucherRepository.findGuestPlatformVoucherByCode(voucherCode, new Date());
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Platform Guest Voucher:"+guestVoucher);
                if (guestVoucher!=null) {
                    //check if already redeem
                    if (email!=null) {
                        List<CustomerVoucher> usedVoucherList = customerVoucherRepository.findByGuestEmailAndVoucherId(email, guestVoucher.getId());
                        if (usedVoucherList.size()>0) {                        
                            CustomerVoucher usedVoucher = usedVoucherList.get(0);
                            if (usedVoucher.getIsUsed() && !guestVoucher.getAllowMultipleRedeem()) {
                                //already used
                                response.setStatus(HttpStatus.NOT_FOUND.value());
                                response.setMessage("Voucher code " + voucherCode + " already used");
                                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                            } else {
                                customerPlatformVoucher = usedVoucher;
                            }
                        } else {
                            customerPlatformVoucher = new CustomerVoucher();
                            customerPlatformVoucher.setGuestEmail(email);
                            customerPlatformVoucher.setIsUsed(false);
                            customerPlatformVoucher.setVoucherId(guestVoucher.getId());
                            customerPlatformVoucher.setCreated(new Date());
                            customerPlatformVoucher.setGuestVoucher(true);
                            customerVoucherRepository.save(customerPlatformVoucher);
                        }
                    } else {
                        response.setStatus(HttpStatus.NOT_FOUND.value());
                        response.setMessage("Please insert email");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                    }
                } else {
                    response.setStatus(HttpStatus.NOT_FOUND.value());
                    response.setMessage("Platform Voucher code " + voucherCode + " not found");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
            } 
        }  
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Platform voucher:"+customerPlatformVoucher);
         
        //check store voucher code if provided
        CustomerVoucher customerStoreVoucher = null;
        if (storeVoucherCode!=null) {
            //check claimed voucher
            if (customerId!=null) {
                customerStoreVoucher = customerVoucherRepository.findCustomerStoreVoucherByCode(customerId, storeVoucherCode, new Date());
            }            
            if (customerStoreVoucher==null) {                
                //find guest voucher
                Voucher guestVoucher = customerVoucherRepository.findGuestStoreVoucherByCode(storeVoucherCode, new Date());
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Store Guest Voucher:"+guestVoucher);
                if (guestVoucher!=null) {
                    //check if already redeem
                    if (email!=null) {
                        List<CustomerVoucher> usedVoucherList = null;
                        if (storeId!=null) {
                            usedVoucherList = customerVoucherRepository.findByGuestEmailAndVoucherIdAndStoreId(email, guestVoucher.getId(), storeId);
                        } else {
                            usedVoucherList = customerVoucherRepository.findByGuestEmailAndVoucherId(email, guestVoucher.getId());
                        }
                        if (usedVoucherList.size()>0) {                        
                            CustomerVoucher usedVoucher = usedVoucherList.get(0);
                            if (usedVoucher.getIsUsed() && !guestVoucher.getAllowMultipleRedeem()) {
                                //already used
                                response.setStatus(HttpStatus.NOT_FOUND.value());
                                response.setMessage("Sorry, you have redeemed this voucher");
                                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                            } else {
                                customerStoreVoucher = usedVoucher;
                            }
                        } else {
                            customerStoreVoucher = new CustomerVoucher();
                            customerStoreVoucher.setGuestEmail(email);
                            customerStoreVoucher.setIsUsed(false);
                            customerStoreVoucher.setVoucherId(guestVoucher.getId());
                            customerStoreVoucher.setVoucher(guestVoucher);
                            customerStoreVoucher.setCreated(new Date());
                            customerStoreVoucher.setGuestVoucher(true);
                            customerVoucherRepository.save(customerStoreVoucher);
                        }
                    } else {
                        response.setStatus(HttpStatus.NOT_FOUND.value());
                        response.setMessage("Please insert email");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                    }
                } else {
                    response.setStatus(HttpStatus.NOT_FOUND.value());
                    response.setMessage("Store Voucher code " + storeVoucherCode + " not found");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
                
            } 
        }   
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Store voucher:"+customerStoreVoucher);
        
        //get delivery charges from delivery-service
        if (deliveryQuotationId!=null) {
            DeliveryQuotation deliveryQuotation = deliveryService.getDeliveryQuotation(deliveryQuotationId);
            deliveryCharge = deliveryQuotation.getAmount();
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "DeliveryCharge from delivery-service:"+deliveryCharge);
        }
        
        try {
             
            //getting store details for cart if from product service
            StoreWithDetails storeWithDetials = productService.getStoreById(cart.getStoreId());
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got store details of cartId: " + cart.getId() + ", and storeId: " + cart.getStoreId());

            StoreCommission storeCommission = productService.getStoreCommissionByStoreId(cart.getStoreId());
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got store commission: " + storeCommission);

            
            Discount discount = StoreDiscountCalculation.CalculateStoreDiscount(cart, deliveryCharge, cartItemRepository, storeDiscountRepository, storeDiscountTierRepository, logprefix, null);        
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cartId:"+id+" deliveryCharge:"+deliveryCharge+" totalSubTotalDiscount:"+discount.getSubTotalDiscount()+" totalShipmentDiscount:"+discount.getDeliveryDiscount());
            
            List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
            
            OrderObject orderTotalObject = OrderCalculation.CalculateOrderTotal(cart, storeWithDetials.getServiceChargesPercentage(), storeCommission,  
                            deliveryCharge, deliveryType, customerPlatformVoucher, customerStoreVoucher, storeWithDetials.getVerticalCode(),
                            cartItemRepository, storeDiscountRepository, storeDiscountTierRepository, logprefix, cartItems);                
            
            if (orderTotalObject.getGotError()) {
                // should return warning if got error
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error while calculating discount:"+orderTotalObject.getErrorMessage());
                response.setSuccessStatus(HttpStatus.EXPECTATION_FAILED);
                response.setMessage(orderTotalObject.getErrorMessage());
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
            }

            discount.setCartGrandTotal(Utilities.roundDouble(orderTotalObject.getTotal(),2));
            discount.setCartDeliveryCharge(Utilities.roundDouble(deliveryCharge,2));
            discount.setStoreServiceCharge(Utilities.roundDouble(orderTotalObject.getStoreServiceCharge(),2));
            discount.setStoreServiceChargePercentage(Utilities.roundDouble(storeWithDetials.getServiceChargesPercentage(),2));
            
            if (customerPlatformVoucher!=null) {
                discount.setVoucherSubTotalDiscount(orderTotalObject.getVoucherSubTotalDiscount());
                discount.setVoucherSubTotalDiscountDescription(orderTotalObject.getVoucherSubTotalDiscountDescription());
                discount.setVoucherDeliveryDiscount(orderTotalObject.getVoucherDeliveryDiscount());
                discount.setVoucherDeliveryDiscountDescription(orderTotalObject.getVoucherDeliveryDiscountDescription());
                discount.setVoucherDiscountType(orderTotalObject.getVoucherDiscountType());
                discount.setVoucherDiscountCalculationType(orderTotalObject.getVoucherDiscountCalculationType());
                discount.setVoucherDiscountMaxAmount(orderTotalObject.getVoucherDiscountMaxAmount());
                discount.setVoucherDiscountCalculationType(orderTotalObject.getVoucherDiscountCalculationType());
                discount.setVoucherDiscountCalculationValue(orderTotalObject.getVoucherDiscountCalculationValue());
            }
            
            if (customerStoreVoucher!=null) {
                discount.setStoreVoucherSubTotalDiscount(orderTotalObject.getStoreVoucherSubTotalDiscount());
                discount.setStoreVoucherSubTotalDiscountDescription(orderTotalObject.getStoreVoucherSubTotalDiscountDescription());
                discount.setStoreVoucherDeliveryDiscount(orderTotalObject.getStoreVoucherDeliveryDiscount());
                discount.setStoreVoucherDeliveryDiscountDescription(orderTotalObject.getStoreVoucherDeliveryDiscountDescription());
                discount.setStoreVoucherDiscountType(orderTotalObject.getStoreVoucherDiscountType());
                discount.setStoreVoucherDiscountCalculationType(orderTotalObject.getStoreVoucherDiscountCalculationType());
                discount.setStoreVoucherDiscountMaxAmount(orderTotalObject.getStoreVoucherDiscountMaxAmount());
                discount.setStoreVoucherDiscountCalculationType(orderTotalObject.getStoreVoucherDiscountCalculationType());
                discount.setStoreVoucherDiscountCalculationValue(orderTotalObject.getStoreVoucherDiscountCalculationValue());
            }
            
            response.setSuccessStatus(HttpStatus.OK);
            response.setData(discount);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        
        } catch (Exception exp) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error calculate discount", exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }

    }
    
    
    @PostMapping(path = {"/groupdiscount"}, name = "carts-discount-by-group", produces = "application/json")
    @PreAuthorize("hasAnyAuthority('carts-discount-by-group', 'all')")
    public ResponseEntity<HttpResponse> getDiscountOfCartGroup(HttpServletRequest request,
                @Valid @RequestBody CartWithItem[] cartList,
                @RequestParam(required = false) String platformVoucherCode,
                @RequestParam(required = false) String customerId,
                @RequestParam(required = false) String email
            ) throws Exception {
                          
        String logprefix = request.getRequestURI() + " ";        
        
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "getDiscountOfCartGroup request. CartList count:"+cartList.length);
        HttpResponse response = new HttpResponse(request.getRequestURI());
        
        for (int i=0;i<cartList.length;i++) {
            String cartId = cartList[i].getCartId();
            Optional<Cart> cartOptional = cartRepository.findById(cartId);
            if (!cartOptional.isPresent()) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Cart not found with cartId: " + cartId);
                response.setErrorStatus(HttpStatus.NOT_FOUND);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        }        
        
        //check platform voucher code if provided
        CustomerVoucher customerPlatformVoucher = null;
        if (platformVoucherCode!=null && (customerId!=null || email!=null)) {
            //check voucher in customer account
            customerPlatformVoucher = customerVoucherRepository.findCustomerPlatformVoucherByCode(customerId, platformVoucherCode, new Date());
            if (customerPlatformVoucher==null) {
                //find guest voucher
                Voucher guestVoucher = customerVoucherRepository.findGuestPlatformVoucherByCode(platformVoucherCode, new Date());
                if (guestVoucher!=null) {
                    //check if already redeem
                    List<CustomerVoucher> usedVoucherList = customerVoucherRepository.findByGuestEmailAndVoucherId(email, guestVoucher.getId());
                    if (usedVoucherList.size()>0) {  
                        CustomerVoucher usedVoucher = usedVoucherList.get(0);
                        if (usedVoucher.getIsUsed() && !guestVoucher.getAllowMultipleRedeem()) {
                            //already used
                            response.setStatus(HttpStatus.NOT_FOUND.value());
                            response.setMessage("Voucher code " + platformVoucherCode + " already used");
                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                        }                        
                    } 
                    customerPlatformVoucher = new CustomerVoucher();
                    customerPlatformVoucher.setGuestEmail(email);
                    customerPlatformVoucher.setIsUsed(false);
                    customerPlatformVoucher.setVoucherId(guestVoucher.getId());
                    customerPlatformVoucher.setVoucher(guestVoucher);
                    customerPlatformVoucher.setGuestVoucher(true);
                } else {
                    response.setStatus(HttpStatus.NOT_FOUND.value());
                    response.setMessage("Voucher code " + platformVoucherCode + " not found");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
            } 
        }  
        
        double groupDeliveryCharge=0;
        double groupCartSubTotal=0;
        double groupServiceCharge=0;
        double groupDeliveryDiscount=0;
        double groupSubTotalDiscount=0;
        boolean gotCartItemDiscount=false;
        
        List<Discount> storeDiscountList = new ArrayList();
        Map<String, Double> combinedDeliveryFeeMap = new HashMap<String, Double>();
        for (int i=0;i<cartList.length;i++) {            
            String cartId = cartList[i].getCartId();
            Optional<Cart> cartOptional = cartRepository.findById(cartId);
            Cart cart = cartOptional.get();
            String deliveryQuotationId = cartList[i].getDeliveryQuotationId();
            String deliveryType = cartList[i].getDeliveryType();
            String storeVoucherCode = cartList[i].getStoreVoucherCode();
             
            //get delivery charges from delivery-service
            double deliveryCharge=0;
            if (deliveryQuotationId!=null) {
                DeliveryQuotation deliveryQuotation = deliveryService.getDeliveryQuotation(deliveryQuotationId);
                deliveryCharge = deliveryQuotation.getAmount();
                if (deliveryQuotation.getCombinedDelivery()!=null && deliveryQuotation.getCombinedDelivery()) {
                   combinedDeliveryFeeMap.put(deliveryQuotationId, deliveryCharge); 
                } else {
                    groupDeliveryCharge = groupDeliveryCharge + deliveryCharge;
                }
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "DeliveryCharge from delivery-service:"+deliveryCharge);
            }            
            
            //check store voucher if provided
            CustomerVoucher customerStoreVoucher = null;
            if (storeVoucherCode!=null && customerId!=null) {
                customerStoreVoucher = customerVoucherRepository.findCustomerStoreVoucherByCode(customerId, storeVoucherCode, new Date());
                if (customerStoreVoucher==null) {
                    response.setStatus(HttpStatus.NOT_FOUND.value());
                    response.setMessage("Voucher code " + storeVoucherCode + " not found");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                } 
            }        
        
            //getting store details for cart if from product service
            StoreWithDetails storeWithDetials = productService.getStoreById(cart.getStoreId());
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got store details of cartId: " + cart.getId() + ", and storeId: " + cart.getStoreId());

            StoreCommission storeCommission = productService.getStoreCommissionByStoreId(cart.getStoreId());
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got store commission: " + storeCommission);
            
            List<CartItem> selectedCartItem = new ArrayList<>();
            for (int x=0;x<cartList[i].getSelectedItemId().size();x++) {
                String itemId = cartList[i].getSelectedItemId().get(x);
                Optional<CartItem> cartItemOpt = cartItemRepository.findById(itemId);
                if (cartItemOpt.isPresent()) {
                    selectedCartItem.add(cartItemOpt.get());
                    if (cartItemOpt.get().getDiscountId()!=null) {
                        gotCartItemDiscount=true;
                    }
                }
            }
            
            Discount discount = StoreDiscountCalculation.CalculateStoreDiscount(cart, deliveryCharge, cartItemRepository, storeDiscountRepository, storeDiscountTierRepository, logprefix, selectedCartItem);        
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cartId:"+cartId+" deliveryCharge:"+deliveryCharge+" totalSubTotalDiscount:"+discount.getSubTotalDiscount()+" totalShipmentDiscount:"+discount.getDeliveryDiscount());
            
            OrderObject orderTotalObject = OrderCalculation.CalculateOrderTotal(cart, storeWithDetials.getServiceChargesPercentage(), storeCommission,  
                            deliveryCharge, deliveryType, null, customerStoreVoucher, storeWithDetials.getVerticalCode(),
                            cartItemRepository, storeDiscountRepository, storeDiscountTierRepository, logprefix, selectedCartItem);                
            
            if (orderTotalObject.getGotError()) {
                // should return warning if got error
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error while calculating discount:"+orderTotalObject.getErrorMessage());
                response.setSuccessStatus(HttpStatus.EXPECTATION_FAILED);
                response.setMessage(orderTotalObject.getErrorMessage());
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
            }
            
            discount.setCartId(cartId);
            discount.setCartGrandTotal(Utilities.roundDouble(orderTotalObject.getTotal(),2));
            discount.setCartDeliveryCharge(Utilities.roundDouble(deliveryCharge,2));
            discount.setStoreServiceCharge(Utilities.roundDouble(orderTotalObject.getStoreServiceCharge(),2));
            discount.setStoreServiceChargePercentage(Utilities.roundDouble(storeWithDetials.getServiceChargesPercentage(),2));
            
            if (customerStoreVoucher!=null) {
                discount.setStoreVoucherSubTotalDiscount(orderTotalObject.getStoreVoucherSubTotalDiscount());
                discount.setStoreVoucherSubTotalDiscountDescription(orderTotalObject.getStoreVoucherSubTotalDiscountDescription());
                discount.setStoreVoucherDeliveryDiscount(orderTotalObject.getStoreVoucherDeliveryDiscount());
                discount.setStoreVoucherDeliveryDiscountDescription(orderTotalObject.getStoreVoucherDeliveryDiscountDescription());
                discount.setStoreVoucherDiscountType(orderTotalObject.getStoreVoucherDiscountType());
                discount.setStoreVoucherDiscountCalculationType(orderTotalObject.getStoreVoucherDiscountCalculationType());
                discount.setStoreVoucherDiscountMaxAmount(orderTotalObject.getStoreVoucherDiscountMaxAmount());
                discount.setStoreVoucherDiscountCalculationType(orderTotalObject.getStoreVoucherDiscountCalculationType());
                discount.setStoreVoucherDiscountCalculationValue(orderTotalObject.getStoreVoucherDiscountCalculationValue());
            }
            storeDiscountList.add(discount); 
            
            groupCartSubTotal = groupCartSubTotal + Utilities.convertToDouble(discount.getCartSubTotal());
            groupServiceCharge = groupServiceCharge + Utilities.convertToDouble(discount.getStoreServiceCharge());
            groupDeliveryDiscount = groupDeliveryDiscount + Utilities.convertToDouble(discount.getDeliveryDiscount());            
            groupSubTotalDiscount = groupSubTotalDiscount + Utilities.convertToDouble(discount.getSubTotalDiscount());
        }
        
        for (Map.Entry<String, Double> combinedDelivery :
            combinedDeliveryFeeMap.entrySet()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "DeliveryQuotationId:"+combinedDelivery.getKey()+" Amount:"+combinedDelivery.getValue());
            groupDeliveryCharge = groupDeliveryCharge + combinedDelivery.getValue();
        }
       
        GroupDiscount groupDiscount = new GroupDiscount();
        groupDiscount.setStoreDiscountList(storeDiscountList);
        
        OrderObject groupOrderTotalObject = OrderCalculation.CalculateGroupOrderTotal(
                        groupCartSubTotal,
                        groupSubTotalDiscount,
                        groupDeliveryCharge,
                        groupDeliveryDiscount,
                        customerPlatformVoucher, 
                        groupServiceCharge,
                        logprefix, gotCartItemDiscount);                

        if (groupOrderTotalObject.getGotError()) {
            // should return warning if got error
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error while calculating discount:"+groupOrderTotalObject.getErrorMessage());
            response.setSuccessStatus(HttpStatus.EXPECTATION_FAILED);
            response.setMessage(groupOrderTotalObject.getErrorMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }
        
        groupDiscount.setSumCartSubTotal(Utilities.roundDouble(groupCartSubTotal, 2));
        groupDiscount.setSumCartGrandTotal(Utilities.roundDouble(groupOrderTotalObject.getTotal(),2));
        groupDiscount.setSumCartDeliveryCharge(Utilities.roundDouble(groupDeliveryCharge,2));
        groupDiscount.setSumServiceCharge(Utilities.roundDouble(groupServiceCharge,2));
        groupDiscount.setSumDeliveryDiscount(Utilities.roundDouble(groupDeliveryDiscount,2));
        groupDiscount.setSumSubTotalDiscount(Utilities.roundDouble(groupSubTotalDiscount, 2));
        
        if (customerPlatformVoucher!=null) {
            groupDiscount.setPlatformVoucherSubTotalDiscount(groupOrderTotalObject.getVoucherSubTotalDiscount());
            groupDiscount.setPlatformVoucherSubTotalDiscountDescription(groupOrderTotalObject.getVoucherSubTotalDiscountDescription());
            groupDiscount.setPlatformVoucherDeliveryDiscount(groupOrderTotalObject.getVoucherDeliveryDiscount());
            groupDiscount.setPlatformVoucherDeliveryDiscountDescription(groupOrderTotalObject.getVoucherDeliveryDiscountDescription());
            groupDiscount.setPlatformVoucherDiscountType(groupOrderTotalObject.getVoucherDiscountType());
            groupDiscount.setPlatformVoucherDiscountCalculationType(groupOrderTotalObject.getVoucherDiscountCalculationType());
            groupDiscount.setPlatformVoucherDiscountMaxAmount(groupOrderTotalObject.getVoucherDiscountMaxAmount());
            groupDiscount.setPlatformVoucherDiscountCalculationType(groupOrderTotalObject.getVoucherDiscountCalculationType());
            groupDiscount.setPlatformVoucherDiscountCalculationValue(groupOrderTotalObject.getVoucherDiscountCalculationValue());
            groupDiscount.setPlatformVoucherId(groupOrderTotalObject.getVoucherId());
            Optional<Voucher> voucherOpt = voucherRepository.findById(groupOrderTotalObject.getVoucherId());
            if (voucherOpt.isPresent()) {
                groupDiscount.setPlatformVoucherName(voucherOpt.get().getName());
            }
        }
        
        response.setSuccessStatus(HttpStatus.OK);
        response.setData(groupDiscount);
        return ResponseEntity.status(HttpStatus.OK).body(response);        

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
            
            Discount discount = StoreDiscountCalculation.CalculateStoreDiscount(cart, 0.00, cartItemRepository, storeDiscountRepository, storeDiscountTierRepository, logprefix, null);        
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
