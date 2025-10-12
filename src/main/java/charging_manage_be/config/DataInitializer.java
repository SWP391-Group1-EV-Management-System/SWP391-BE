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
import charging_manage_be.repository.user_reputations.UserReputationRepository;
import charging_manage_be.repository.users.UserRepository;
import charging_manage_be.repository.waiting_list.WaitingListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {
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
    private PaymentMethodRepository paymentMethodRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private ChargingSessionRepository chargingSessionRepository;
    @Autowired
    private WaitingListRepository waitingListRepository;

    @Override
    public void run(String... args) {
        // Initialize Reputation Levels
        if(reputationLevelRepository.count() > 0) {
            return; // Dữ liệu đã được khởi tạo, không cần thêm nữa
        }

        // Initialize Charging Types first
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

        // Initialize reputation levels
        ReputationLevelEntity goodLevel = new ReputationLevelEntity();
        goodLevel.setLevelID(1);
        goodLevel.setLevelName("Tốt");
        goodLevel.setMaxWaitMinutes(15);
        goodLevel.setDescription("Người dùng có uy tín tốt");
        goodLevel.setMaxScore(100);
        goodLevel.setMinScore(60);
        reputationLevelRepository.save(goodLevel);

        ReputationLevelEntity mediumLevel = new ReputationLevelEntity();
        mediumLevel.setLevelID(2);
        mediumLevel.setLevelName("Khá");
        mediumLevel.setMaxWaitMinutes(10);
        mediumLevel.setDescription("Người dùng có uy tín khá");
        mediumLevel.setMaxScore(59);
        mediumLevel.setMinScore(20);
        reputationLevelRepository.save(mediumLevel);

        ReputationLevelEntity badLevel = new ReputationLevelEntity();
        badLevel.setLevelID(3);
        badLevel.setLevelName("Xấu");
        badLevel.setMaxWaitMinutes(5);
        badLevel.setDescription("Người dùng có uy tín xấu");
        badLevel.setMaxScore(19);
        badLevel.setMinScore(0);
        reputationLevelRepository.save(badLevel);

        // Initialize Users (adding more users to reach 15, including more managers)
        UserEntity[] users = new UserEntity[15];
        String[] roles = {
            "DRIVER", "DRIVER", "DRIVER", "DRIVER", "DRIVER",  // 5 drivers
            "MANAGER", "MANAGER", "MANAGER", "MANAGER", "MANAGER", // 5 managers for stations 1-5
            "MANAGER", "MANAGER", "MANAGER", "MANAGER", "MANAGER"  // 5 managers for stations 6-10
        };

        for (int i = 0; i < 15; i++) {
            users[i] = new UserEntity();
            users[i].setUserID(String.format("USR%03d", i + 1));
            users[i].setFirstName("User");
            users[i].setLastName("" + (i + 1));
            users[i].setBirthDate(new Date());
            users[i].setGender(i % 2 == 0);
            users[i].setRole(roles[i]);
            users[i].setEmail("user" + (i + 1) + "@test.com");
            users[i].setPassword("password");
            users[i].setPhoneNumber("0123456" + String.format("%03d", i));
            users[i].setStatus(true);
            userRepository.save(users[i]);
        }

        // Initialize User Reputation (15 entries)
        Optional<ReputationLevelEntity> goodOpt = reputationLevelRepository.findById(1);
        Optional<ReputationLevelEntity> mediumOpt = reputationLevelRepository.findById(2);
        Optional<ReputationLevelEntity> badOpt = reputationLevelRepository.findById(3);

        if (goodOpt.isPresent() && mediumOpt.isPresent() && badOpt.isPresent()) {
            ReputationLevelEntity goodRep = goodOpt.get();
            ReputationLevelEntity mediumRep = mediumOpt.get();
            ReputationLevelEntity badRep = badOpt.get();

            for (int i = 0; i < 15; i++) {
                UserReputationEntity userRep = new UserReputationEntity();
                userRep.setUserReputationID("REP" + String.format("%03d", i + 1));
                userRep.setUser(users[i]);
                if (i < 6) userRep.setReputationLevel(goodRep);
                else if (i < 10) userRep.setReputationLevel(mediumRep);
                else userRep.setReputationLevel(badRep);
                userRep.setNotes("Reputation for user " + (i + 1));
                userRep.setCurrentScore(100); // Set current_score to 100
                userReputationRepository.save(userRep);
            }
        }

        // Initialize Cars (10 entries, only for drivers)
        String[] carTypes = {"Sedan", "SUV", "Hatchback", "Crossover", "MPV"};

        for (int i = 0; i < 10; i++) {
            CarEntity car = new CarEntity();
            car.setCarID("CAR" + String.format("%03d", i + 1));
            car.setLicensePlate(String.format("%02dA-%05d", 30 + i, 12345 + i));
            car.setUser(users[i % 5]); // Assign only to the first 5 users (DRIVERS)
            car.setTypeCar(carTypes[i % carTypes.length]);
            car.setChassisNumber("CHASSIS" + String.format("%03d", i + 1));
            car.setChargingType(i % 3 == 0 ? ccs : (i % 3 == 1 ? chademo : ac));
            carRepository.save(car);
        }

        // Initialize Charging Stations (10 entries)
        for (int i = 0; i < 10; i++) {
            ChargingStationEntity station = new ChargingStationEntity();
            station.setIdChargingStation("STA" + String.format("%03d", i + 1));
            station.setNameChargingStation("Trạm " + (char)('A' + i));
            station.setAddress((i + 1) * 100 + " Test Street, District " + (i + 1));
            station.setUserManager(users[i + 5]); // Assign to managers (users[5] through users[14])
            station.setNumberOfPosts(3 + i % 3);
            station.setActive(true);
            chargingStationRepository.save(station);
        }

        // Initialize Charging Posts (10 entries)
        ChargingStationEntity[] stations = chargingStationRepository.findAll().toArray(new ChargingStationEntity[0]);

        // Store charging posts array for later use
        ChargingPostEntity[] chargingPosts = new ChargingPostEntity[10];
        for (int i = 0; i < 10; i++) {
            ChargingPostEntity post = new ChargingPostEntity();
            post.setIdChargingPost("POST" + String.format("%03d", i + 1));
            post.setChargingStation(stations[i % stations.length]);
            post.setChargingType(Arrays.asList(ccs, chademo, ac));
            post.setChargingFeePerKWh(new BigDecimal("100").add(BigDecimal.valueOf(i * 10)));
            post.setMaxPower(new BigDecimal("10000").add(BigDecimal.valueOf(i * 1000)));
            post.setActive(true);
            chargingPosts[i] = chargingPostRepository.save(post);
        }

        // Initialize Payment Methods
        String[] paymentMethods = {"CASH", "BANK_TRANSFER", "MOMO", "VNPAY"};
        for (int i = 0; i < paymentMethods.length; i++) {
            PaymentMethodEntity method = new PaymentMethodEntity();
            method.setIdPaymentMethod("PM" + String.format("%03d", i + 1));
            method.setNamePaymentMethod(paymentMethods[i]);
            paymentMethodRepository.save(method);
        }

        // Initialize Bookings and related entities
        LocalDateTime now = LocalDateTime.of(2025, 10, 12, 8, 0); // Current context date

        // Create some bookings
        for (int i = 0; i < 10; i++) {
            UserEntity user = users[i % 5]; // Only drivers can book
            CarEntity userCar = null;
            // Find the user's car
            for (CarEntity car : carRepository.findAll()) {
                if (car.getUser().getUserID().equals(user.getUserID())) {
                    userCar = car;
                    break;
                }
            }
            if (userCar == null) continue;

            // Create booking
            BookingEntity booking = new BookingEntity();
            booking.setBookingId("BOK" + String.format("%03d", i + 1));
            booking.setUser(user);
            booking.setCar(userCar);
            booking.setChargingStation(stations[i % stations.length]);
            booking.setChargingPost(chargingPosts[i % chargingPosts.length]);
            booking.setMaxWaitingTime(15); // Default to 15 minutes
            booking.setCreatedAt(now.plusHours(i));
            booking.setStatus(i < 5 ? "COMPLETED" : "CANCEL"); // Set status based on whether it's completed or pending
            bookingRepository.save(booking);

            // Create charging session for completed bookings (first 5)
            if (i < 5) {
                ChargingSessionEntity session = new ChargingSessionEntity();
                session.setChargingSessionId("SES" + String.format("%03d", i + 1));
                session.setBooking(booking);
                session.setChargingPost(chargingPosts[i % chargingPosts.length]);
                session.setStation(stations[i % stations.length]);
                session.setUser(user);
                session.setUserManage(stations[i % stations.length].getUserManager());
                session.setStartTime(booking.getCreatedAt().plusMinutes(15));
                session.setExpectedEndTime(now.plusHours(i).plusHours(2));
                session.setDone(true);
                chargingSessionRepository.save(session);

                // Create payment for completed sessions
                Optional<PaymentMethodEntity> paymentMethod = paymentMethodRepository.findById("PM" + String.format("%03d", i % paymentMethods.length + 1));
                if (paymentMethod.isPresent()) {
                    PaymentEntity payment = new PaymentEntity();
                    payment.setPaymentId("PAY" + String.format("%03d", i + 1));
                    payment.setUser(user);
                    payment.setChargingSessionId(session.getChargingSessionId());
                    payment.setSession(session);
                    payment.setPrice(new BigDecimal("500000")); // 500,000 VND
                    payment.setPaymentMethod(paymentMethod.get());
                    payment.setPaid(true);
                    payment.setPaidAt(now.plusHours(i).plusHours(2).plusMinutes(5));
                    paymentRepository.save(payment);
                }
            } else {
                // Create waiting list entries for pending bookings
                WaitingListEntity waitingList = new WaitingListEntity();
                waitingList.setWaitingListId("WAIT" + String.format("%03d", i + 1));
                waitingList.setChargingStation(stations[i % stations.length]);
                waitingList.setUser(user);
                waitingList.setChargingPost(chargingPosts[i % chargingPosts.length]);
                waitingList.setBooking(booking);
                waitingList.setCar(userCar);
                waitingList.setExpectedWaitingTime(now.plusHours(i).plusMinutes(15));
                waitingListRepository.save(waitingList);
                // Update booking with waiting list reference
                booking.setWaitingList(waitingList);
                bookingRepository.save(booking);
            }
        }
    }
}
