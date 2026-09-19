package startup.vn.pharmacyservice.controllers;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RefreshScope
@RestController
@RequestMapping("/api/v1/bill")
public class BillController {

    @Value("${pharmacy.vat-rate}")
    private BigDecimal vatRate;

    @PostMapping
    public BillResponse calculate(@RequestBody BillRequest request) {
        BigDecimal medicineTotal = request.medicineTotal();
        BigDecimal vatAmount = medicineTotal
                .multiply(vatRate)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = medicineTotal
                .add(vatAmount)
                .setScale(2, RoundingMode.HALF_UP);

        return new BillResponse(medicineTotal, vatRate, vatAmount, total);
    }

    public record BillRequest(BigDecimal medicineTotal) {
    }

    public record BillResponse(
            BigDecimal medicineTotal,
            BigDecimal vatRate,
            BigDecimal vatAmount,
            BigDecimal total) {
    }
}
