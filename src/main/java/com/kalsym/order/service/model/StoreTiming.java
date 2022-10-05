/*
 * Copyright (C) 2021 saros
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.kalsym.order.service.model;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author saros
 */
@Entity
@Getter
@Setter
@IdClass(StoreTimingIdentity.class)
@Table(name = "store_timing")
@ToString
public class StoreTiming implements Serializable {

    @Id
    private String storeId;
    @Id
    private String day;

    private String openTime;

    private String closeTime;

    private Boolean isOff;
    
    private String breakStartTime;
    
    private String breakEndTime;
    
    public void update(StoreTiming storeTiming) {
        if (null != storeTiming.getOpenTime()) {
            this.openTime = storeTiming.getOpenTime();
        }
        if (null != storeTiming.getCloseTime()) {
            this.closeTime = storeTiming.getCloseTime();
        }
        if (null != storeTiming.getIsOff()) {
            this.isOff = storeTiming.getIsOff();
        }
    }

}
