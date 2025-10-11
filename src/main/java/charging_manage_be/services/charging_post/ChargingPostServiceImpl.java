package charging_manage_be.services.charging_post;

import charging_manage_be.model.entity.charging.ChargingPostEntity;
import charging_manage_be.model.entity.charging.ChargingTypeEntity;
import charging_manage_be.repository.charging_post.ChargingPostRepository;
import charging_manage_be.repository.charging_type.ChargingTypeRepository;
import charging_manage_be.services.charging_station.ChargingStationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
   // ChargingPostServiceImpl postService = context.getBean(ChargingPostServiceImpl.class); gọi trong main
    private String generateUniqueId() {
        String newId;
        do {
            newId = generateRandomId(characterLength, numberLength);
        } while (isPaymentIdExists(newId));
        return newId;
    }
    @Override
    public ChargingPostEntity getChargingPostById(String id)
    {
        if(!ChargingPostRepository.existsById(id))
        {
            return null;
        }
        return ChargingPostRepository.findById(id).get();
    }

    public boolean isPaymentIdExists(String id) {
        return ChargingPostRepository.existsById(id);
    }

    @Override
    public boolean addPost(String stationId, boolean isActive, List<Integer> listType, BigDecimal maxPower, BigDecimal chargingFeePerKWh)
    {
        var post = new ChargingPostEntity();
        post.setIdChargingPost(generateUniqueId());
        post.setActive(isActive);
        List<ChargingTypeEntity> listChargingType =  new ArrayList<>();
        for(int i : listType)
        {
            ChargingTypeEntity type = chargingTypeRepository.findById(i).orElse(null);
            if(type != null) {
                listChargingType.add(type);
            }
        }
        post.setChargingType(listChargingType);
        post.setMaxPower(maxPower);
        post.setChargingFeePerKWh(chargingFeePerKWh);
        var station = stationService.getStationById(stationId);
        if(station == null)
        {
            return false;
        }
        post.setChargingStation(station);
        ChargingPostRepository.save(post);
        return true;
    }

    @Override
    public boolean updatePost(ChargingPostEntity post)

    {
        if(post == null || !ChargingPostRepository.existsById(post.getIdChargingPost()))
        {
            return false;
        }
        ChargingPostRepository.save(post);
        return true;
    }

    @Override
    public List<ChargingPostEntity> getAllPosts() {
        return ChargingPostRepository.findAll();
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


}
