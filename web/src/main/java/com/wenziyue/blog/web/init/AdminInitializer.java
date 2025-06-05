package com.wenziyue.blog.web.init;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wenziyue.blog.common.enums.UserRoleEnum;
import com.wenziyue.blog.common.enums.UserStatusEnum;
import com.wenziyue.blog.dal.entity.UserEntity;
import com.wenziyue.blog.dal.mapper.UserMapper;
import com.wenziyue.uid.common.Status;
import com.wenziyue.uid.core.IdGen;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 初始化管理员账号
 *
 * @author wenziyue
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final UserMapper userMapper;
    private final IdGen idGen;
    private final PasswordEncoder passwordEncoder;

    @Value("${blog.init-admin:true}")
    private boolean initAdminEnabled;

    @Value("${blog.default-admin-name}")
    private String defaultName;

    @Value("${blog.default-admin-password}")
    private String defaultPassword;


    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (!initAdminEnabled) {
            log.info("⏭ 跳过管理员账号初始化");
            return;
        }

        long count = userMapper.selectCount(
                new LambdaQueryWrapper<UserEntity>()
                        .eq(UserEntity::getRole, 1)
                        .eq(UserEntity::getDeleted, 0)
        );

        if (count == 0) {
            UserEntity admin = new UserEntity();
            val idResult = idGen.nextId();
            log.info("获取的id:{}", idResult);
            if (!idResult.getStatus().equals(Status.EXCEPTION)) {
                admin.setId(10000000000000001L);
            } else {
                admin.setId(idResult.getId());
            }

//            admin.setId(100000000000000001L);
            admin.setName(defaultName);
            admin.setPassword(passwordEncoder.encode(defaultPassword));
            admin.setBio("管理员");
            admin.setRole(UserRoleEnum.ADMIN);   // 管理员
            admin.setStatus(UserStatusEnum.ENABLED); // 正常
            userMapper.insert(admin);
            log.info("✅ 管理员账号已初始化：用户名 [{}]，默认密码 [{}]", defaultName, defaultPassword);
        } else {
            log.info("✅ 管理员账号已存在，无需初始化");
        }
    }
}