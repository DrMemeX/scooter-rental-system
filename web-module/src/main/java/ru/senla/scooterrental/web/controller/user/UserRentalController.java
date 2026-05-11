package ru.senla.scooterrental.web.controller.user;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.senla.scooterrental.rental.entity.Rental;
import ru.senla.scooterrental.rental.service.RentalService;
import ru.senla.scooterrental.web.dto.request.rental.FinishRentalRequest;
import ru.senla.scooterrental.web.dto.request.rental.StartRentalRequest;
import ru.senla.scooterrental.web.dto.response.rental.RentalResponse;
import ru.senla.scooterrental.web.mapper.RentalWebMapper;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rentals")
public class UserRentalController {

    private final RentalService rentalService;

    public UserRentalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.CREATED)
    public RentalResponse startRental(
            @Valid @RequestBody StartRentalRequest request
    ) {
        Rental rental = rentalService.startRental(
                request.userId(),
                request.scooterId(),
                request.tariffType(),
                request.plannedHours()
        );

        return RentalWebMapper.toResponse(rental);
    }

    @PostMapping("/{rentalId}/finish")
    public RentalResponse finishRental(
            @PathVariable Long rentalId,
            @Valid @RequestBody FinishRentalRequest request
    ) {
        Rental rental = rentalService.finishRental(
                rentalId,
                request.rentalPointId(),
                request.distanceKm(),
                request.promoCode()
        );

        return RentalWebMapper.toResponse(rental);
    }

    @PostMapping("/{rentalId}/manual-finish")
    public RentalResponse requestManualFinish(
            @PathVariable Long rentalId
    ) {
        Rental rental = rentalService.requestManualFinish(rentalId);

        return RentalWebMapper.toResponse(rental);
    }

    @GetMapping("/my")
    public List<RentalResponse> getMyRentals(
            @RequestParam Long userId
    ) {
        return rentalService.getRentalsByUserId(userId)
                .stream()
                .map(RentalWebMapper::toResponse)
                .toList();
    }
}