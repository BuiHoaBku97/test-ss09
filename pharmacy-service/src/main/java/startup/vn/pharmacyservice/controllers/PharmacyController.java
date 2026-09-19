package startup.vn.pharmacyservice.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pharmacy")
public class PharmacyController {

    @Value("${app.brand-name}")
    private String brandName;

    @Value("${app.hotline}")
    private String hotline;

    @GetMapping("/test")
    public String test() {
        return "Hello, Pharmacy Service!";
    }

    @GetMapping("/info")
    public String getInfo() {
        return "System info - Brand Name: " + brandName + ", Hotline: " + hotline;
    }
}
