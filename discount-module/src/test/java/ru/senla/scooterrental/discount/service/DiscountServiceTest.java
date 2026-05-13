package ru.senla.scooterrental.discount.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.senla.scooterrental.discount.entity.PromoCode;
import ru.senla.scooterrental.discount.exceptions.DiscountValidationException;
import ru.senla.scooterrental.discount.repository.PromoCodeRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscountServiceTest {

    @Mock
    private PromoCodeRepository promoCodeRepository;

    @InjectMocks
    private DiscountService discountService;

    private PromoCode promoCode;

    @BeforeEach
    void setUp() {
        promoCode = new PromoCode(
                "SALE10",
                BigDecimal.TEN
        );
    }

    @Test
    void createPromoCode_shouldSavePromoCode_whenCodeIsUnique() {
        when(promoCodeRepository.existsByCode("SALE10"))
                .thenReturn(false);

        when(promoCodeRepository.save(promoCode))
                .thenReturn(promoCode);

        PromoCode result = discountService.createPromoCode(promoCode);

        assertNotNull(result);
        assertEquals("SALE10", result.getCode());

        verify(promoCodeRepository).save(promoCode);
    }

    @Test
    void createPromoCode_shouldThrowException_whenCodeAlreadyExists() {
        when(promoCodeRepository.existsByCode("SALE10"))
                .thenReturn(true);

        DiscountValidationException exception =
                assertThrows(
                        DiscountValidationException.class,
                        () -> discountService.createPromoCode(promoCode)
                );

        assertEquals(
                "Промокод уже существует",
                exception.getMessage()
        );

        verify(promoCodeRepository, never()).save(any());
    }

    @Test
    void getByCodeOrThrow_shouldReturnPromoCode_whenPromoExists() {
        when(promoCodeRepository.findByCode("SALE10"))
                .thenReturn(Optional.of(promoCode));

        PromoCode result = discountService.getByCodeOrThrow("sale10");

        assertNotNull(result);
        assertEquals("SALE10", result.getCode());
    }

    @Test
    void getByCodeOrThrow_shouldThrowException_whenPromoNotFound() {
        when(promoCodeRepository.findByCode("INVALID"))
                .thenReturn(Optional.empty());

        DiscountValidationException exception =
                assertThrows(
                        DiscountValidationException.class,
                        () -> discountService.getByCodeOrThrow("invalid")
                );

        assertEquals(
                "Промокод не найден",
                exception.getMessage()
        );
    }

    @Test
    void getAllPromoCodes_shouldReturnAllPromoCodes() {
        when(promoCodeRepository.findAll())
                .thenReturn(List.of(promoCode));

        List<PromoCode> result = discountService.getAllPromoCodes();

        assertEquals(1, result.size());
    }

    @Test
    void getPromoCodeById_shouldReturnPromoCode_whenIdExists() {
        when(promoCodeRepository.findById(1L))
                .thenReturn(Optional.of(promoCode));

        PromoCode result = discountService.getPromoCodeById(1L);

        assertNotNull(result);
        assertEquals("SALE10", result.getCode());
    }

    @Test
    void getPromoCodeById_shouldThrowException_whenIdInvalid() {
        DiscountValidationException exception =
                assertThrows(
                        DiscountValidationException.class,
                        () -> discountService.getPromoCodeById(0L)
                );

        assertEquals(
                "ID промокода должен быть положительным",
                exception.getMessage()
        );
    }

    @Test
    void applyDiscount_shouldReturnDiscountedPrice_whenPromoCodeIsValid() {
        when(promoCodeRepository.findByCode("SALE10"))
                .thenReturn(Optional.of(promoCode));

        BigDecimal result = discountService.applyDiscount(
                BigDecimal.valueOf(100),
                "sale10"
        );

        assertEquals(
                BigDecimal.valueOf(90.00).setScale(2),
                result.setScale(2)
        );
    }

    @Test
    void applyDiscount_shouldReturnSamePrice_whenPromoCodeIsNull() {
        BigDecimal result = discountService.applyDiscount(
                BigDecimal.valueOf(100),
                null
        );

        assertEquals(
                BigDecimal.valueOf(100),
                result
        );
    }

    @Test
    void applyDiscount_shouldThrowException_whenPromoCodeInactive() {
        promoCode.deactivate();

        when(promoCodeRepository.findByCode("SALE10"))
                .thenReturn(Optional.of(promoCode));

        DiscountValidationException exception =
                assertThrows(
                        DiscountValidationException.class,
                        () -> discountService.applyDiscount(
                                BigDecimal.valueOf(100),
                                "SALE10"
                        )
                );

        assertEquals(
                "Промокод не активен",
                exception.getMessage()
        );
    }

    @Test
    void deactivate_shouldDeactivatePromoCode() {
        when(promoCodeRepository.findByCode("SALE10"))
                .thenReturn(Optional.of(promoCode));

        discountService.deactivate("SALE10");

        assertFalse(promoCode.isActive());

        verify(promoCodeRepository).save(promoCode);
    }

    @Test
    void activate_shouldActivatePromoCode() {
        promoCode.deactivate();

        when(promoCodeRepository.findByCode("SALE10"))
                .thenReturn(Optional.of(promoCode));

        discountService.activate("SALE10");

        assertTrue(promoCode.isActive());

        verify(promoCodeRepository).save(promoCode);
    }
}