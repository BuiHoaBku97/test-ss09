package startup.vn.pharmacyservice.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pharmacy")
public class PharmacyController {

    @GetMapping("/test")
    public String test() {
        return "Hello, Pharmacy Service!";
    }
}
