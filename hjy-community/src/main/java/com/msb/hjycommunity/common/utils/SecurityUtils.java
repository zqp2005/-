package com.msb.hjycommunity.common.utils;

import com.msb.hjycommunity.common.constant.HttpStatus;
import com.msb.hjycommunity.common.core.exception.CustomException;
import com.msb.hjycommunity.system.domain.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class SecurityUtils {

    public static String getUserName()
        {
            try {
                return getLoginUser().getUsername();
            } catch (Exception e) {
                throw new CustomException(HttpStatus.UNAUTHORIZED,"获取用户账户异常");
            }
        }
        public static LoginUser getLoginUser()
        {
            try {
                return (LoginUser) getAuhtentication().getPrincipal();
            } catch (Exception e) {
                throw new CustomException(HttpStatus.UNAUTHORIZED,"获取用户信息异常");
            }
        }
        public static Authentication getAuhtentication()
        {
            return SecurityContextHolder.getContext().getAuthentication();
        }
    /**
     * 生成 BCryptPasswordEncoder 密码
     * @param password
     * @return: java.lang.String
     */
    public static String encryptPassword(String password){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(password);
    }
}
