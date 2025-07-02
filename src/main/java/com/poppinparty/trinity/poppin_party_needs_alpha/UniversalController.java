package com.poppinparty.trinity.poppin_party_needs_alpha;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.poppinparty.trinity.poppin_party_needs_alpha.Services.EmailService;

@Controller
public class UniversalController {

    @Autowired
    private EmailService emailService;

    @GetMapping("/dummy")
    public String dummyPage() {
        return "dummy";
    }

    @GetMapping("/test-email")
    @ResponseBody
    public String testEmail() {
        emailService.sendResetCode("email.com", "123456");
        return "Test email sent!";
    }

    // here some funny parts to do when in free time idk
}
