package com.example.payment_transfer_service.security;

import com.example.payment_transfer_service.entity.User;
import com.example.payment_transfer_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrId) throws UsernameNotFoundException {
        User user;

        user = userRepository.findById(usernameOrId).orElse(null);

        if (user == null) {
            user = userRepository.findByUsername(usernameOrId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + usernameOrId));
        }

        log.debug("Loading user details for: {}", user.getUsername());
        return UserPrincipal.create(user);
    }
}
