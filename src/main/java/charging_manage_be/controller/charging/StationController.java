package charging_manage_be.controller.charging;

import charging_manage_be.model.entity.charging.ChargingPostEntity;
import charging_manage_be.model.entity.charging.ChargingStationEntity;
import charging_manage_be.services.charging_station.ChargingStationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/charging/station")
public class StationController {

    @Autowired
    private ChargingStationService chargingStationService;

    @PostMapping("/create")
    public ResponseEntity<String> createChargingStation(@RequestBody ChargingStationEntity chargingStationEntity) {
        chargingStationService.addStation(chargingStationEntity);
        return ResponseEntity.ok("Station create completed successfully");
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateChargingStation(@PathVariable String stationId, @RequestBody ChargingStationEntity chargingStationEntity) {
        try{
            chargingStationEntity.setIdChargingStation(stationId);
            chargingStationService.updateFullStation(chargingStationEntity);
            return ResponseEntity.ok("Station update completed successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{stationId}")
    public ResponseEntity<ChargingStationEntity> getChargingStationById(@PathVariable String stationId) {
        ChargingStationEntity station = chargingStationService.getStationById(stationId);
        if (station != null) {
            return ResponseEntity.ok(station);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<ChargingStationEntity>> getAllChargingStations() {
        List<ChargingStationEntity> stations = chargingStationService.getAllStations();
        return ResponseEntity.ok(stations);
    }

    @GetMapping("/posts/{stationId}" )
    public ResponseEntity<List<ChargingPostEntity>> getAllPostsInStation(@PathVariable String stationId) {
        List<ChargingPostEntity> posts = chargingStationService.getAllPostsInStation(stationId);
        return ResponseEntity.ok(posts);
    }

}
