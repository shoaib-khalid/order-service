package com.kalsym.order.service.model.repository;

import com.kalsym.order.service.model.OrderCompletionStatusConfig;
import com.kalsym.order.service.model.OrderCompletionStatusConfigId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
//import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 *
 * @author 7cu
 */
@Repository
public interface OrderCompletionStatusConfigRepository extends PagingAndSortingRepository<OrderCompletionStatusConfig, OrderCompletionStatusConfigId>,
        JpaRepository<OrderCompletionStatusConfig, OrderCompletionStatusConfigId> {

    public List<OrderCompletionStatusConfig> findByVerticalIdAndStatusAndStorePickupAndStoreDeliveryTypeAndPaymentType(
            String verticalId,
            String status,
            Boolean storePickup,
            String storeDeliveryType,
            String orderPaymentType
    );
    
    
    public List<OrderCompletionStatusConfig> findByVerticalIdAndStatusSequenceAndStorePickupAndStoreDeliveryTypeAndPaymentType(
            String verticalId,
            int statusSequence,
            Boolean storePickup,
            String storeDeliveryType,
            String orderPaymentType
    );
    
    
    public List<OrderCompletionStatusConfig> findByVerticalIdAndStatusAndPaymentType(
            String verticalId,
            String status,
            String orderPaymentType
    );

    public List<OrderCompletionStatusConfig> findByVerticalIdAndStatusAndStoreDeliveryType(
            String verticalId,
            String status,
            String deliveryType
    );

}
