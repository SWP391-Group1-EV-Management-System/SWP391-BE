package charging_manage_be.controller.car;

import charging_manage_be.model.dto.car.CarResponseDTO;
import charging_manage_be.model.entity.cars.CarEntity;
import charging_manage_be.services.car.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/car")
public class CarController {

    @Autowired
    private CarService carService;




    @GetMapping ("/user/{userID}")
    public ResponseEntity<List<CarResponseDTO>> getCarByUserID(@PathVariable String userID) {
        List<CarEntity> cars = carService.findAllCarByUserID(userID);
        List<CarResponseDTO> carResponses = new ArrayList<>();
        for (CarEntity car : cars) {
            List<String> listWaiting = car.getWaitingList().stream()
                    .map(waiting -> waiting.getWaitingListId())
                    .toList();
            List<String> listBooking = car.getBookingList().stream()
                    .map(booking -> booking.getBookingId())
                    .toList();
            CarResponseDTO carResponse = new CarResponseDTO(car.getCarID(), car.getLicensePlate(), car.getUser().getUserID(), car.getTypeCar(), car.getChassisNumber(), car.getChargingType().getIdChargingType(),listWaiting, listBooking);
            carResponses.add(carResponse);
        }

        return ResponseEntity.ok(carResponses);
    }



}
