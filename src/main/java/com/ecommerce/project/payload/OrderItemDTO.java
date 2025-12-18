package com.ecommerce.project.payload;

import com.ecommerce.project.model.Order;
import com.ecommerce.project.model.Product;
import jakarta.persistence.*;
import jdk.jfr.Name;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {

    private Long orderItemId;
    private Product product;
    private Integer quantity;
    private Double discount;
    private Double orderedProductPrice;
}
