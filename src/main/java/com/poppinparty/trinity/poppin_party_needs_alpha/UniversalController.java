package com.poppinparty.trinity.poppin_party_needs_alpha;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UniversalController {
    @GetMapping("/dummy")
    public String dummyPage() {
        return "dummy";
    }

    // here some funny parts to do when in free time idk
}
