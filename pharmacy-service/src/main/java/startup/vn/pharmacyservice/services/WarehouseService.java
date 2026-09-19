package startup.vn.pharmacyservice.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class WarehouseService {

    private final RestClient restClient;
    private final String warehouseServiceUrl;

    public WarehouseService(
            @Qualifier("loadBalancedRestClientBuilder") RestClient.Builder restClientBuilder,
            @Value("${warehouse.service-url:http://warehouse-service}") String warehouseServiceUrl) {
        this.restClient = restClientBuilder.build();
        this.warehouseServiceUrl = warehouseServiceUrl;
    }

    @CircuitBreaker(name = "warehouseCB", fallbackMethod = "stockFallback")
    public StockResponse getStock(String medicineCode) {
        return restClient.get()
                .uri(warehouseServiceUrl + "/api/v1/stock/{medicineCode}", medicineCode)
                .retrieve()
                .body(StockResponse.class);
    }

    private StockResponse stockFallback(String medicineCode, Throwable throwable) {
        return new StockResponse(medicineCode, 0, false, "Warehouse service unavailable");
    }

    public record StockResponse(
            String medicineCode,
            int quantity,
            boolean available,
            String message) {
    }
}
