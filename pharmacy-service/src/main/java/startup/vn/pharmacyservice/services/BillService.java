package startup.vn.pharmacyservice.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;

@Service
public class BillService {

    private final InsuranceService insuranceService;

    public BillService(InsuranceService insuranceService) {
        this.insuranceService = insuranceService;
    }

    @RateLimiter(name = "invoiceRateLimiter", fallbackMethod = "rateLimitFallback")
    @Retry(name = "invoiceRetry", fallbackMethod = "retryFallback")
    public BillResponse createBill(BillRequest request, BigDecimal vatRate) {
        if (request.simulateNetworkFailure()) {
            throw new ResourceAccessException("Electronic invoice provider is unavailable");
        }

        BigDecimal medicineTotal = request.medicineTotal();
        BigDecimal vatAmount = medicineTotal
                .multiply(vatRate)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = medicineTotal
                .add(vatAmount)
                .setScale(2, RoundingMode.HALF_UP);

        return new BillResponse(
                medicineTotal,
                vatRate,
                vatAmount,
                total,
                "ISSUED",
                "Electronic invoice created");
    }

    public CompletableFuture<BillResponse> createBillWithInsurance(
            BillRequest request,
            BigDecimal vatRate) {
        return insuranceService.validate(request)
                .thenApply(insurance -> calculateInsuranceBill(request, vatRate, insurance));
    }

    private BillResponse calculateInsuranceBill(
            BillRequest request,
            BigDecimal vatRate,
            InsuranceService.InsuranceResult insurance) {
        BigDecimal medicineTotal = request.medicineTotal();
        BigDecimal discount = insurance.validated()
                ? medicineTotal.multiply(insurance.discountRate()).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2);
        BigDecimal total = medicineTotal.subtract(discount).setScale(2, RoundingMode.HALF_UP);

        return new BillResponse(
                medicineTotal,
                vatRate,
                BigDecimal.ZERO.setScale(2),
                total,
                insurance.validated() ? "INSURANCE_VALIDATED" : "PENDING",
                insurance.validated()
                        ? "Insurance validated and discount applied"
                        : "Insurance validation pending; undiscounted medicine price");
    }

    private BillResponse retryFallback(
            BillRequest request,
            BigDecimal vatRate,
            Throwable throwable) {
        return fallbackResponse(request, vatRate, "Invoice provider unavailable after retries");
    }

    private BillResponse rateLimitFallback(
            BillRequest request,
            BigDecimal vatRate,
            Throwable throwable) {
        return fallbackResponse(request, vatRate, "Invoice rate limit exceeded");
    }

    private BillResponse fallbackResponse(
            BillRequest request,
            BigDecimal vatRate,
            String message) {
        BigDecimal medicineTotal = request.medicineTotal();
        BigDecimal vatAmount = medicineTotal
                .multiply(vatRate)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = medicineTotal
                .add(vatAmount)
                .setScale(2, RoundingMode.HALF_UP);

        return new BillResponse(
                medicineTotal,
                vatRate,
                vatAmount,
                total,
                "PENDING",
                message);
    }

    public record BillRequest(
            BigDecimal medicineTotal,
            boolean simulateNetworkFailure,
            boolean simulateInsuranceTimeout,
            boolean simulateInsuranceFailure,
            BigDecimal insuranceDiscountRate) {
        public BillRequest {
            if (insuranceDiscountRate == null) {
                insuranceDiscountRate = BigDecimal.ZERO;
            }
        }
    }

    public record BillResponse(
            BigDecimal medicineTotal,
            BigDecimal vatRate,
            BigDecimal vatAmount,
            BigDecimal total,
            String status,
            String message) {
    }
}
