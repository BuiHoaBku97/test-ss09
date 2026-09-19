package startup.vn.pharmacyservice.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import startup.vn.pharmacyservice.services.WarehouseService;

@RestController
@RequestMapping("/api/v1/warehouse")
public class WarehouseStockController {

    private final WarehouseService warehouseService;

    public WarehouseStockController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping("/stock/{medicineCode}")
    public WarehouseService.StockResponse getStock(@PathVariable String medicineCode) {
        return warehouseService.getStock(medicineCode);
    }
}
