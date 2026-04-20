package com.stockwatch.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 2048)
    private String url;

    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private boolean notified = false;

    @Column(nullable = false)
    private boolean active = true;

    @Column
    private LocalDateTime lastChecked;

    @Column
    private LocalDateTime lastInStock;

    @Column
    private LocalDateTime createdAt = LocalDateTime.now();

    public Product(String name, String url, String email) {
        this.name = name;
        this.url = url;
        this.email = email;
    }
}
