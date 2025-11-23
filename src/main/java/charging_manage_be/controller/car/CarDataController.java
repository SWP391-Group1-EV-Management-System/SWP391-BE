package charging_manage_be.controller.car;

import charging_manage_be.model.entity.cars.CarDataEntity;
import charging_manage_be.services.car.CarDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/car_data")
public class CarDataController {
    @Autowired
    CarDataService carDataService;
    @GetMapping("/all")
    public ResponseEntity<List<CarDataEntity>> getAllCarData() {

        List<CarDataEntity> list  = carDataService.getAllCarData();
        return ResponseEntity.ok(list);
    }
}
