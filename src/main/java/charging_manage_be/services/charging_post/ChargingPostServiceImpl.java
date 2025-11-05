package charging_manage_be.services.charging_post;

import charging_manage_be.model.dto.agent.StationAndPost;
import charging_manage_be.model.entity.charging.ChargingPostEntity;
import charging_manage_be.model.entity.charging.ChargingStationEntity;
import charging_manage_be.model.entity.charging.ChargingTypeEntity;
import charging_manage_be.repository.charging_post.ChargingPostRepository;
import charging_manage_be.repository.charging_type.ChargingTypeRepository;
import charging_manage_be.services.booking.BookingService;
import charging_manage_be.services.charging_session.ChargingSessionService;
import charging_manage_be.services.charging_station.ChargingStationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static charging_manage_be.util.RandomId.generateRandomId;
@Service
public class ChargingPostServiceImpl implements ChargingPostService {
    private final int characterLength = 2;
    private final int numberLength = 1;
    @Autowired // vì sử dụng bản spring boot khá cao nên không cần @Autowired vẫn chạy được

    private ChargingPostRepository ChargingPostRepository;
    @Autowired
    private ChargingTypeRepository chargingTypeRepository;
    @Autowired
    private ChargingStationService stationService;
    @Autowired
    @Lazy
    private ChargingSessionService chargingSessionService;

    @Autowired
    @Lazy
    private BookingService bookingService;

    // ChargingPostServiceImpl postService = context.getBean(ChargingPostServiceImpl.class); gọi trong main
    private String generateUniqueId() {
        String newId;
        do {
            newId = generateRandomId(characterLength, numberLength);
        } while (isPaymentIdExists(newId));
        return newId;
    }

    @Override
    public ChargingPostEntity getChargingPostById(String id) {
        if (!ChargingPostRepository.existsById(id)) {
            return null;
        }
        return ChargingPostRepository.findById(id).get();
    }

    public boolean isPaymentIdExists(String id) {
        return ChargingPostRepository.existsById(id);
    }

    @Override
    public boolean addPost(String stationId, boolean isActive, List<Integer> listType, BigDecimal maxPower, BigDecimal chargingFeePerKWh) {
        var post = new ChargingPostEntity();
        post.setIdChargingPost(generateUniqueId());
        post.setActive(isActive);
        List<ChargingTypeEntity> listChargingType = new ArrayList<>();
        for (int i : listType) {
            ChargingTypeEntity type = chargingTypeRepository.findById(i).orElse(null);
            if (type != null) {
                listChargingType.add(type);
            }
        }
        post.setChargingType(listChargingType);
        post.setMaxPower(maxPower);
        post.setChargingFeePerKWh(chargingFeePerKWh);
        var station = stationService.getStationById(stationId);
        if (station == null) {
            return false;
        }
        post.setChargingStation(station);
        ChargingPostRepository.save(post);
        return true;
    }

    @Override
    public boolean updatePost(ChargingPostEntity post) {
        if (post == null || !ChargingPostRepository.existsById(post.getIdChargingPost())) {
            return false;
        }
        ChargingPostRepository.save(post);
        return true;
    }

    @Override
    public List<ChargingPostEntity> getAllPosts() {
        return ChargingPostRepository.findAll();
    }

    @Override
    public Map<String, Boolean> getPostAvailabilityMap(List<ChargingPostEntity> posts) {
        Map<String, Boolean> map = new HashMap<>();

        if (posts != null && !posts.isEmpty()) {
            for (ChargingPostEntity post : posts) {
                boolean isIdle = bookingService.isPostIdleInBooking(post.getIdChargingPost())
                        && chargingSessionService.isPostIdleBySession(post.getIdChargingPost());
                map.put(post.getIdChargingPost(), isIdle);
            }
        }

        return map;
    }
    @Override
    public StationAndPost mapToDTO(ChargingStationEntity station, Double userLat, Double userLon) {
        StationAndPost dto = new StationAndPost();

        // Map basic fields
        dto.setIdChargingStation(station.getIdChargingStation());
        dto.setNameChargingStation(station.getNameChargingStation());
        dto.setAddress(station.getAddress());
        dto.setActive(station.isActive());
        dto.setEstablishedTime(station.getEstablishedTime());
        dto.setNumberOfPosts(station.getNumberOfPosts());
        dto.setLatitude(station.getLatitude());
        dto.setLongitude(station.getLongitude());

        // Tính distance nếu có tọa độ người dùng
        if (userLat != null && userLon != null) {
            double distance = stationService.calculateDistance(
                    userLat, userLon,
                    station.getLatitude(), station.getLongitude()
            );
            dto.setDistanceKm(Math.round(distance * 100.0) / 100.0); // Làm tròn 2 chữ số
            // nhân với 100 để giữ toàn vẹn 2 số sau dấu phẩy tránh bị làm tròn mất sau đó chia lại để hiển thị đúng gía trị ( Math.round(478,79899) = 479)
        }

        // Map post availability
        dto.setPostAvailable(getPostAvailabilityMap(station.getChargingPosts()));

        return dto;
    }

    @Override
    public boolean isPostGotBooking(String postId) {
        return bookingService.isPostIdleInBooking(postId) && chargingSessionService.isPostIdleBySession(postId);
        // return true khi cả 2 đều true
        // còn lại là false
    }
}


//    public boolean addPost(String stationId, boolean isActive, BigDecimal changingFeePerKWh, BigDecimal maxPower)
//    {
//        var post = new ChargingPostEntity();
//        post.setIdChargingPost(generateUniquePaymentId());
//        post.setActive(isActive);
//        post.setChargingFeePerKWh(changingFeePerKWh);
//        post.setMaxPower(maxPower);
//        var station = new ChargingStationRepositoryImpl().getStationById(stationId);
//        if(station == null)
//        {
//            return false;
//        }
//        post.setChargingStation(station);
//        // hàm add này phải gọi hàm update số trụ sạc trong station
//        // gọi bằng Spring boot
//        stationService.updateNumberOfPosts(station);
//        return ChargingPostRepository.addPost(post);
//
//    }



