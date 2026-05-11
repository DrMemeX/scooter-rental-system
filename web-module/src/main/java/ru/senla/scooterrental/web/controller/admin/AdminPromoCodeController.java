package ru.senla.scooterrental.web.controller.admin;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.senla.scooterrental.discount.entity.PromoCode;
import ru.senla.scooterrental.discount.service.DiscountService;
import ru.senla.scooterrental.web.dto.request.discount.CreatePromoCodeRequest;
import ru.senla.scooterrental.web.dto.response.discount.PromoCodeResponse;
import ru.senla.scooterrental.web.mapper.DiscountWebMapper;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/promo-codes")
public class AdminPromoCodeController {

    private final DiscountService discountService;

    public AdminPromoCodeController(DiscountService discountService) {
        this.discountService = discountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PromoCodeResponse createPromoCode(
            @Valid @RequestBody CreatePromoCodeRequest request
    ) {
        PromoCode promoCode = new PromoCode(
                request.code(),
                request.percent()
        );

        PromoCode savedPromoCode = discountService.createPromoCode(promoCode);

        return DiscountWebMapper.toResponse(savedPromoCode);
    }

    @GetMapping
    public List<PromoCodeResponse> getAllPromoCodes() {
        return discountService.getAllPromoCodes()
                .stream()
                .map(DiscountWebMapper::toResponse)
                .toList();
    }

    @GetMapping("/{promoCodeId}")
    public PromoCodeResponse getPromoCodeById(
            @PathVariable Long promoCodeId
    ) {
        PromoCode promoCode = discountService.getPromoCodeById(promoCodeId);

        return DiscountWebMapper.toResponse(promoCode);
    }

    @PatchMapping("/{code}/activate")
    public void activatePromoCode(
            @PathVariable String code
    ) {
        discountService.activate(code);
    }

    @PatchMapping("/{code}/deactivate")
    public void deactivatePromoCode(
            @PathVariable String code
    ) {
        discountService.deactivate(code);
    }
}