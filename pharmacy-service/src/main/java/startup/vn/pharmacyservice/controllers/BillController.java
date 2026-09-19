package startup.vn.pharmacyservice.controllers;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import startup.vn.pharmacyservice.services.BillService;
import startup.vn.pharmacyservice.services.BillService.BillRequest;
import startup.vn.pharmacyservice.services.BillService.BillResponse;

@RefreshScope
@RestController
@RequestMapping("/api/v1/bill")
public class BillController {

    @Value("${pharmacy.vat-rate}")
    private BigDecimal vatRate;

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    @PostMapping
    public BillResponse calculate(@RequestBody BillRequest request) {
        return billService.createBill(request, vatRate);
    }
}
