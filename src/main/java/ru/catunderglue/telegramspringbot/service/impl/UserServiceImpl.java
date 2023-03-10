package ru.catunderglue.telegramspringbot.service.impl;

import org.springframework.stereotype.Service;
import ru.catunderglue.telegramspringbot.model.User;
import ru.catunderglue.telegramspringbot.repository.UserRepository;
import ru.catunderglue.telegramspringbot.service.UserService;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void create(User user) {
        userRepository.save(user);
    }

    @Override
    public List<User> readAll() {
        return userRepository.findAll();
    }

    @Override
    public User read(long id) {
        return userRepository.getOne((int) id);
    }

    @Override
    public boolean isContains(int userId){
        return userRepository.existsById(userId);
    }
}
