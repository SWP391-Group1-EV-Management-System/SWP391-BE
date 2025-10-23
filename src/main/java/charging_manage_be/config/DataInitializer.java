package charging_manage_be.config;

import charging_manage_be.model.entity.booking.BookingEntity;
import charging_manage_be.model.entity.booking.WaitingListEntity;
import charging_manage_be.model.entity.cars.CarEntity;
import charging_manage_be.model.entity.charging.ChargingPostEntity;
import charging_manage_be.model.entity.charging.ChargingSessionEntity;
import charging_manage_be.model.entity.charging.ChargingStationEntity;
import charging_manage_be.model.entity.charging.ChargingTypeEntity;
import charging_manage_be.model.entity.payments.PaymentEntity;
import charging_manage_be.model.entity.payments.PaymentMethodEntity;
import charging_manage_be.model.entity.reputations.ReputationLevelEntity;
import charging_manage_be.model.entity.reputations.UserReputationEntity;
import charging_manage_be.model.entity.service_package.PackageTransactionEntity;
import charging_manage_be.model.entity.service_package.PaymentServicePackageEntity;
import charging_manage_be.model.entity.service_package.ServicePackageEntity;
import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.repository.booking.BookingRepository;
import charging_manage_be.repository.cars.CarRepository;
import charging_manage_be.repository.charging_post.ChargingPostRepository;
import charging_manage_be.repository.charging_session.ChargingSessionRepository;
import charging_manage_be.repository.charging_station.ChargingStationRepository;
import charging_manage_be.repository.charging_type.ChargingTypeRepository;
import charging_manage_be.repository.payments.PaymentMethodRepository;
import charging_manage_be.repository.payments.PaymentRepository;
import charging_manage_be.repository.reputations.ReputationLevelRepository;
import charging_manage_be.repository.service_package.PackageTransactionRepository;
import charging_manage_be.repository.service_package.PaymentServicePackageRepository;
import charging_manage_be.repository.service_package.ServicePackageRepository;
import charging_manage_be.repository.user_reputations.UserReputationRepository;
import charging_manage_be.repository.users.UserRepository;
import charging_manage_be.repository.waiting_list.WaitingListRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

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
    @Autowired
    private ServicePackageRepository servicePackageRepository;
    @Autowired
    private PaymentMethodRepository paymentMethodRepository;
    @Autowired
    private PaymentServicePackageRepository paymentServicePackageRepository;
    @Autowired
    private PackageTransactionRepository packageTransactionRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private WaitingListRepository waitingListRepository;
    @Autowired
    private ChargingSessionRepository chargingSessionRepository;
    @Autowired
    private PaymentRepository paymentRepository;


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

        UserEntity driverC = new UserEntity();
        driverC.setUserID("DRV003");
        driverC.setFirstName("Driver");
        driverC.setLastName("C");
        driverC.setBirthDate(new Date());
        driverC.setGender(true);
        driverC.setRole("DRIVER");
        driverC.setEmail("driverc@test.com");
        driverC.setPassword("password");
        driverC.setPhoneNumber("0123456770");
        driverC.setStatus(false);
        userRepository.save(driverC);

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

        UserEntity manager2 = new UserEntity();
        manager2.setUserID("MGR002");
        manager2.setFirstName("Manager2");
        manager2.setLastName("Test");
        manager2.setBirthDate(new Date());
        manager2.setGender(true);
        manager2.setRole("MANAGER");
        manager2.setEmail("manager2@test.com");
        manager2.setPassword("password");
        manager2.setPhoneNumber("0987654321");
        manager2.setStatus(true);
        userRepository.save(manager2);

        UserEntity manager3 = new UserEntity();
        manager3.setUserID("MGR003");
        manager3.setFirstName("Manager3");
        manager3.setLastName("Test");
        manager3.setBirthDate(new Date());
        manager3.setGender(true);
        manager3.setRole("MANAGER");
        manager3.setEmail("manager3@test.com");
        manager3.setPassword("password");
        manager3.setPhoneNumber("0987654321");
        manager3.setStatus(true);
        userRepository.save(manager3);

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

        UserEntity admin = new UserEntity();
        admin.setUserID("ADM001");
        admin.setFirstName("Admin");
        admin.setLastName("Test");
        admin.setBirthDate(new Date());
        admin.setGender(true);
        admin.setRole("ADMIN");
        admin.setEmail("admin@test.com");
        admin.setPassword("password");
        admin.setPhoneNumber("0123498799");
        admin.setStatus(true);
        userRepository.save(admin);

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
        driverBRep.setCurrentScore(99);
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
        car1.setChargingType(ac);
        carRepository.save(car1);
        CarEntity car3 = new CarEntity();
        car3.setCarID("CAR003");
        car3.setLicensePlate("59A-99999");
        car3.setUser(driverB);
        car3.setTypeCar("Posche");
        car3.setChassisNumber("CHASSIS123921");
        car3.setChargingType(chademo);
        carRepository.save(car3);

        // Initialize Charging Station
        ChargingStationEntity stationA1 = new ChargingStationEntity();
        stationA1.setIdChargingStation("STA001");
        stationA1.setNameChargingStation("Trạm A1");
        stationA1.setAddress("123 Test Street");
        stationA1.setUserManager(manager);
        stationA1.setNumberOfPosts(3);
        stationA1.setActive(true);
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
        //Station 2
        // Initialize Charging Station
        ChargingStationEntity stationA2 = new ChargingStationEntity();
        stationA2.setIdChargingStation("STA002");
        stationA2.setNameChargingStation("Trạm A2");
        stationA2.setAddress("531 Truong Chinh");
        stationA2.setUserManager(manager2);
        stationA2.setNumberOfPosts(4);
        stationA2.setActive(true);
        chargingStationRepository.save(stationA2);

        ChargingStationEntity stationA3 = new ChargingStationEntity();
        stationA3.setIdChargingStation("STA003");
        stationA3.setNameChargingStation("Trạm A3");
        stationA3.setAddress("Bỏ Hoang");
        stationA3.setUserManager(manager3);
        stationA3.setNumberOfPosts(0);
        stationA3.setActive(false);
        chargingStationRepository.save(stationA3);
        // Initialize Charging Posts

        // Lấy các ChargingTypeEntity từ DB
        ChargingPostEntity post4 = new ChargingPostEntity();
        post4.setIdChargingPost("POST004");
        post4.setChargingStation(stationA2);
        post4.setChargingType(Arrays.asList(type1, type3));
        post4.setChargingFeePerKWh(new BigDecimal("100"));
        post4.setMaxPower(new BigDecimal("10000"));
        post4.setActive(true);
        chargingPostRepository.save(post4);

        ChargingPostEntity post5 = new ChargingPostEntity();
        post5.setIdChargingPost("POST005");
        post5.setChargingStation(stationA2);
        post5.setChargingType(Arrays.asList(type2,type3));
        post5.setChargingFeePerKWh(new BigDecimal("100"));
        post5.setMaxPower(new BigDecimal("10000"));
        post5.setActive(true);
        chargingPostRepository.save(post5);

        ChargingPostEntity post6 = new ChargingPostEntity();
        post6.setIdChargingPost("POST006");
        post6.setChargingStation(stationA2);
        post6.setChargingType(Arrays.asList(type1, type2,type3));
        post6.setChargingFeePerKWh(new BigDecimal("100"));
        post6.setMaxPower(new BigDecimal("10000"));
        post6.setActive(true);
        chargingPostRepository.save(post6);
        ChargingPostEntity post7 = new ChargingPostEntity();
        post7.setIdChargingPost("POST007");
        post7.setChargingStation(stationA2);
        post7.setChargingType(Arrays.asList(type1, type2));
        post7.setChargingFeePerKWh(new BigDecimal("100"));
        post7.setMaxPower(new BigDecimal("10000"));
        post7.setActive(true);
        chargingPostRepository.save(post7);
        // SERVICE PACKET


        ServicePackageEntity basicPkg = new ServicePackageEntity();
        basicPkg.setPackageId("PKG_BASIC");
        basicPkg.setPackageName("Basic Package");
        basicPkg.setDescription("Basic monthly package");
        basicPkg.setBillingCycle(2); // 1 month
        basicPkg.setPrice(new BigDecimal("299"));
        basicPkg.setUnit("MONTH");
        basicPkg.setQuota(1000);
        servicePackageRepository.save(basicPkg);

        ServicePackageEntity plsPkg = new ServicePackageEntity();
        plsPkg.setPackageId("PKG_PLUS");
        plsPkg.setPackageName("Plus Package");
        plsPkg.setDescription("Plus monthly package");
        plsPkg.setBillingCycle(5); // 1 month
        plsPkg.setPrice(new BigDecimal("599"));
        plsPkg.setUnit("MONTH");
        plsPkg.setQuota(2500);
        servicePackageRepository.save(plsPkg);

        ServicePackageEntity proPkg = new ServicePackageEntity();
        proPkg.setPackageId("PKG_PRO");
        proPkg.setPackageName("Pro Package");
        proPkg.setDescription("Pro monthly package");
        proPkg.setBillingCycle(10); // 1 month
        proPkg.setPrice(new BigDecimal("999"));
        proPkg.setUnit("MONTH");
        proPkg.setQuota(5000);
        servicePackageRepository.save(proPkg);
        // Payment method
        PaymentMethodEntity cash = new PaymentMethodEntity();
        cash.setIdPaymentMethod("PMT_CASH");
        cash.setNamePaymentMethod("Cash");
        paymentMethodRepository.save(cash);

        PaymentMethodEntity card = new PaymentMethodEntity();
        card.setIdPaymentMethod("PMT_MOMO");
        card.setNamePaymentMethod("Momo");
        paymentMethodRepository.save(card);

        PaymentMethodEntity packet = new PaymentMethodEntity();
        packet.setIdPaymentMethod("PMT_PACKAGE");
        packet.setNamePaymentMethod("Package");
        paymentMethodRepository.save(packet);
        // PACKAGE TRANSACTION AND PaymentServicePackageEntity
        PaymentServicePackageEntity psp1 = new PaymentServicePackageEntity();
        psp1.setPaymentServicePackageId("ABCDEE");
        psp1.setServicePackage(basicPkg);
        psp1.setPaymentMethod(card);
        psp1.setUser(driverA);
        psp1.setPaidAt(LocalDateTime.now());
        psp1.setPaid(true);
        psp1.setPrice(basicPkg.getPrice());
        PaymentServicePackageEntity savedPsp1 = paymentServicePackageRepository.save(psp1);

        PackageTransactionEntity tx1 = new PackageTransactionEntity();
        tx1.setPackageTransactionId("ABC123UUU");
        tx1.setUser(driverA);
        tx1.setServicePackage(basicPkg);
        tx1.setPaymentServicePackage(savedPsp1);
        tx1.setRemainingQuota(basicPkg.getQuota());
        tx1.setStatus("ACTIVE");
        tx1.setSignPackageAt(LocalDateTime.now());
        tx1.setExpirePackageAt(LocalDateTime.now().plusMonths(basicPkg.getBillingCycle()));
        packageTransactionRepository.save(tx1);

        // Create second payment + transaction (example unpaid)
        PaymentServicePackageEntity psp2 = new PaymentServicePackageEntity();
        psp2.setPaymentServicePackageId("ABC123ABC");
        psp2.setServicePackage(proPkg);
        psp2.setPaymentMethod(card);
        psp2.setUser(driverB);
        psp2.setPaidAt(LocalDateTime.now());
        psp2.setPaid(true);
        psp2.setPrice(proPkg.getPrice());
        PaymentServicePackageEntity savedPsp2 = paymentServicePackageRepository.save(psp2);

        PackageTransactionEntity tx2 = new PackageTransactionEntity();
        tx2.setPackageTransactionId("ABC123DEF");
        tx2.setUser(driverB);
        tx2.setServicePackage(proPkg);
        tx2.setPaymentServicePackage(savedPsp2);
        tx2.setRemainingQuota(proPkg.getQuota());
        tx2.setStatus("ACTIVE");
        tx2.setSignPackageAt(LocalDateTime.now());
        tx2.setExpirePackageAt(LocalDateTime.now().plusMonths(proPkg.getBillingCycle()));
        packageTransactionRepository.save(tx2);

        // BOOKING AND WAITINGLIST

        // Waiting list for driverA
        WaitingListEntity wl1 = new WaitingListEntity();
        wl1.setWaitingListId("WL001");
        wl1.setUser(driverA);
        wl1.setChargingStation(stationA1);
        wl1.setChargingPost(post1);
        wl1.setCar(car);
        wl1.setExpectedWaitingTime(LocalDateTime.now().plusMinutes(15));
        wl1.setStatus("BOOKING");
        wl1.setCreatedAt(LocalDateTime.now());
        wl1.setOutedAt(LocalDateTime.now());
        waitingListRepository.save(wl1);

        // Booking linked to wl1 (driverA)
        BookingEntity b1 = new BookingEntity();
        b1.setBookingId("BKG001");
        b1.setWaitingList(wl1);
        b1.setUser(driverA);
        b1.setChargingStation(stationA1);
        b1.setChargingPost(post1);
        b1.setCar(car);
        b1.setMaxWaitingTime(15);
        b1.setStatus("COMPLETE");
        b1.setArrivalTime(LocalDateTime.now().plusMinutes(10));
        b1.setCreatedAt(LocalDateTime.now());
        bookingRepository.save(b1);

        // Direct booking for driverB (no waiting list)
        BookingEntity b2 = new BookingEntity();
        b2.setBookingId("BKG002");
        b2.setWaitingList(null);
        b2.setUser(driverB);
        b2.setChargingStation(stationA2);
        b2.setChargingPost(post4);
        b2.setCar(car1);
        b2.setMaxWaitingTime(10);
        b2.setStatus("COMPLETE");
        b2.setArrivalTime(LocalDateTime.now().plusMinutes(10));
        b2.setCreatedAt(LocalDateTime.now());
        bookingRepository.save(b2);

        // Waiting list for driverB's other car
        WaitingListEntity wl2 = new WaitingListEntity();
        wl2.setWaitingListId("WL002");
        wl2.setUser(driverB);
        wl2.setChargingStation(stationA2);
        wl2.setChargingPost(post6);
        wl2.setCar(car3);
        wl2.setExpectedWaitingTime(LocalDateTime.now().plusMinutes(20));
        wl2.setStatus("BOOKING");
        wl2.setCreatedAt(LocalDateTime.now());
        wl2.setOutedAt(LocalDateTime.now());
        waitingListRepository.save(wl2);

        BookingEntity b4 = new BookingEntity();
        b4.setBookingId("BKG004");
        b4.setWaitingList(null);
        b4.setUser(driverB);
        b4.setChargingStation(stationA2);
        b4.setChargingPost(post6);
        b4.setCar(car3);
        b4.setMaxWaitingTime(20);
        b4.setStatus("CANCEL");
        b4.setCreatedAt(LocalDateTime.now());
        bookingRepository.save(b4);

        // Booking linked to wl2 (driverB)
        BookingEntity b3 = new BookingEntity();
        b3.setBookingId("BKG003");
        b3.setWaitingList(wl2);
        b3.setUser(driverB);
        b3.setChargingStation(stationA2);
        b3.setChargingPost(post6);
        b3.setCar(car3);
        b3.setMaxWaitingTime(20);
        b3.setStatus("COMPLETE");
        b3.setArrivalTime(LocalDateTime.now().plusMinutes(10));
        b3.setCreatedAt(LocalDateTime.now());
        bookingRepository.save(b3);

        WaitingListEntity wl3 = new WaitingListEntity();
        wl3.setWaitingListId("WL002");
        wl3.setUser(driverB);
        wl3.setChargingStation(stationA2);
        wl3.setChargingPost(post5);
        wl3.setCar(car3);
        wl3.setExpectedWaitingTime(LocalDateTime.now().plusMinutes(20));
        wl3.setStatus("CANCEL");
        wl3.setCreatedAt(LocalDateTime.now());
        wl3.setOutedAt(LocalDateTime.now());
        waitingListRepository.save(wl3);

        //PAYMENT AND SESSION

        // Session + payment for BKG001 (driverA)
        ChargingSessionEntity s1 = new ChargingSessionEntity();
        s1.setChargingSessionId("SESSION001");
        s1.setBooking(b1); // BKG001
        s1.setChargingPost(post1);
        s1.setStation(stationA1);
        s1.setUser(driverA);
        s1.setUserManage(staff);
        s1.setExpectedEndTime(LocalDateTime.now().plusMinutes(30));
        s1.setKWh(new BigDecimal("20.5"));
        s1.setTotalAmount(new BigDecimal("2050"));
        s1.setStartTime(LocalDateTime.now());
        s1.setEndTime(LocalDateTime.now());
        s1.setDone(true);
        ChargingSessionEntity savedS1 = chargingSessionRepository.save(s1);

        PaymentEntity p1 = new PaymentEntity();
        p1.setPaymentId("PAYMENT001");
        p1.setUser(driverA);
        p1.setPaymentMethod(card);
        p1.setPrice(savedS1.getTotalAmount());
        p1.setSession(savedS1);
        p1.setPaid(true);
        p1.setPaidAt(LocalDateTime.now());
        p1.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(p1);

        // Session + unpaid payment for BKG002 (driverB)
        ChargingSessionEntity s2 = new ChargingSessionEntity();
        s2.setChargingSessionId("SESSION002");
        s2.setBooking(b2); // BKG002
        s2.setChargingPost(post4);
        s2.setStation(stationA2);
        s2.setUser(driverB);
        s2.setUserManage(staff);
        s2.setExpectedEndTime(LocalDateTime.now().plusMinutes(40));
        s2.setKWh(new BigDecimal("15.0"));
        s2.setTotalAmount(new BigDecimal("1500"));
        s2.setStartTime(LocalDateTime.now());
        s2.setEndTime(LocalDateTime.now());
        s2.setDone(true);
        ChargingSessionEntity savedS2 = chargingSessionRepository.save(s2);

        PaymentEntity p2 = new PaymentEntity();
        p2.setPaymentId("PAYMENT001");
        p2.setUser(driverB);
        p2.setPaymentMethod(cash);
        p2.setPrice(savedS2.getTotalAmount());
        p2.setSession(savedS2);
        p2.setCreatedAt(LocalDateTime.now());
        // leave isPaid false (PrePersist will set createdAt/isPaid)
        paymentRepository.save(p2);

        // Session + payment for BKG003 (driverB, completed)
        ChargingSessionEntity s3 = new ChargingSessionEntity();
        s3.setChargingSessionId("SESSION003");
        s3.setBooking(b3); // BKG003
        s3.setChargingPost(post6);
        s3.setStation(stationA2);
        s3.setUser(driverB);
        s3.setUserManage(staff);
        s3.setExpectedEndTime(LocalDateTime.now().minusMinutes(5));
        s3.setKWh(new BigDecimal("12.0"));
        s3.setTotalAmount(new BigDecimal("1200"));
        s3.setEndTime(LocalDateTime.now());
        s3.setDone(true);
        s3.setStartTime(LocalDateTime.now());
        s3.setEndTime(LocalDateTime.now());
        s3.setDone(true);
        ChargingSessionEntity savedS3 = chargingSessionRepository.save(s3);

        ChargingSessionEntity s4 = new ChargingSessionEntity();
        s4.setChargingSessionId("SESSION004");
        //s4.setBooking(b3); // BKG003
        s4.setChargingPost(post6);
        s4.setStation(stationA2);
        s4.setUser(driverB);
        s4.setUserManage(staff);
        s4.setExpectedEndTime(LocalDateTime.now().minusMinutes(5));
        s4.setKWh(new BigDecimal("12.0"));
        s4.setTotalAmount(new BigDecimal("1500"));
        s4.setEndTime(LocalDateTime.now());
        s4.setDone(true);
        s4.setStartTime(LocalDateTime.now());
        s4.setEndTime(LocalDateTime.now());
        s4.setDone(true);
        ChargingSessionEntity savedS4 = chargingSessionRepository.save(s4);

        PaymentEntity p3 = new PaymentEntity();
        p3.setPaymentId("PAYMENT001");
        p3.setUser(driverB);
        p3.setPaymentMethod(packet);
        p3.setPrice(savedS3.getTotalAmount());
        p3.setSession(savedS3);
        p3.setPaid(true);
        p3.setPaidAt(LocalDateTime.now());
        p3.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(p3);

        PaymentEntity p4 = new PaymentEntity();
        p4.setPaymentId("PAYMENT002");
        p4.setUser(driverB);
        p4.setPaymentMethod(cash);
        p4.setPrice(savedS4.getTotalAmount());
        p4.setSession(savedS4);
        p4.setPaid(true);
        p4.setPaidAt(LocalDateTime.now());
        p4.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(p4);
    }
}
