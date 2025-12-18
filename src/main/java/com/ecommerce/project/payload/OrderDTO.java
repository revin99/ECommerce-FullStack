package com.ecommerce.project.payload;

import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.OrderItem;
import com.ecommerce.project.model.Payment;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    private Long orderId;
    private String email;
    private List<OrderItem> orderItems = new ArrayList<>();
    private LocalDate orderDate;
    private Payment payment;
    private Double totalAmount;
    private String orderStatus;
    private Address address;
}
