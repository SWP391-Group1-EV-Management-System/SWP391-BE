//package charging_manage_be.model.entity.service_package;
//
//import charging_manage_be.model.entity.users.UserEntity;
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Entity
//@Table(name = "package_transaction")
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//public class PackageTransactionEntity {
//    @Id
//    @Column(name = "package_transaction_id", nullable = false)
//    private String packageTransactionId;
//    @ManyToOne
//    @JoinColumn(name = "user_id", nullable = false)
//    private UserEntity user;
//    @ManyToOne
//    @JoinColumn(name = "service_package_id", nullable = false)
//    private ServicePackageEntity servicePackage;
//
//
//
//
//}
