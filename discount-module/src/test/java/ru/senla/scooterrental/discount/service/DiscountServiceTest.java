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
    void createPromoCode_shouldThrowException_whenPromoCodeIsNull() {
        DiscountValidationException exception =
                assertThrows(
                        DiscountValidationException.class,
                        () -> discountService.createPromoCode(null)
                );

        assertEquals(
                "Промокод не задан",
                exception.getMessage()
        );

        verify(promoCodeRepository, never()).save(any());
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
    void getByCodeOrThrow_shouldThrowException_whenCodeIsNull() {
        DiscountValidationException exception =
                assertThrows(
                        DiscountValidationException.class,
                        () -> discountService.getByCodeOrThrow(null)
                );

        assertEquals(
                "Код промокода не задан",
                exception.getMessage()
        );
    }

    @Test
    void getByCodeOrThrow_shouldThrowException_whenCodeIsBlank() {
        DiscountValidationException exception =
                assertThrows(
                        DiscountValidationException.class,
                        () -> discountService.getByCodeOrThrow("   ")
                );

        assertEquals(
                "Код промокода не может быть пустым",
                exception.getMessage()
        );
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
        assertEquals("SALE10", result.get(0).getCode());
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
    void getPromoCodeById_shouldThrowException_whenIdIsNull() {
        DiscountValidationException exception =
                assertThrows(
                        DiscountValidationException.class,
                        () -> discountService.getPromoCodeById(null)
                );

        assertEquals(
                "ID промокода должен быть положительным",
                exception.getMessage()
        );
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
    void getPromoCodeById_shouldThrowException_whenPromoNotFound() {
        when(promoCodeRepository.findById(999L))
                .thenReturn(Optional.empty());

        DiscountValidationException exception =
                assertThrows(
                        DiscountValidationException.class,
                        () -> discountService.getPromoCodeById(999L)
                );

        assertEquals(
                "Промокод с ID 999 не найден",
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

        verify(promoCodeRepository, never()).findByCode(any());
    }

    @Test
    void applyDiscount_shouldThrowException_whenPriceIsNull() {
        DiscountValidationException exception =
                assertThrows(
                        DiscountValidationException.class,
                        () -> discountService.applyDiscount(
                                null,
                                "SALE10"
                        )
                );

        assertEquals(
                "Цена не задан",
                exception.getMessage()
        );
    }

    @Test
    void applyDiscount_shouldThrowException_whenPriceIsNegative() {
        DiscountValidationException exception =
                assertThrows(
                        DiscountValidationException.class,
                        () -> discountService.applyDiscount(
                                BigDecimal.valueOf(-100),
                                "SALE10"
                        )
                );

        assertEquals(
                "Цена не может быть отрицательным",
                exception.getMessage()
        );
    }

    @Test
    void applyDiscount_shouldThrowException_whenPromoCodeIsBlank() {
        DiscountValidationException exception =
                assertThrows(
                        DiscountValidationException.class,
                        () -> discountService.applyDiscount(
                                BigDecimal.valueOf(100),
                                "   "
                        )
                );

        assertEquals(
                "Код промокода не может быть пустым",
                exception.getMessage()
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
    void deactivate_shouldThrowException_whenPromoNotFound() {
        when(promoCodeRepository.findByCode("UNKNOWN"))
                .thenReturn(Optional.empty());

        DiscountValidationException exception =
                assertThrows(
                        DiscountValidationException.class,
                        () -> discountService.deactivate("UNKNOWN")
                );

        assertEquals(
                "Промокод не найден",
                exception.getMessage()
        );

        verify(promoCodeRepository, never()).save(any());
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

    @Test
    void activate_shouldThrowException_whenPromoNotFound() {
        when(promoCodeRepository.findByCode("UNKNOWN"))
                .thenReturn(Optional.empty());

        DiscountValidationException exception =
                assertThrows(
                        DiscountValidationException.class,
                        () -> discountService.activate("UNKNOWN")
                );

        assertEquals(
                "Промокод не найден",
                exception.getMessage()
        );

        verify(promoCodeRepository, never()).save(any());
    }

    @Test
    void promoCodeConstructor_shouldNormalizeCodeToUpperCase() {
        PromoCode result = new PromoCode(
                " sale10 ",
                BigDecimal.TEN
        );

        assertEquals("SALE10", result.getCode());
    }

    @Test
    void promoCodeConstructor_shouldThrowException_whenCodeIsNull() {
        DiscountValidationException exception =
                assertThrows(
                        DiscountValidationException.class,
                        () -> new PromoCode(
                                null,
                                BigDecimal.TEN
                        )
                );

        assertEquals(
                "Код промокода не может быть пустым",
                exception.getMessage()
        );
    }

    @Test
    void promoCodeConstructor_shouldThrowException_whenCodeIsBlank() {
        DiscountValidationException exception =
                assertThrows(
                        DiscountValidationException.class,
                        () -> new PromoCode(
                                "   ",
                                BigDecimal.TEN
                        )
                );

        assertEquals(
                "Код промокода не может быть пустым",
                exception.getMessage()
        );
    }

    @Test
    void promoCodeConstructor_shouldThrowException_whenPercentIsNull() {
        DiscountValidationException exception =
                assertThrows(
                        DiscountValidationException.class,
                        () -> new PromoCode(
                                "SALE10",
                                null
                        )
                );

        assertEquals(
                "Процент скидки не задан",
                exception.getMessage()
        );
    }

    @Test
    void promoCodeConstructor_shouldThrowException_whenPercentIsZero() {
        DiscountValidationException exception =
                assertThrows(
                        DiscountValidationException.class,
                        () -> new PromoCode(
                                "SALE10",
                                BigDecimal.ZERO
                        )
                );

        assertEquals(
                "Процент скидки должен быть положительным",
                exception.getMessage()
        );
    }

    @Test
    void promoCodeConstructor_shouldThrowException_whenPercentIsNegative() {
        DiscountValidationException exception =
                assertThrows(
                        DiscountValidationException.class,
                        () -> new PromoCode(
                                "SALE10",
                                BigDecimal.valueOf(-5)
                        )
                );

        assertEquals(
                "Процент скидки должен быть положительным",
                exception.getMessage()
        );
    }

    @Test
    void promoCodeConstructor_shouldThrowException_whenPercentGreaterThan15() {
        DiscountValidationException exception =
                assertThrows(
                        DiscountValidationException.class,
                        () -> new PromoCode(
                                "SUPER30",
                                BigDecimal.valueOf(30)
                        )
                );

        assertEquals(
                "Процент скидки не может быть больше 15",
                exception.getMessage()
        );
    }
}