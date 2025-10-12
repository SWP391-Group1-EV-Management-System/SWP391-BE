package charging_manage_be.services.status_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserStatusService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    public void setUserStatus(String userId, String status) {
        redisTemplate.opsForHash().put("user:" + userId, "status", status);
        //redisTemplate.expire("user:" + userId, Duration.ofHours(1));
    }

    public String getUserStatus(String userId) {
        return (String) redisTemplate.opsForHash().get("user:" + userId, "status");
    }

    public void idleUserStatus(String userId) {
        redisTemplate.opsForHash().put("user:" + userId, "status","idle");
    }
}
