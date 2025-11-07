package com.arka.cart_service.infrastructure.persistence.repositories;

import com.arka.cart_service.domain.entities.CartStatus;
import com.arka.cart_service.infrastructure.persistence.model.CartDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CartMongoRepository extends MongoRepository<CartDocument, String> {
    Optional<CartDocument> findByCustomerIdAndStatus(Long customerId, CartStatus status);
    List<CartDocument> findByStatus(CartStatus status);

    @Query("{ 'status': 'ACTIVE', 'lastUpdatedAt': { $lt: ?0 } }")
    List<CartDocument> findAbandonedCarts(LocalDateTime thresholdDate);
}
