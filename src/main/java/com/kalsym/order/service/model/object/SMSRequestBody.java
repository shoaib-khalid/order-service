package com.kalsym.order.service.model.object;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SMSRequestBody {
    String orderId;
    String phoneNumber;
}
