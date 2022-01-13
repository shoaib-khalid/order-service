/*
 * Copyright (C) 2022 taufik
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
package com.kalsym.order.service.model.repository;

/**
 *
 * @author taufik
 */
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.CrudRepository;

import java.io.Serializable;
import java.util.Optional;

@NoRepositoryBean
public interface CustomRepository<T, ID extends Serializable>
extends CrudRepository<T, ID> {
    void refresh(T t);
}
