package charging_manage_be.repository.users;

import charging_manage_be.model.entity.users.UserEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepositoryImpl implements UserRepository{

    @PersistenceContext // Dùng để đánh dấu EntityManager sẽ được tiêm bởi container quản lý
    // tiêm có nghĩa là tự động gán giá trị cho biến này khi đối tượng được khởi tạo
    // Thay vì khởi tạo EntityManager một cách thủ công là: EntityManager em = ...;
    private EntityManager entityManager;

    @Override
    @Transactional // Dùng để khởi tạo một giao dịch thay cho việc phải tự mình quản lý giao dịch như .begin, commit, rollback
    public boolean addUser(UserEntity user) {
        try{
            entityManager.persist(user);
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            // Hỏi thầy là có cần phải rollback khi tác động trực tiếp DB nhưng failed không
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateUser(UserEntity user) {
        try{
            entityManager.merge(user);
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteUser(String userID) {
        try{
            UserEntity user = entityManager.find(UserEntity.class, userID);
            if(user != null){
                entityManager.remove(user);
                return true;
            }
            return false;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public UserEntity getUserByID(String userID) {
        try{
            return entityManager.find(UserEntity.class, userID);
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<UserEntity> getAllUsers() {
        try{
            return entityManager.createQuery("from UserEntity", UserEntity.class).getResultList();
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
