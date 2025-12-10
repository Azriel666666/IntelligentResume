package app.intelligent.resume.security;

import app.intelligent.resume.entity.User;
import app.intelligent.resume.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 用户详情服务实现类
 *
 * @author Intelligent Resume Team
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 根据用户名查询用户
        User user = userRepository.selectByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 根据用户类型设置角色
        String role = getUserRole(user.getUserType());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)))
                .accountExpired(false)
                .accountLocked(user.getStatus() == 0)
                .credentialsExpired(false)
                .disabled(user.getStatus() == 0)
                .build();
    }

    /**
     * 根据用户类型获取角色
     */
    private String getUserRole(Integer userType) {
        return switch (userType) {
            case 1 -> "ADMIN";
            case 2 -> "HR";
            case 3 -> "SEEKER";
            default -> "SEEKER";
        };
    }
}
