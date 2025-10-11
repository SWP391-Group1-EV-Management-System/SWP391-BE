package charging_manage_be.controller.car;

import charging_manage_be.model.dto.car.CarResponse;
import charging_manage_be.model.entity.cars.CarEntity;
import charging_manage_be.services.car.CarService;
import charging_manage_be.services.users.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/car")
public class CarController {

    @Autowired
    private CarService carService;

    @PostMapping
    public ResponseEntity<CarEntity> createCar(@RequestBody  CarEntity carEntity) {
        CarEntity savedCar = carService.insertCar(carEntity);
        return ResponseEntity.ok(savedCar);
    }
    @PutMapping("/{carID}")
    public ResponseEntity<CarEntity> updateCar(@PathVariable String carID, @RequestBody CarEntity carDetails) {
        try {
            carDetails.setCarID(carID);
            CarEntity updatedCar = carService.updateCar(carDetails);
            return ResponseEntity.ok(updatedCar);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{carID}")
    public ResponseEntity<CarEntity> getCar(@PathVariable String carID) {
        Optional<CarEntity> car = carService.getCarByCarID(carID);
        if (car != null) {
            return ResponseEntity.ok(car.get());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<CarEntity>> getAllCars() {
        List<CarEntity> cars = carService.findAllCar();
        return ResponseEntity.ok(cars);
    }

    @DeleteMapping("/{carID}")
    public ResponseEntity<String> deleteCar(@PathVariable String carID) {
        boolean deleted = carService.deleteCarByCarID(carID);
        if (deleted == true) {
            return ResponseEntity.ok("Car deleted successfully");
        }
        return ResponseEntity.notFound().build();
    }
    @GetMapping ("/user/{userID}")
    public ResponseEntity<List<CarResponse>> getCarByUserID(@PathVariable String userID) {
        List<CarEntity> cars = carService.findAllCarByUserID(userID);
        List<CarResponse> carResponses = new ArrayList<>();
        for (CarEntity car : cars) {
            List<String> listWaiting = car.getWaitingList().stream()
                    .map(waiting -> waiting.getWaitingListId())
                    .toList();
            List<String> listBooking = car.getBookingList().stream()
                    .map(booking -> booking.getBookingId())
                    .toList();
            CarResponse carResponse = new CarResponse(car.getCarID(), car.getLicensePlate(), car.getUser().getUserID(), car.getTypeCar(), car.getChassisNumber(), car.getChargingType().getIdChargingType(),listWaiting, listBooking);
            carResponses.add(carResponse);
        }

        return ResponseEntity.ok(carResponses);
    }



}
