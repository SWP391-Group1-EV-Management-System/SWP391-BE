package charging_manage_be.services.charging_station;

import charging_manage_be.model.dto.agent.LocationRequest;
import charging_manage_be.model.dto.agent.StationAndPost;
import charging_manage_be.model.dto.charging.station.ChargingStationRequestDTO;
import charging_manage_be.model.entity.charging.ChargingPostEntity;
import charging_manage_be.model.entity.charging.ChargingStationEntity;
import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.repository.charging_station.ChargingStationRepository;
import charging_manage_be.repository.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.xml.stream.Location;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static charging_manage_be.util.RandomId.generateRandomId;
@Service
public class ChargingStationServiceImpl implements  ChargingStationService {
    // cấu hình độ dài của id
    private final int characterLength = 2;
    private final int numberLength = 2;
    @Autowired
    private ChargingStationRepository chargingStationRepository;
    @Autowired
    private UserRepository userRepository;

    @Value("${opencage.api.key}")
    private String apiKey;
    private static final String OPENCAGE_GEOCODING_URL = "https://api.opencagedata.com/geocode/v1/json";

    @Override
    public ChargingStationEntity updateNumberOfPosts(ChargingStationEntity station) {
        // cập nhật số lượng trụ theo kích thước của list trụ sạc
        // gọi hàm này sau khi thêm hoặc xóa trụ sạc
         station.setNumberOfPosts(station.getChargingPosts().size());
        chargingStationRepository.save(station);
        return station;
    }
    private String generateUniqueId() {
        String newId;
        do {
            newId = generateRandomId(characterLength, numberLength);
        } while (isPaymentIdExists(newId));
        return newId;
    }

    @Override
    public boolean isPaymentIdExists(String id) {
        return chargingStationRepository.existsById(id);
    }

    @Override
    public boolean addStation(ChargingStationRequestDTO station) {
        UserEntity manager = userRepository.findById(station.getUserManagerId()).orElse(null);
        ChargingStationEntity newStation = new ChargingStationEntity();
        newStation.setIdChargingStation(generateUniqueId());
        newStation.setNameChargingStation(station.getNameChargingStation());
        newStation.setAddress(station.getAddress());
        newStation.setActive(station.isActive());
        newStation.setUserManager(manager);
        newStation.setNumberOfPosts(station.getNumberOfPosts());

        // Lấy tọa độ từ địa chỉ và set vào station
        double[] coordinates = getCoordinatesFromAddress(station.getAddress());
        if (coordinates != null) {
            newStation.setLatitude(coordinates[0]);
            newStation.setLongitude(coordinates[1]);
        }

        chargingStationRepository.save(newStation);
        return true;
    }

    @Override
    public ChargingStationEntity getStationById(String stationId)
    {
        return chargingStationRepository.findByIdChargingStation(stationId);
    }

    @Override
    public boolean updateStation(String stationId, ChargingStationRequestDTO stationRequestDTO){
        ChargingStationEntity chargingStation = chargingStationRepository.findByIdChargingStation(stationId);
        if(chargingStation == null){
            throw new RuntimeException("Station not found with id: " + stationId);
        }
        UserEntity manager = userRepository.findById(stationRequestDTO.getUserManagerId()).orElse(null);
        chargingStation.setUserManager(manager);
        chargingStation.setAddress(stationRequestDTO.getAddress());
        chargingStation.setActive(stationRequestDTO.isActive());
        chargingStation.setNameChargingStation(stationRequestDTO.getNameChargingStation());
        chargingStation.setNumberOfPosts(stationRequestDTO.getNumberOfPosts());
        chargingStationRepository.save(chargingStation);
        return true;
    }

    @Override
    public List<ChargingStationEntity> getAllStations()
    {
        return chargingStationRepository.findAll();
    }
    @Override
    public List<ChargingPostEntity> getAllPostsInStation(String stationId)
    {
        ChargingStationEntity station = chargingStationRepository.findByIdChargingStation(stationId);
        if(station == null)
        {
            return null;
        }
        return station.getChargingPosts();
    }

    @Override
    public List<ChargingStationEntity> getAllStationAvailable() {
        return chargingStationRepository.findAllByIsActiveTrue();
    }


    public double[] getCoordinatesFromAddress(String address) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(OPENCAGE_GEOCODING_URL)
                    .queryParam("q", address)
                    .queryParam("key", apiKey)
                    .toUriString(); //Xây dựng URL để truy cập vào API

            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(url, String.class); // // Tạo một yêu cầu Http Get vào API Để lấy về chuỗi json (dựa vào url ở trên để truy cập API)
            if (response == null) {
                System.out.println("No response from API");
                return null;
            }


            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject(); // Chuỗi chuỗi json thành String
            JsonArray results = jsonResponse.get("results").getAsJsonArray(); // Lấy theo mảng results trong chuỗi json vừa được parse sang String

            if (results.size() > 0) {
                JsonObject first = results.get(0).getAsJsonObject(); // Lấy ra phần tử đầu tiên trong mảng results
                JsonObject geometry = first.get("geometry").getAsJsonObject(); // Geometry là lấy ra được tọa độ latitude và longitude trong chuỗi json trả ra
                double latitude = geometry.get("lat").getAsDouble(); // lat ở đây là latitude
                double longitude = geometry.get("lng").getAsDouble(); // lng là longitude
                System.out.println("Latitude: " + latitude + ", Longitude: " + longitude);
                return new double[]{latitude, longitude};
            }
            else{
                System.out.println("No results found for address: " + address);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<ChargingStationEntity> findNearestStations(LocationRequest request) {
        List<ChargingStationEntity> allStations = chargingStationRepository.findAll();

        // return bọc một chuỗi stream và trã về, tất cả những dòng return con là giá trị con của nó
        //stream() cho phép lặp qua từng phần tử con trong list và gồm có lọc, lặp, sắp xếp, gom lại
        //peek lấy mỗi phần từ con từ stream
        return allStations.stream()
                .map(station -> Map.entry( // .map biến đổi những phần tử thành thứ khác
                        station,
                        calculateDistance(
                                request.getLatitude(),
                                request.getLongitude(),
                                station.getLatitude(),
                                station.getLongitude()
                        )
                ))//mục đích lưu tạm giá trị distance vào Map để so sánh
                // tiến hành lọc Map lấy value so sánh với bán kính
                .filter(entry -> entry.getValue() <= request.getRadiusKm())
                .sorted(Map.Entry.comparingByValue()) // sắp xếp bé đến lớn
                .limit(request.getLimit()) // chỉ lấy limit phần tử
                .map(Map.Entry::getKey)//lấy lại entity station
                .collect(Collectors.toList());
    }

    @Override
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Bán kính Trái Đất (km)

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Khoảng cách tính bằng km
    }

    @Override
    public long countTotalActiveStations() {
        return chargingStationRepository.countByIsActiveTrue();
    }

    @Override
    public ChargingStationEntity getStationByChargingPost(ChargingPostEntity chargingPost) {
        return chargingStationRepository.findStationByChargingPostEntity(chargingPost.getIdChargingPost()).orElse(null);
    }

    @Override
    public ChargingStationEntity getStationByUserId(String userId) {
        return chargingStationRepository.findByUserManager_UserID(userId).orElse(null);
    }

}
