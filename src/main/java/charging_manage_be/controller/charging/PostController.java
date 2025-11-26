package charging_manage_be.controller.charging;

import charging_manage_be.model.dto.charging.post.PostRequestDTO;
import charging_manage_be.model.dto.charging.post.PostResponseDTO;
import charging_manage_be.model.entity.charging.ChargingPostEntity;
import charging_manage_be.model.entity.charging.ChargingStationEntity;
import charging_manage_be.model.entity.charging.ChargingTypeEntity;
import charging_manage_be.services.charging_post.ChargingPostService;
import charging_manage_be.services.charging_station.ChargingStationService;
import charging_manage_be.services.charging_type.ChargingTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/charging/post")
public class PostController {
    @Autowired
    private ChargingPostService chargingPostService;
    @Autowired
    private ChargingStationService chargingStationService;
    @Autowired
    private ChargingTypeService chargingTypeService;

    @PostMapping("/create")
    public ResponseEntity<String> createChargingPost(@RequestBody PostRequestDTO postRequestDTO)
    {
        String stationId = postRequestDTO.getStationId();
        boolean isActive = postRequestDTO.isActive();
        List<Integer> listType = postRequestDTO.getListType();
        BigDecimal maxPower = postRequestDTO.getMaxPower();
        BigDecimal chargingFeePerKWh = postRequestDTO.getChargingFeePerKWh();
        chargingPostService.addPost(stationId, isActive, listType, maxPower, chargingFeePerKWh);
        chargingStationService.updateNumberOfPosts(chargingStationService.getStationById(stationId));
        return ResponseEntity.ok("Post create completed successfully");
    }
    // Java
// File: `src/main/java/charging_manage_be/controller/charging/PostController.java`

    @PutMapping ("/update/{postId}")
    public ResponseEntity<String> updateChargingPost(@PathVariable String postId, @RequestBody PostRequestDTO postRequestDTO)
    {
        ChargingPostEntity chargingPostEntity = chargingPostService.getChargingPostById(postId);
        if (chargingPostEntity == null) {
            return ResponseEntity.notFound().build();
        }

        // Capture old station before reassignment
        ChargingStationEntity oldStation = chargingPostEntity.getChargingStation();

        String stationId = postRequestDTO.getStationId();
        ChargingStationEntity newStation = chargingStationService.getStationById(stationId);
        if (newStation == null) {
            return ResponseEntity.badRequest().body("Target station not found with id: " + stationId);
        }

        boolean isActive = postRequestDTO.isActive();
        List<Integer> listType = postRequestDTO.getListType();
        BigDecimal maxPower = postRequestDTO.getMaxPower();
        BigDecimal chargingFeePerKWh = postRequestDTO.getChargingFeePerKWh();

        chargingPostEntity.setActive(isActive);
        chargingPostEntity.setMaxPower(maxPower);
        chargingPostEntity.setChargingFeePerKWh(chargingFeePerKWh);
        chargingPostEntity.setChargingStation(newStation);

        List<ChargingTypeEntity> chargingTypeEntities = listType.stream()
                .map(id -> chargingTypeService.getChargingTypeById(id))
                .collect(Collectors.toList());
        chargingPostEntity.setChargingType(chargingTypeEntities);

        chargingPostService.updatePost(chargingPostEntity);

        // Update counts for new station
        chargingStationService.updateNumberOfPosts(newStation);

        // If moved from a different station, update the old station count as well
        if (oldStation != null && !oldStation.getIdChargingStation().equals(newStation.getIdChargingStation())) {
            chargingStationService.updateNumberOfPosts(oldStation);
        }

        return ResponseEntity.ok("Post update completed successfully");
    }


    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDTO> getChargingPostById(@PathVariable String postId) {
        ChargingPostEntity post = chargingPostService.getChargingPostById(postId);
        List<Integer> listType = post.getChargingType().stream()
                .map(type -> type.getIdChargingType())
                .collect(Collectors.toList());
        List<String> listSession = post.getChargingSessions().stream()
                .map(session -> session.getChargingSessionId())
                .collect(Collectors.toList());
        List<String> listBooking = post.getBookings().stream()
                .map(booking -> booking.getBookingId())
                .collect(Collectors.toList());
        List<String> listWaiting = post.getWaitingList().stream()
                .map(waiting -> waiting.getWaitingListId())
                .collect(Collectors.toList());
        PostResponseDTO postR = new PostResponseDTO(post.getIdChargingPost(), post.isActive(), post.getMaxPower(), post.getChargingFeePerKWh(), post.getChargingStation().getIdChargingStation(), listType, listWaiting, listBooking, listSession);
        if (post != null) {
            return ResponseEntity.ok(postR);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<PostResponseDTO>> getAllChargingPosts() {
        List<ChargingPostEntity> posts = chargingPostService.getAllPosts();
        List<PostResponseDTO> postList = new ArrayList<>();
        for (ChargingPostEntity post : posts) {
            List<Integer> listType = post.getChargingType().stream()
                    .map(type -> type.getIdChargingType())
                    .collect(Collectors.toList());
            List<String> listSession = post.getChargingSessions().stream()
                    .map(session -> session.getChargingSessionId())
                    .collect(Collectors.toList());
            List<String> listBooking = post.getBookings().stream()
                    .map(booking -> booking.getBookingId())
                    .collect(Collectors.toList());
            List<String> listWaiting = post.getWaitingList().stream()
                    .map(waiting -> waiting.getWaitingListId())
                    .collect(Collectors.toList());
            PostResponseDTO postR = new PostResponseDTO(post.getIdChargingPost(), post.isActive(), post.getMaxPower(), post.getChargingFeePerKWh(), post.getChargingStation().getIdChargingStation(), listType, listWaiting, listBooking, listSession);
            postList.add(postR);
        }
        return ResponseEntity.ok(postList);
    }

}
/*
    @Id
    @Column (name = "id_charging_post")
    private String idChargingPost;
    @Column (name = "is_active",nullable = false)
    private boolean isActive;
    @Column (name="maxPower", nullable = false)
    private BigDecimal maxPower;
    @Column (name="charging_fee_per_kwh", nullable = false)
    private BigDecimal chargingFeePerKWh;
    @ManyToOne
    @JoinColumn(name = "id_charging_station", nullable = false)
    private ChargingStationEntity chargingStation;
    @OneToMany(mappedBy = "chargingPost")
    private List<ChargingSessionEntity> chargingSessions;
    @ManyToMany
    @JoinTable(
            name = "charging_type_post",
            joinColumns = @JoinColumn(name = "id_charging_post"),
            inverseJoinColumns = @JoinColumn(name = "id_charging_type")
    )
    private List<ChargingTypeEntity> chargingType;
    @OneToMany(mappedBy = "chargingPost" )
    private List<WaitingListEntity> waitingList;

    @OneToMany(mappedBy = "chargingPost" )
    private List<BookingEntity> bookings;

 */