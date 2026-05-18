package ru.senla.scooterrental.web.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.senla.scooterrental.discount.entity.PromoCode;
import ru.senla.scooterrental.discount.enums.PromoCodeStatus;
import ru.senla.scooterrental.discount.exceptions.DiscountValidationException;
import ru.senla.scooterrental.discount.service.DiscountService;
import ru.senla.scooterrental.web.dto.request.discount.CreatePromoCodeRequest;
import ru.senla.scooterrental.web.error.GlobalExceptionHandler;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

        when(discountService.createPromoCode(any(PromoCode.class)))
                .thenReturn(promoCode);

        mockMvc.perform(
                        post("/api/v1/admin/promo-codes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.code").value("SALE10"))
                .andExpect(jsonPath("$.percent").value(10))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(discountService).createPromoCode(any(PromoCode.class));
    }

    @Test
    void createPromoCode_shouldReturnBadRequest_whenRequestIsInvalid()
            throws Exception {

        CreatePromoCodeRequest request =
                new CreatePromoCodeRequest("", BigDecimal.ZERO);

        mockMvc.perform(
                        post("/api/v1/admin/promo-codes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(discountService);
    }

    @Test
    void createPromoCode_shouldReturnBadRequest_whenBodyIsMissing()
            throws Exception {

        mockMvc.perform(
                        post("/api/v1/admin/promo-codes")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(discountService);
    }

    @Test
    void createPromoCode_shouldReturnBadRequest_whenPercentIsGreaterThan15()
            throws Exception {

        CreatePromoCodeRequest request =
                new CreatePromoCodeRequest("SALE30", BigDecimal.valueOf(30));

        mockMvc.perform(
                        post("/api/v1/admin/promo-codes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(discountService);
    }

    @Test
    void createPromoCode_shouldReturnBadRequest_whenServiceThrowsValidation()
            throws Exception {

        CreatePromoCodeRequest request =
                new CreatePromoCodeRequest("SALE10", BigDecimal.valueOf(10));

        when(discountService.createPromoCode(any(PromoCode.class)))
                .thenThrow(new DiscountValidationException(
                        "Промокод не задан"
                ));

        mockMvc.perform(
                        post("/api/v1/admin/promo-codes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(discountService).createPromoCode(any(PromoCode.class));
    }

    @Test
    void createPromoCode_shouldReturnConflict_whenPromoCodeAlreadyExists()
            throws Exception {

        CreatePromoCodeRequest request =
                new CreatePromoCodeRequest("SALE10", BigDecimal.valueOf(10));

        when(discountService.createPromoCode(any(PromoCode.class)))
                .thenThrow(new DiscountValidationException(
                        "Промокод уже существует"
                ));

        mockMvc.perform(
                        post("/api/v1/admin/promo-codes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(discountService).createPromoCode(any(PromoCode.class));
    }

    @Test
    void getAllPromoCodes_shouldReturnSuccessfully() throws Exception {
        PromoCode promoCode = promoCode();

        when(discountService.getAllPromoCodes())
                .thenReturn(List.of(promoCode));

        mockMvc.perform(get("/api/v1/admin/promo-codes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].code").value("SALE10"))
                .andExpect(jsonPath("$[0].percent").value(10))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(discountService).getAllPromoCodes();
    }

    @Test
    void getAllPromoCodes_shouldReturnEmptyListSuccessfully()
            throws Exception {

        when(discountService.getAllPromoCodes())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/admin/promo-codes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(discountService).getAllPromoCodes();
    }

    @Test
    void getPromoCodeById_shouldReturnSuccessfully() throws Exception {
        PromoCode promoCode = promoCode();

        when(discountService.getPromoCodeById(1L))
                .thenReturn(promoCode);

        mockMvc.perform(get("/api/v1/admin/promo-codes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.code").value("SALE10"))
                .andExpect(jsonPath("$.percent").value(10))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(discountService).getPromoCodeById(1L);
    }

    @Test
    void getPromoCodeById_shouldReturnBadRequest_whenIdIsInvalid()
            throws Exception {

        when(discountService.getPromoCodeById(0L))
                .thenThrow(new DiscountValidationException(
                        "ID промокода должен быть положительным"
                ));

        mockMvc.perform(get("/api/v1/admin/promo-codes/0"))
                .andExpect(status().isBadRequest());

        verify(discountService).getPromoCodeById(0L);
    }

    @Test
    void getPromoCodeById_shouldReturnBadRequest_whenPromoCodeNotFound()
            throws Exception {

        when(discountService.getPromoCodeById(99L))
                .thenThrow(new DiscountValidationException(
                        "Промокод с ID 99 не найден"
                ));

        mockMvc.perform(get("/api/v1/admin/promo-codes/99"))
                .andExpect(status().isBadRequest());

        verify(discountService).getPromoCodeById(99L);
    }

    @Test
    void getPromoCodeById_shouldReturnBadRequest_whenIdTypeIsInvalid()
            throws Exception {

        mockMvc.perform(get("/api/v1/admin/promo-codes/abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(discountService);
    }

    @Test
    void activatePromoCode_shouldActivateSuccessfully() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/promo-codes/SALE10/activate"))
                .andExpect(status().isOk());

        verify(discountService).activate("SALE10");
    }

    @Test
    void activatePromoCode_shouldReturnBadRequest_whenCodeIsInvalid()
            throws Exception {

        doThrow(new DiscountValidationException(
                "Промокод не найден"
        )).when(discountService).activate("UNKNOWN");

        mockMvc.perform(patch("/api/v1/admin/promo-codes/UNKNOWN/activate"))
                .andExpect(status().isBadRequest());

        verify(discountService).activate("UNKNOWN");
    }

    @Test
    void deactivatePromoCode_shouldDeactivateSuccessfully() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/promo-codes/SALE10/deactivate"))
                .andExpect(status().isOk());

        verify(discountService).deactivate("SALE10");
    }

    @Test
    void deactivatePromoCode_shouldReturnBadRequest_whenCodeIsInvalid()
            throws Exception {

        doThrow(new DiscountValidationException(
                "Промокод не найден"
        )).when(discountService).deactivate("UNKNOWN");

        mockMvc.perform(patch("/api/v1/admin/promo-codes/UNKNOWN/deactivate"))
                .andExpect(status().isBadRequest());

        verify(discountService).deactivate("UNKNOWN");
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