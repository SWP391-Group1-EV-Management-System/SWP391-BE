package charging_manage_be.controller.charging;

import charging_manage_be.model.dto.charging.station.ChargingStationResponseDTO;
import charging_manage_be.model.dto.charging.post.PostResponseDTO;
import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.booking.WaitingListEntity;
import charging_manage_be.model.entity.charging.ChargingPostEntity;
import charging_manage_be.model.entity.charging.ChargingSessionEntity;
import charging_manage_be.model.entity.charging.ChargingStationEntity;
import charging_manage_be.model.entity.charging.ChargingTypeEntity;
import charging_manage_be.services.charging_station.ChargingStationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ChargingStationResponseDTO> getChargingStationById(@PathVariable String stationId) {
        ChargingStationEntity station = chargingStationService.getStationById(stationId);
        if (station == null) {
            throw new RuntimeException("Station not found with id: " + stationId);
        }
        else {
            ChargingStationResponseDTO stationDTO = new ChargingStationResponseDTO();
            stationDTO.setIdChargingStation(station.getIdChargingStation());
            stationDTO.setNameChargingStation(station.getNameChargingStation());
            stationDTO.setAddress(station.getAddress());
            stationDTO.setActive(station.isActive());
            stationDTO.setEstablishedTime(station.getEstablishedTime());
            stationDTO.setNumberOfPosts(station.getNumberOfPosts());
            stationDTO.setUserManagerName(station.getUserManager().getLastName());
            stationDTO.setChargingPostIds(station.getChargingPosts().stream().map(ChargingPostEntity::getIdChargingPost).toList());
            stationDTO.setChargingSessionIds(station.getChargingSession().stream().map(ChargingSessionEntity::getChargingSessionId).toList());
            return ResponseEntity.ok(stationDTO);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<ChargingStationResponseDTO>> getAllChargingStations() {
        List<ChargingStationResponseDTO> stations = chargingStationService.getAllStations().stream().map(chargingStationEntity -> {
            ChargingStationResponseDTO stationDTO = new ChargingStationResponseDTO();
            stationDTO.setIdChargingStation(chargingStationEntity.getIdChargingStation());
            stationDTO.setNameChargingStation(chargingStationEntity.getNameChargingStation());
            stationDTO.setAddress(chargingStationEntity.getAddress());
            stationDTO.setActive(chargingStationEntity.isActive());
            stationDTO.setEstablishedTime(chargingStationEntity.getEstablishedTime());
            stationDTO.setNumberOfPosts(chargingStationEntity.getNumberOfPosts());
            stationDTO.setUserManagerName(chargingStationEntity.getUserManager().getLastName());
            stationDTO.setChargingPostIds(chargingStationEntity.getChargingPosts().stream().map(ChargingPostEntity::getIdChargingPost).toList());
            stationDTO.setChargingSessionIds(chargingStationEntity.getChargingSession().stream().map(ChargingSessionEntity::getChargingSessionId).toList());
            return stationDTO;
        }).toList();
        return ResponseEntity.ok(stations);
    }

    @GetMapping("/amountPost/{stationId}")
    public ResponseEntity<Integer> getAmountPostInStation(@PathVariable ChargingStationEntity stationId) {
        ChargingStationEntity stationEntity = chargingStationService.updateNumberOfPosts(stationId);
        if (stationEntity == null) {
            throw new RuntimeException("Station not found with id: " + stationId.getIdChargingStation());
        }
        return ResponseEntity.ok(stationEntity.getNumberOfPosts());
    }

    @GetMapping("/posts/{stationId}" )
    public ResponseEntity<List<PostResponseDTO>> getAllPostsInStation(@PathVariable String stationId) {
        List<PostResponseDTO> posts = chargingStationService.getAllPostsInStation(stationId).stream().map(chargingPostEntity -> {
            PostResponseDTO postDTO = new PostResponseDTO();
            postDTO.setIdChargingPost(chargingPostEntity.getIdChargingPost());
            postDTO.setActive(chargingPostEntity.isActive());
            postDTO.setMaxPower(chargingPostEntity.getMaxPower());
            postDTO.setChargingFeePerKWh(chargingPostEntity.getChargingFeePerKWh());
            postDTO.setChargingStation(chargingPostEntity.getChargingStation().getNameChargingStation());
            postDTO.setChargingType(chargingPostEntity.getChargingType().stream().map(ChargingTypeEntity::getIdChargingType).toList());
            postDTO.setWaitingList(chargingPostEntity.getWaitingList().stream().map(WaitingListEntity::getWaitingListId).toList());
            postDTO.setBookings(chargingPostEntity.getBookings().stream().map(BookingEntity::getBookingId).toList());
            postDTO.setChargingSessions(chargingPostEntity.getChargingSessions().stream().map(ChargingSessionEntity::getChargingSessionId).toList());
            return postDTO;
        }).toList();
        return ResponseEntity.ok(posts);
    }
}
