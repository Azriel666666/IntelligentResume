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
 * 支持通过手机号加载用户信息
 *
 * @author Intelligent Resume Team
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 通过手机号加载用户信息
     * @param phone 手机号（参数名为username是因为Spring Security接口定义，实际传入手机号）
     */
    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        // 根据手机号查询用户
        User user = userRepository.selectByPhone(phone);

        if (user == null) {
            throw new UsernameNotFoundException("该手机号未注册: " + phone);
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new UsernameNotFoundException("该账号已被禁用");
        }

        // 根据用户类型设置角色
        String role = getUserRole(user.getUserType());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getPhone()) // 使用手机号作为用户名
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
