package com.bankapp.bankingapp.infrastructure.security;

import com.bankapp.bankingapp.application.interfaces.repository.IUserRepository;
import com.bankapp.bankingapp.domain.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final IUserRepository userRepository;

    public UserDetailsServiceImpl(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Tìm user bằng username hoặc email
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // enabled = isActive() → PENDING/LOCKED/DISABLED user sẽ bị
        // AuthenticationManager từ chối
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                user.isActive(), // enabled
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                getAuthorities(user));
    }

    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());
    }
}
