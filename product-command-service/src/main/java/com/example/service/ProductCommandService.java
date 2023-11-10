package com.example.service;

import java.util.Optional;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.dto.ProductEvent;
import com.example.entity.Product;
import com.example.repository.ProductRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ProductCommandService {

    private ProductRepository repository;

    private KafkaTemplate<String, Object> kafkaTemplate;

    public Product createProduct(ProductEvent productEvent) {

        final Product productDO = repository.save(productEvent.getProduct());
        ProductEvent event = new ProductEvent("CreateProduct", productDO);
        kafkaTemplate.send("product-event-topic", event);
        return productDO;
    }

    public Product updateProduct(long id, ProductEvent productEvent) {

        final Optional<Product> optionalProduct = repository.findById(id);

        if (optionalProduct.isPresent()) {

            Product product = productEvent.getProduct();

            final Product existingProduct = optionalProduct.get();
            existingProduct.setName(product.getName());
            existingProduct.setPrice(product.getPrice());
            existingProduct.setDescription(product.getDescription());

            Product productDO = repository.save(existingProduct);
            ProductEvent event = new ProductEvent("UpdateProduct", productDO);
            kafkaTemplate.send("product-event-topic", event);

            return productDO;
        }

        return null;
    }

}
