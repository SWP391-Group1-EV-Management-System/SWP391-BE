package charging_manage_be.controller.charging;

import charging_manage_be.model.entity.charging.ChargingPostEntity;
import charging_manage_be.services.charging_post.ChargingPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/charging/post")
public class PostController {
    @Autowired
    private ChargingPostService chargingPostService;

    @PostMapping("/create")
    public ResponseEntity<String> createChargingPost(@RequestBody String stationId,
                                                        boolean isActive,
                                                        List<Integer> listType,
                                                        BigDecimal maxPower,
                                                        BigDecimal chargingFeePerKWh,
                                                        String postId)
    {
        ChargingPostEntity post = new ChargingPostEntity();
        post.setIdChargingPost(postId);
        chargingPostService.addPost(stationId, isActive, listType, maxPower, chargingFeePerKWh);

        return ResponseEntity.ok("Post create completed successfully");
    }
    @PostMapping ("/update")
    public ResponseEntity<String> updateChargingPost(@PathVariable String postId)
    {
        chargingPostService.updatePost(ChargingPostEntity);
        return ResponseEntity.ok("Post update completed successfully");
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