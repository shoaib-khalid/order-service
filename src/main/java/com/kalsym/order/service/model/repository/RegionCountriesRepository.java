package com.kalsym.order.service.model.repository;

import com.kalsym.order.service.model.RegionCountry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author 7cu
 */
@Repository
public interface RegionCountriesRepository extends PagingAndSortingRepository<RegionCountry, String>, JpaRepository<RegionCountry, String> {

}
