package ru.senla.scooterrental.web.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.senla.scooterrental.discount.entity.PromoCode;
import ru.senla.scooterrental.discount.enums.PromoCodeStatus;
import ru.senla.scooterrental.discount.service.DiscountService;
import ru.senla.scooterrental.web.dto.request.discount.CreatePromoCodeRequest;
import ru.senla.scooterrental.web.error.GlobalExceptionHandler;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminPromoCodeControllerTest {

    private MockMvc mockMvc;
    private DiscountService discountService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        discountService = mock(DiscountService.class);

        AdminPromoCodeController controller =
                new AdminPromoCodeController(discountService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void createPromoCode_shouldCreateSuccessfully() throws Exception {
        PromoCode promoCode = promoCode();

        CreatePromoCodeRequest request =
                new CreatePromoCodeRequest("SALE10", BigDecimal.valueOf(10));

        when(discountService.createPromoCode(
                org.mockito.ArgumentMatchers.any(PromoCode.class)
        )).thenReturn(promoCode);

        mockMvc.perform(
                        post("/api/v1/admin/promo-codes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated());

        verify(discountService).createPromoCode(
                org.mockito.ArgumentMatchers.any(PromoCode.class)
        );
    }

    @Test
    void getAllPromoCodes_shouldReturnSuccessfully() throws Exception {
        PromoCode promoCode = promoCode();

        when(discountService.getAllPromoCodes())
                .thenReturn(List.of(promoCode));

        mockMvc.perform(get("/api/v1/admin/promo-codes"))
                .andExpect(status().isOk());

        verify(discountService).getAllPromoCodes();
    }

    @Test
    void getPromoCodeById_shouldReturnSuccessfully() throws Exception {
        PromoCode promoCode = promoCode();

        when(discountService.getPromoCodeById(1L))
                .thenReturn(promoCode);

        mockMvc.perform(get("/api/v1/admin/promo-codes/1"))
                .andExpect(status().isOk());

        verify(discountService).getPromoCodeById(1L);
    }

    @Test
    void activatePromoCode_shouldActivateSuccessfully() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/promo-codes/SALE10/activate"))
                .andExpect(status().isOk());

        verify(discountService).activate("SALE10");
    }

    @Test
    void deactivatePromoCode_shouldDeactivateSuccessfully() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/promo-codes/SALE10/deactivate"))
                .andExpect(status().isOk());

        verify(discountService).deactivate("SALE10");
    }

    private PromoCode promoCode() {
        PromoCode promoCode = mock(PromoCode.class);

        lenient().when(promoCode.getId()).thenReturn(1L);
        lenient().when(promoCode.getCode()).thenReturn("SALE10");
        lenient().when(promoCode.getPercent()).thenReturn(BigDecimal.valueOf(10));
        lenient().when(promoCode.getStatus())
                .thenReturn(PromoCodeStatus.ACTIVE);

        return promoCode;
    }
}