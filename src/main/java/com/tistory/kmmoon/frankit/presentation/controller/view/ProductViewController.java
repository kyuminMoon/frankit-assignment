package com.tistory.kmmoon.frankit.presentation.controller.view;

import com.tistory.kmmoon.frankit.application.service.ProductService;
import com.tistory.kmmoon.frankit.presentation.dto.ProductDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/view/products")
public class ProductViewController {

    private final ProductService productService;

    @GetMapping
    public String getProductList(Model model, 
                               @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        log.info("상품 목록 페이지 요청");
        Page<ProductDto.ListResponse> products = productService.getProducts(pageable);
        model.addAttribute("products", products);
        return "product/list";
    }

    @GetMapping("/{productId}")
    public String getProductDetail(@PathVariable Long productId, Model model) {
        log.info("상품 상세 페이지 요청, id: {}", productId);
        ProductDto.Response product = productService.getProductById(productId);
        model.addAttribute("product", product);
        return "product/detail";
    }

    @GetMapping("/new")
    public String getProductForm(Model model) {
        log.info("상품 등록 페이지 요청");
        model.addAttribute("product", new ProductDto.Request());
        return "product/form";
    }

    @GetMapping("/{productId}/edit")
    public String getProductEditForm(@PathVariable Long productId, Model model) {
        log.info("상품 수정 페이지 요청, id: {}", productId);
        ProductDto.Response product = productService.getProductById(productId);
        model.addAttribute("product", product);
        return "product/edit";
    }
}