package com.arka.cart_service.infrastructure.scheduler;

import com.arka.cart_service.application.dto.AbandonedCartDTO;
import com.arka.cart_service.application.usecases.GetAbandonedCartsUseCase;
import com.arka.cart_service.domain.entities.Cart;
import com.arka.cart_service.domain.repositories.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "carts.abandoned.schedule.enabled", havingValue = "true", matchIfMissing = true)
public class AbandonedCartScheduler {

    private final GetAbandonedCartsUseCase getAbandonedCartsUseCase;
    private final CartRepository cartRepository;

    @Scheduled(cron = "${carts.abandoned.schedule.cron:0 0 9 * * *}")
    public void checkAbandonedCarts() {
        log.info("Starting abandoned cart detection job...");

        try {
            List<AbandonedCartDTO> abandonedCarts = getAbandonedCartsUseCase.execute();

            if (!abandonedCarts.isEmpty()) {
                log.info("Detected {} abandoned carts", abandonedCarts.size());

                // Marcar carritos como abandonados
                for (AbandonedCartDTO abandonedCart : abandonedCarts) {
                    cartRepository.findById(abandonedCart.getCartId()).ifPresent(cart -> {
                        cart.markAsAbandoned();
                        cartRepository.save(cart);
                        log.debug("Cart {} marked as abandoned", cart.getId());
                    });
                }

                log.info("Abandoned cart detection job completed: {} carts marked", abandonedCarts.size());
            } else {
                log.info("No abandoned carts detected");
            }
        } catch (Exception e) {
            log.error("Error in abandoned cart detection job: {}", e.getMessage(), e);
        }
    }
}
