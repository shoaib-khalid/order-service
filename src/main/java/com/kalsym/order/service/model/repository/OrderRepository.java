package com.kalsym.order.service.model.repository;

import com.kalsym.order.service.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
//import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.List;

/**
 *
 * @author 7cu
 */
@Repository
public interface OrderRepository extends PagingAndSortingRepository<Order, String>, JpaRepository<Order, String>, JpaSpecificationExecutor<Order> {

//    List<Order> findByCustomerId(@Param("customerId") String customerId);
    
//    Page<Order> findAllByCreatedBetween(Date from, Date to, Pageable pageable);
    
    @Query("SELECT c.completionStatus, COUNT(c.id) FROM Order AS c GROUP BY c.completionStatus")
    List<Object[]> getCountSummary();
    
    /*
    public List<Order> findByConditions(String name, Integer price, Integer stock) {  
        messageRequestRepository.findAll((Specification<MessageRequest>) (itemRoot, query, criteriaBuilder) -> {
            //List is used here to store various query conditions for dynamic query
            List<Predicate> predicatesList = new ArrayList<>();
            //name fuzzy query, like statement
            if (name != null) {
                predicatesList.add(
                    criteriaBuilder.and(
                        criteriaBuilder.like(
                            itemRoot.get("name"), "%" + name + "%")));
            }
            // itemPrice less than or equal to <=statement
            if (price != null) {
                predicatesList.add(
                    criteriaBuilder.and(
                        criteriaBuilder.le(
                            itemRoot.get("price"), price)));
            }
            //itemStock greater than or equal to >=statement
            if (stock != null) {
                predicatesList.add(
                    criteriaBuilder.and(
                        criteriaBuilder.ge(
                            itemRoot.get("stock"), stock)));
            }
            //where() splicing query criteria
            query.where(predicatesList.toArray(new Predicate[predicatesList.size()]));
            //Return the redicate assembled with CriteriaQuery
            return query.getRestriction();
        });
    }*/
}
