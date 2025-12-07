package com.ecommerce.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long productId;
    private String productName;
    private String description;
    private String image;
    private Integer quantity;
    private Double price; //100
    private Double discount; //25
    private Double specialPrice; //75

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}
