package com.example.ewalllet.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RedisTemplate<String,User> redisTemplate;

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    public String saveUser(UserRequestDto userRequestDto){
        try {

            User user = User.builder().name(userRequestDto.getName()).username(userRequestDto.getUsername()).email(userRequestDto.getEmail())
                    .mobNo(userRequestDto.getMobNo()).age(userRequestDto.getAge()).build();

              userRepository.save(user);
              saveInCache(user);

              kafkaTemplate.send("create_wallet", user.getUsername());

             return "User created successfully! ";
        }
        catch (Exception e){
            return "Can't create User";
        }
    }

    public void saveInCache(User user){
        Map map = objectMapper.convertValue(user,Map.class);

        String key = "UserKey_"+user.getUsername();

        redisTemplate.opsForHash().putAll(key,map);
        redisTemplate.expire(key, Duration.ofHours(12));
    }


    public UserResponseDto findByUserName(String username){

        // first find in cache
        String key = "UserKey_"+username;
        Map map = redisTemplate.opsForHash().entries(key);

        User user = new User();

        if(map==null || map.size()==0){

            // get from db
            user = userRepository.findByUsername(username);
            saveInCache(user);

        }
        else{
            user = objectMapper.convertValue(map,User.class);
        }

        // convert to userResponse object

        UserResponseDto userResponseDto = UserResponseDto.builder().username(user.getUsername()).name(user.getName()).email(user.getEmail()).mobNo(user.getMobNo()).build();

        return userResponseDto;
    }
}
