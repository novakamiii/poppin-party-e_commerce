package com.poppinparty.trinity.poppin_party_needs_alpha;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.ui.Model;

@Controller
public class ProductController {

    //TODO: this shit aint even that optimized, i vibe coded

    @Autowired
    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/index")
    public String mainPage() {
        return "index";
    }

    //TODO:Placeholder, make a backend for account login first
    @GetMapping("/products")
    public String showProducts(Model model) {
    model.addAttribute("products", productRepository.findAll());
    return "product_list"; // This loads product_list.html from /templates
    }


    //todo: Add a new product
    @GetMapping("/products/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        return "add_product";
    }

    //done: Handle the form submission for adding a new product
    @PostMapping("/products/add")
    public String addProduct(@ModelAttribute Product product) {
        productRepository.save(product);
        return "redirect:/products";
    }

    // edit product
    @GetMapping("/products/edit/{id}")
    public String editProduct(@PathVariable("id") Long id, Model model) {
        Product product = productRepository.findById(id)
                                           .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        model.addAttribute("product", product);
        return "edit_product";
    }

    //what the fuck is this line of code
    @PostMapping("/products/update/{id}")
    public String updateProduct(@PathVariable("id") Long id, Product product) {
        product.setId(id);
        productRepository.save(product);
        return "redirect:/products";
    }

    // delete a product
    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productRepository.deleteById(id);
        return "redirect:/products";
    }
}
