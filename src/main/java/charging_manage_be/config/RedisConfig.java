package charging_manage_be.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Bean
    RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory("localhost", 6379);
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>(); // Khai báo RedisTemplate với kiểu key và value là String
        redisTemplate.setConnectionFactory(redisConnectionFactory); // Thiết lập connection factory
        redisTemplate.setKeySerializer(new StringRedisSerializer()); // Sử dụng StringRedisSerializer cho key với StringRedisSerializer có nghĩa là key sẽ được lưu dưới dạng chuỗi
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer()); // Sử dụng GenericJackson2JsonRedisSerializer cho value với GenericJackson2JsonRedisSerializer có nghĩa là value sẽ được lưu dưới dạng JSON
        return redisTemplate;
    }
}
