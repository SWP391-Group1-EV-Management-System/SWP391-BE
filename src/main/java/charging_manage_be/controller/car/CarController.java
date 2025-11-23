package charging_manage_be.controller.car;

import charging_manage_be.model.dto.car.CarRequestDTO;
import charging_manage_be.model.dto.car.CarResponseDTO;
import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.booking.WaitingListEntity;
import charging_manage_be.model.entity.cars.CarEntity;
import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.repository.cars.CarRepository;
import charging_manage_be.repository.users.UserRepository;
import charging_manage_be.services.car.CarService;
import charging_manage_be.services.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/car")
public class CarController {

    @Autowired
    private CarService carService;
    @Autowired
    private CarRepository carRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    @PostMapping("/add")
    public ResponseEntity<String> addCar(@RequestBody CarRequestDTO carRequestDTO) {
        if (carRequestDTO == null) {
            return ResponseEntity.badRequest().body("Invalid car data");
        }
        carService.insertCar(carRequestDTO);
        return ResponseEntity.ok().body("success");
    }

    @PutMapping("/update/{carId}")
    public ResponseEntity<String> updateCar(@PathVariable String carId, @RequestBody CarRequestDTO carRequestDTO) {
        CarEntity carEntity = carService.getCarByCarID(carId);
        if (carEntity == null) {
            return ResponseEntity.badRequest().body("Car not found");
        }
        UserEntity userEntity = userRepository.findById(carEntity.getUser().getUserID()).orElse(null);
        if (userEntity == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        carService.updateCar(carId, carRequestDTO);
        return ResponseEntity.ok().body("success");
    }

    @GetMapping("/all/{userId}")
    public ResponseEntity<List<CarResponseDTO>> getAllCarByUserId(@PathVariable String userId) {
        String userIdAfterCheck = userId;
        if(userId.contains("@"))
        {
            UserEntity user =  userService.findByEmail(userId).orElse(null);
            if(user != null) {
                userIdAfterCheck = user.getUserID();
            }
        }
       List<CarResponseDTO> carResponseDTO = carService.findAllCarByUserID(userIdAfterCheck).stream().map(CarEntity -> {
           CarResponseDTO carDTO = new CarResponseDTO();
           carDTO.setCarID(CarEntity.getCarID());
           carDTO.setLicensePlate(CarEntity.getLicensePlate());
           carDTO.setUser(CarEntity.getUser().getUserID());
           carDTO.setTypeCar(CarEntity.getTypeCar());
           carDTO.setChassisNumber(CarEntity.getChassisNumber());
           carDTO.setChargingType(CarEntity.getChargingType().getIdChargingType());
           carDTO.setWaitingList(CarEntity.getWaitingList().stream().map(WaitingListEntity::getWaitingListId).toList());
           carDTO.setBookingList(CarEntity.getBookingList().stream().map(BookingEntity::getBookingId).toList());
           return carDTO;
       }).toList();
         return ResponseEntity.ok(carResponseDTO);
    }

    @GetMapping("/{carId}")
    public ResponseEntity<CarResponseDTO> getCarById(@PathVariable String carId) {
        CarEntity carEntity = carService.getCarByCarID(carId);
        if (carEntity == null) {
            return ResponseEntity.badRequest().body(null);
        }
        CarResponseDTO carDTO = new CarResponseDTO();
        carDTO.setCarID(carEntity.getCarID());
        carDTO.setLicensePlate(carEntity.getLicensePlate());
        carDTO.setUser(carEntity.getUser().getUserID());
        carDTO.setTypeCar(carEntity.getTypeCar());
        carDTO.setChassisNumber(carEntity.getChassisNumber());
        carDTO.setChargingType(carEntity.getChargingType().getIdChargingType());
        carDTO.setWaitingList(carEntity.getWaitingList().stream().map(WaitingListEntity::getWaitingListId).toList());
        carDTO.setBookingList(carEntity.getBookingList().stream().map(BookingEntity::getBookingId).toList());
        return ResponseEntity.ok(carDTO);
    }

    @DeleteMapping("/delete/{carId}")
    public ResponseEntity<String> deleteCar(@PathVariable String carId) {
        CarEntity carEntity = carService.getCarByCarID(carId);
        if (carEntity == null) {
            return ResponseEntity.badRequest().body("Car not found");
        }
        else{
            carService.deleteCarByCarID(carId);
        }
        return ResponseEntity.ok().body("success");
    }
    @GetMapping("/random_pin")
    public ResponseEntity<?> randomPin(@RequestParam String userId) {
        int currentPin = carService.pinRandom();
        int minuteMax = carService.maxMinutes(currentPin);

        // Lưu PIN hiện tại vào Redis
        carService.storeCurrentPin(userId, currentPin);

        Map<String, Object> response = new HashMap<>();
        response.put("currentPin", currentPin);
        response.put("minuteMax", minuteMax);
        return ResponseEntity.ok(response);
    }
}
