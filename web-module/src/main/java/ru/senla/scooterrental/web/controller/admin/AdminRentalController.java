package ru.senla.scooterrental.web.controller.admin;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.senla.scooterrental.rental.entity.Rental;
import ru.senla.scooterrental.rental.service.RentalService;
import ru.senla.scooterrental.web.dto.request.rental.ApproveManualFinishRequest;
import ru.senla.scooterrental.web.dto.response.rental.RentalResponse;
import ru.senla.scooterrental.web.mapper.RentalWebMapper;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/rentals")
public class AdminRentalController {

    private final RentalService rentalService;

    public AdminRentalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @GetMapping
    public List<RentalResponse> getAllRentals() {
        return rentalService.getAllRentals()
                .stream()
                .map(RentalWebMapper::toResponse)
                .toList();
    }

    @GetMapping("/{rentalId}")
    public RentalResponse getRentalById(
            @PathVariable("rentalId") Long rentalId
    ) {
        Rental rental = rentalService.getRentalOrThrow(rentalId);

        return RentalWebMapper.toResponse(rental);
    }

    @GetMapping("/users/{userId}")
    public List<RentalResponse> getRentalsByUserId(
            @PathVariable("userId") Long userId
    ) {
        return rentalService.getRentalsByUserId(userId)
                .stream()
                .map(RentalWebMapper::toResponse)
                .toList();
    }

    @GetMapping("/scooters/{scooterId}")
    public List<RentalResponse> getRentalsByScooterId(
            @PathVariable("scooterId") Long scooterId
    ) {
        return rentalService.getRentalsByScooterId(scooterId)
                .stream()
                .map(RentalWebMapper::toResponse)
                .toList();
    }

    @PostMapping("/{rentalId}/approve-manual-finish")
    public RentalResponse approveManualFinish(
            @PathVariable("rentalId") Long rentalId,
            @Valid @RequestBody ApproveManualFinishRequest request
    ) {
        Rental rental = rentalService.approveManualFinish(
                rentalId,
                request.rentalPointId(),
                request.distanceKm(),
                request.promoCode()
        );

        return RentalWebMapper.toResponse(rental);
    }
}