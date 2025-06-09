-- 密码可为空（兼容第三方登录）
alter table `TB_WZY_BLOG_USER`
    modify `password` VARCHAR(255) DEFAULT NULL COMMENT '密码（加密后的）';