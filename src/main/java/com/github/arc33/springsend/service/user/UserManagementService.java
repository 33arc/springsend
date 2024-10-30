package com.github.arc33.springsend.service.user;


import com.github.arc33.springsend.dto.user.UserRegisterRequest;
import com.github.arc33.springsend.model.User;
import org.springframework.data.domain.Page;

public interface UserManagementService {
    void registerUser(UserRegisterRequest request);
    void activateUser(String username, String confirmationCode);
    void resendConfirmationCode(String username);
    void adminConfirmSignUp(String username);
    String getRole(String username);
    void deleteUser(String username);
    Page<User> listUsers(int page, int size);
    Page<User> searchUsers(String username, int page, int size);
}