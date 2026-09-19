package startup.vn.pharmacyservice.services;

import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;

@Service
@RefreshScope
public class InsuranceService {

    @TimeLimiter(name = "insuranceValidation")
    @CircuitBreaker(name = "insuranceValidation", fallbackMethod = "insuranceFallback")
    @Retry(name = "insuranceValidation")
    public CompletableFuture<InsuranceResult> validate(BillService.BillRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            if (request.simulateInsuranceTimeout()) {
                sleep(5000);
            }
            if (request.simulateInsuranceFailure()) {
                throw new ResourceAccessException("Insurance server is unavailable");
            }
            return new InsuranceResult(true, request.insuranceDiscountRate());
        });
    }

    private CompletableFuture<InsuranceResult> insuranceFallback(
            BillService.BillRequest request,
            Throwable throwable) {
        return CompletableFuture.completedFuture(InsuranceResult.unavailable());
    }

    private void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ResourceAccessException("Insurance validation was interrupted");
        }
    }

    public record InsuranceResult(boolean validated, java.math.BigDecimal discountRate) {
        static InsuranceResult unavailable() {
            return new InsuranceResult(false, java.math.BigDecimal.ZERO);
        }
    }
}
