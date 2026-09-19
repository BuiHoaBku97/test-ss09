package startup.vn.warehouseservice.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/stock")
public class WarehouseController {

    @GetMapping("/{medicineCode}")
    public StockResponse getStock(@PathVariable String medicineCode) {
        if ("FAIL".equalsIgnoreCase(medicineCode)) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Fake warehouse failure");
        }

        int quantity = switch (medicineCode.toUpperCase()) {
            case "PARA500" -> 120;
            case "AMOX500" -> 45;
            default -> 10;
        };

        return new StockResponse(medicineCode, quantity, quantity > 0);
    }

    public record StockResponse(String medicineCode, int quantity, boolean available) {
    }
}
