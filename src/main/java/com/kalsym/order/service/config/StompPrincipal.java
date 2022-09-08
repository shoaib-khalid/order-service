/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.config;

import java.security.Principal;

/**
 *
 * @author taufik
 */
public class StompPrincipal implements Principal {

  private String name;

  public StompPrincipal(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }
}