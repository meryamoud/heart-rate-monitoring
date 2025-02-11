package devxplorers.heart_rate_monitor.User;

import org.springframework.beans.factory.annotation.Autowired;

public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User getUserByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }
}