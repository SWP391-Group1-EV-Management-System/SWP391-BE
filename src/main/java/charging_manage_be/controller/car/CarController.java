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
    // cố định 1 xe dung lượng từ 1 đến 100 là 92kW
    // trạm sạc cố định là 250KW
    // vậy với trạm 250kW thì từ 0 đến 100 sẽ mất 22p -> 13 giây sẽ sạc đuọc 1% pin
    //13.25 là đang gán cứng cho xe 92kw, 1% xe thì phải sạc 13.25s

    @Autowired
    private CarService carService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    @PostMapping("/addForUser")
    public ResponseEntity<String> addCarForUser(@RequestBody CarRequestDTO carRequestDTO) {
        if (carRequestDTO == null) {
            return ResponseEntity.badRequest().body("Invalid car data");
        }
        boolean check = carService.insertCarByUser(carRequestDTO);
        if (!check) {
            return ResponseEntity.badRequest().body("Car with the same license plate or chassis number already exists");
        }
        return ResponseEntity.ok().body("success");
    }

    @PutMapping("user/update/{carId}")
    public ResponseEntity<String> updateCarForUser(@PathVariable String carId, @RequestBody CarRequestDTO carRequestDTO) {
        CarEntity carEntity = carService.getCarByCarID(carId);
        if (carEntity == null) {
            return ResponseEntity.badRequest().body("Car not found");
        }
        UserEntity userEntity = userRepository.findById(carEntity.getUser().getUserID()).orElse(null);
        if (userEntity == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        boolean check =  carService.updateCarByUser(carId, carRequestDTO);
        if(check == false){
            return ResponseEntity.badRequest().body("Update failed");
        }
        return ResponseEntity.ok().body("success");
    }

//    @PutMapping("admin/update/{carId}")
//    public ResponseEntity<String> updateCarForAdmin(@PathVariable String carId, @RequestBody CarRequestDTO carRequestDTO) {
//        CarEntity carEntity = carService.getCarByCarID(carId);
//        if (carEntity == null) {
//            return ResponseEntity.badRequest().body("Car not found");
//        }
//        UserEntity userEntity = userRepository.findById(carEntity.getUser().getUserID()).orElse(null);
//        if (userEntity == null) {
//            return ResponseEntity.badRequest().body("User not found");
//        }
//        boolean check = carService.updateCarByAdmin(carId, carRequestDTO);
//        if(check == false){
//            return ResponseEntity.badRequest().body("Update failed");
//        }
//        return ResponseEntity.ok().body("success");
//    }

    @GetMapping("/allForUser/{userId}")
    public ResponseEntity<List<CarResponseDTO>> getAllActiveCarByUserId(@PathVariable String userId) {
        String userIdAfterCheck = userId;
        if(userId.contains("@"))
        {
            UserEntity user =  userService.findByEmail(userId).orElse(null);
            if(user != null) {
                userIdAfterCheck = user.getUserID();
            }
        }
       List<CarResponseDTO> carResponseDTO = carService.findAllActiveCarsByUserID(userIdAfterCheck).stream().map(CarEntity -> {
           CarResponseDTO carDTO = new CarResponseDTO();
           carDTO.setCarID(CarEntity.getCarID());
           carDTO.setLicensePlate(CarEntity.getLicensePlate());
           carDTO.setUser(CarEntity.getUser().getUserID());
           carDTO.setTypeCar(CarEntity.getTypeCar());
           carDTO.setChassisNumber(CarEntity.getChassisNumber());
           carDTO.setChargingType(CarEntity.getChargingType().getIdChargingType());
           carDTO.setActive(CarEntity.getIsActive());
           carDTO.setWaitingList(CarEntity.getWaitingList().stream().map(WaitingListEntity::getWaitingListId).toList());
           carDTO.setBookingList(CarEntity.getBookingList().stream().map(BookingEntity::getBookingId).toList());
           return carDTO;
       }).toList();
         return ResponseEntity.ok(carResponseDTO);
    }

    @GetMapping("/allForAdmin")
    public ResponseEntity<List<CarResponseDTO>> getAllCars() {
        List<CarEntity> list = carService.findAllCar();
        List<CarResponseDTO> carResponseDTO = list.stream().map(CarEntity -> {
            CarResponseDTO carDTO = new CarResponseDTO();
            carDTO.setCarID(CarEntity.getCarID());
            carDTO.setLicensePlate(CarEntity.getLicensePlate());
            carDTO.setUser(CarEntity.getUser().getUserID());
            carDTO.setTypeCar(CarEntity.getTypeCar());
            carDTO.setChassisNumber(CarEntity.getChassisNumber());
            carDTO.setChargingType(CarEntity.getChargingType().getIdChargingType());
            carDTO.setActive(CarEntity.getIsActive());
            carDTO.setWaitingList(CarEntity.getWaitingList().stream().map(WaitingListEntity::getWaitingListId).toList());
            carDTO.setBookingList(CarEntity.getBookingList().stream().map(BookingEntity::getBookingId).toList());
            return carDTO;
        }).toList();
        return ResponseEntity.ok(carResponseDTO);
    }

    @GetMapping("/carId/{carId}")
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
        carDTO.setActive(carEntity.getIsActive());
        carDTO.setWaitingList(carEntity.getWaitingList().stream().map(WaitingListEntity::getWaitingListId).toList());
        carDTO.setBookingList(carEntity.getBookingList().stream().map(BookingEntity::getBookingId).toList());
        return ResponseEntity.ok(carDTO);
    }

    @GetMapping("/license_plate/{licensePlate}")
    public ResponseEntity<List<CarResponseDTO>> getCarByLicensePlate(@PathVariable String licensePlate){
        List<CarEntity> carEntity = carService.findByLicensePlate(licensePlate);
        List<CarResponseDTO> carDTOs = new ArrayList<>();
        for (CarEntity car : carEntity) {
            CarResponseDTO carDTO = new CarResponseDTO();
            carDTO.setCarID(car.getCarID());
            carDTO.setLicensePlate(car.getLicensePlate());
            carDTO.setUser(car.getUser().getUserID());
            carDTO.setTypeCar(car.getTypeCar());
            carDTO.setChassisNumber(car.getChassisNumber());
            carDTO.setChargingType(car.getChargingType().getIdChargingType());
            carDTO.setActive(car.getIsActive());
            carDTO.setWaitingList(car.getWaitingList().stream().map(WaitingListEntity::getWaitingListId).toList());
            carDTO.setBookingList(car.getBookingList().stream().map(BookingEntity::getBookingId).toList());
            carDTOs.add(carDTO);
        }
        return ResponseEntity.ok().body(carDTOs);
    }

    @GetMapping("/chassis_number/{chassisNumber}")
    public ResponseEntity<List<CarResponseDTO>> getCarByChassisNumber(@PathVariable String chassisNumber){
        List<CarEntity> carEntity = carService.findByChassisNumber(chassisNumber);
        List<CarResponseDTO> carDTOs = new ArrayList<>();
        for (CarEntity car : carEntity) {
            CarResponseDTO carDTO = new CarResponseDTO();
            carDTO.setCarID(car.getCarID());
            carDTO.setLicensePlate(car.getLicensePlate());
            carDTO.setUser(car.getUser().getUserID());
            carDTO.setTypeCar(car.getTypeCar());
            carDTO.setChassisNumber(car.getChassisNumber());
            carDTO.setChargingType(car.getChargingType().getIdChargingType());
            carDTO.setActive(car.getIsActive());
            carDTO.setWaitingList(car.getWaitingList().stream().map(WaitingListEntity::getWaitingListId).toList());
            carDTO.setBookingList(car.getBookingList().stream().map(BookingEntity::getBookingId).toList());
            carDTOs.add(carDTO);
        }
        return ResponseEntity.ok().body(carDTOs);
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
    @PostMapping("/random_pin")
    public ResponseEntity<?> randomPin(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String postId = request.get("postId");
        int currentPin = carService.pinRandom();
        long maxSecond = carService.maxSecond(currentPin, postId);

        // Lưu PIN hiện tại vào Redis
        carService.storeCurrentPin(userId, currentPin);

        Map<String, Object> response = new HashMap<>();
        response.put("currentPin", currentPin);
        response.put("maxSecond", maxSecond);
        return ResponseEntity.ok(response);
    }
}
