package charging_manage_be.config;

import charging_manage_be.model.entity.cars.CarEntity;
import charging_manage_be.model.entity.charging.ChargingPostEntity;
import charging_manage_be.model.entity.charging.ChargingStationEntity;
import charging_manage_be.model.entity.charging.ChargingTypeEntity;
import charging_manage_be.model.entity.reputations.ReputationLevelEntity;
import charging_manage_be.model.entity.reputations.UserReputationEntity;
import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.repository.cars.CarRepository;
import charging_manage_be.repository.charging_post.ChargingPostRepository;
import charging_manage_be.repository.charging_station.ChargingStationRepository;
import charging_manage_be.repository.charging_type.ChargingTypeRepository;
import charging_manage_be.repository.reputations.ReputationLevelRepository;
import charging_manage_be.repository.user_reputations.UserReputationRepository;
import charging_manage_be.repository.users.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;

@Component
public class DataInitializer  implements CommandLineRunner {
    @Autowired
    private ReputationLevelRepository reputationLevelRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserReputationRepository userReputationRepository;
    @Autowired
    private CarRepository carRepository;
    @Autowired
    private ChargingTypeRepository chargingTypeRepository;
    @Autowired
    private ChargingStationRepository chargingStationRepository;
    @Autowired
    private ChargingPostRepository chargingPostRepository;
    @Override
    public void run(String... args) throws Exception {
        // Initialize Reputation Levels
        if(reputationLevelRepository.count() > 0) {
            return; // Dữ liệu đã được khởi tạo, không cần thêm nữa
        }
        ReputationLevelEntity good = new ReputationLevelEntity();
        good.setLevelID(1);
        good.setLevelName("Tốt");
        good.setMaxWaitMinutes(30);
        good.setMinScore(71);
        good.setMaxScore(100);
        good.setDescription("Người dùng có uy tín tốt");
        reputationLevelRepository.save(good);

        ReputationLevelEntity medium = new ReputationLevelEntity();
        medium.setLevelID(2);
        medium.setLevelName("Khá");
        medium.setMaxWaitMinutes(20);
        medium.setMinScore(31);
        medium.setMaxScore(70);
        medium.setDescription("Người dùng có uy tín khá");
        reputationLevelRepository.save(medium);

        ReputationLevelEntity bad = new ReputationLevelEntity();
        bad.setLevelID(3);
        bad.setLevelName("Xấu");
        bad.setMaxWaitMinutes(10);
        bad.setMinScore(0);
        bad.setMaxScore(30);
        bad.setDescription("Người dùng có uy tín xấu");
        reputationLevelRepository.save(bad);

        // Initialize Users
        UserEntity driverA = new UserEntity();
        driverA.setUserID("DRV001");
        driverA.setFirstName("Driver");
        driverA.setLastName("A");
        driverA.setBirthDate(new Date());
        driverA.setGender(true);
        driverA.setRole("DRIVER");
        driverA.setEmail("drivera@test.com");
        driverA.setPassword("password");
        driverA.setPhoneNumber("0123456789");
        driverA.setStatus(true);
        userRepository.save(driverA);
        UserEntity driverB = new UserEntity();
        driverB.setUserID("DRV002");
        driverB.setFirstName("Driver");
        driverB.setLastName("B");
        driverB.setBirthDate(new Date());
        driverB.setGender(true);
        driverB.setRole("DRIVER");
        driverB.setEmail("driverb@test.com");
        driverB.setPassword("password");
        driverB.setPhoneNumber("0123456780");
        driverB.setStatus(true);
        userRepository.save(driverB);

        UserEntity manager = new UserEntity();
        manager.setUserID("MGR001");
        manager.setFirstName("Manager");
        manager.setLastName("Test");
        manager.setBirthDate(new Date());
        manager.setGender(true);
        manager.setRole("MANAGER");
        manager.setEmail("manager@test.com");
        manager.setPassword("password");
        manager.setPhoneNumber("0987654321");
        manager.setStatus(true);
        userRepository.save(manager);

        UserEntity staff = new UserEntity();
        staff.setUserID("STF001");
        staff.setFirstName("Staff");
        staff.setLastName("Test");
        staff.setBirthDate(new Date());
        staff.setGender(true);
        staff.setRole("STAFF");
        staff.setEmail("staff@test.com");
        staff.setPassword("password");
        staff.setPhoneNumber("0123498765");
        staff.setStatus(true);
        userRepository.save(staff);

        // Initialize User Reputation
        UserReputationEntity driverARep = new UserReputationEntity();
        driverARep.setUserReputationID("REP001");
        driverARep.setUser(driverA);
        driverARep.setReputationLevel(good);
        driverARep.setCurrentScore(100);
        driverARep.setNotes("Initial reputation");
        userReputationRepository.save(driverARep);

        UserReputationEntity driverBRep = new UserReputationEntity();
        driverBRep.setUserReputationID("REP002");
        driverBRep.setUser(driverB);
        driverBRep.setReputationLevel(bad);
        driverBRep.setCurrentScore(10);
        driverBRep.setNotes("Initial reputation");
        userReputationRepository.save(driverBRep);

        // Initialize Charging Types
        ChargingTypeEntity ccs = new ChargingTypeEntity();
        ccs.setIdChargingType(1);
        ccs.setNameChargingType("CCS");
        chargingTypeRepository.save(ccs);

        ChargingTypeEntity chademo = new ChargingTypeEntity();
        chademo.setIdChargingType(2);
        chademo.setNameChargingType("CHAdeMO");
        chargingTypeRepository.save(chademo);
        ChargingTypeEntity ac = new ChargingTypeEntity();
        ac.setIdChargingType(3);
        ac.setNameChargingType("AC");
        chargingTypeRepository.save(ac);

        // Initialize Car
        CarEntity car = new CarEntity();
        car.setCarID("CAR001");
        car.setLicensePlate("29A-12345");
        car.setUser(driverA);
        car.setTypeCar("Sedan");
        car.setChassisNumber("CHASSIS001");
        car.setChargingType(ccs);
        carRepository.save(car);
        CarEntity car1 = new CarEntity();
        car1.setCarID("CAR002");
        car1.setLicensePlate("59A-12345");
        car1.setUser(driverB);
        car1.setTypeCar("Sedan");
        car1.setChassisNumber("CHASSIS123");
        car1.setChargingType(ccs);
        carRepository.save(car1);

        // Initialize Charging Station
        ChargingStationEntity stationA1 = new ChargingStationEntity();
        stationA1.setIdChargingStation("STA001");
        stationA1.setNameChargingStation("Trạm A1");
        stationA1.setAddress("123 Test Street");
        stationA1.setUserManager(manager);
        stationA1.setNumberOfPosts(3);
        chargingStationRepository.save(stationA1);

        // Initialize Charging Posts

        // Lấy các ChargingTypeEntity từ DB
        ChargingTypeEntity type1 = chargingTypeRepository.findById(1).get();
        ChargingTypeEntity type2 = chargingTypeRepository.findById(2).get();
        ChargingTypeEntity type3 = chargingTypeRepository.findById(3).get();
        ChargingPostEntity post1 = new ChargingPostEntity();
        post1.setIdChargingPost("POST001");
        post1.setChargingStation(stationA1);
        post1.setChargingType(Arrays.asList(type1, type2,type3));
        post1.setChargingFeePerKWh(new BigDecimal("100"));
        post1.setMaxPower(new BigDecimal("10000"));
        post1.setActive(true);
        chargingPostRepository.save(post1);

        ChargingPostEntity post2 = new ChargingPostEntity();
        post2.setIdChargingPost("POST002");
        post2.setChargingStation(stationA1);
        post2.setChargingType(Arrays.asList(type1, type2,type3));
        post2.setChargingFeePerKWh(new BigDecimal("100"));
        post2.setMaxPower(new BigDecimal("10000"));
        post2.setActive(true);
        chargingPostRepository.save(post2);

        ChargingPostEntity post3 = new ChargingPostEntity();
        post3.setIdChargingPost("POST003");
        post3.setChargingStation(stationA1);
        post3.setChargingType(Arrays.asList(type1, type2,type3));
        post3.setChargingFeePerKWh(new BigDecimal("100"));
        post3.setMaxPower(new BigDecimal("10000"));
        post3.setActive(true);
        chargingPostRepository.save(post3);
    }
}
