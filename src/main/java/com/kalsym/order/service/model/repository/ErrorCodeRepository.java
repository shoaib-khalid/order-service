package com.kalsym.order.service.model.repository;

import com.kalsym.order.service.model.ErrorCode;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author 7cu
 */
@Repository
public interface ErrorCodeRepository extends PagingAndSortingRepository<ErrorCode, String>, JpaRepository<ErrorCode, String> {
    public Optional<ErrorCode> findByModulesAndErrorCategoryAndErrorCode(String modules, String errorCategory, String errorCode);
}
