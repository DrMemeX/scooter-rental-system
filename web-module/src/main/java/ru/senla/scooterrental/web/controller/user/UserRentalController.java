package ru.senla.scooterrental.web.controller.user;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.senla.scooterrental.rental.entity.Rental;
import ru.senla.scooterrental.rental.service.RentalService;
import ru.senla.scooterrental.user.service.UserService;
import ru.senla.scooterrental.user.entity.User;
import ru.senla.scooterrental.web.dto.request.rental.FinishRentalRequest;
import ru.senla.scooterrental.web.dto.request.rental.StartRentalRequest;
import ru.senla.scooterrental.web.dto.response.rental.RentalResponse;
import ru.senla.scooterrental.web.mapper.RentalWebMapper;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rentals")
public class UserRentalController {

    private final RentalService rentalService;
    private final UserService userService;

    public UserRentalController(RentalService rentalService,
                                UserService userService) {
        this.rentalService = rentalService;
        this.userService = userService;
    }

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.CREATED)
    public RentalResponse startRental(
            @Valid @RequestBody StartRentalRequest request,
            Authentication authentication
    ) {
        User currentUser = userService.getByEmail(authentication.getName());

        Rental rental = rentalService.startRental(
                currentUser.getId(),
                request.scooterId(),
                request.tariffType(),
                request.plannedHours()
        );

        return RentalWebMapper.toResponse(rental);
    }

    @PostMapping("/{rentalId}/finish")
    public RentalResponse finishRental(
            @PathVariable("rentalId") Long rentalId,
            @Valid @RequestBody FinishRentalRequest request,
            Authentication authentication
    ) {
        Rental rental = rentalService.getRentalOrThrow(rentalId);

        User currentUser = userService.getByEmail(authentication.getName());

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin
                && !rental.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Недостаточно прав");
        }

        Rental finishedRental = rentalService.finishRental(
                rentalId,
                request.rentalPointId(),
                request.distanceKm(),
                request.promoCode()
        );

        return RentalWebMapper.toResponse(finishedRental);
    }

    @PostMapping("/{rentalId}/manual-finish")
    public RentalResponse requestManualFinish(
            @PathVariable("rentalId") Long rentalId,
            Authentication authentication
    ) {
        Rental rental = rentalService.getRentalOrThrow(rentalId);

        User currentUser = userService.getByEmail(authentication.getName());

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin
                && !rental.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Недостаточно прав");
        }

        Rental updatedRental =
                rentalService.requestManualFinish(rentalId);

        return RentalWebMapper.toResponse(updatedRental);
    }

    @GetMapping("/my")
    public List<RentalResponse> getMyRentals(Authentication authentication) {
        User currentUser = userService.getByEmail(authentication.getName());

        return rentalService.getRentalsByUserId(currentUser.getId())
                .stream()
                .map(RentalWebMapper::toResponse)
                .toList();
    }
}