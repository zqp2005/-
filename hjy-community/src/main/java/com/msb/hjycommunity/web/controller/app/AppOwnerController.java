package com.msb.hjycommunity.web.controller.app;

import com.msb.hjycommunity.common.core.controller.BaseController;
import com.msb.hjycommunity.common.core.domain.BaseResponse;
import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.framework.security.service.AppOwnerTokenService;
import com.msb.hjycommunity.property.domain.HjyOwner;
import com.msb.hjycommunity.property.domain.dto.AppLoginRequest;
import com.msb.hjycommunity.property.domain.dto.AppRegisterRequest;
import com.msb.hjycommunity.property.service.HjyOwnerService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 业主端（小程序）注册/登录 Controller
 * <p>
 * 注册支持三条路径：新手机号注册、存量业主（密码为空）激活、已注册手机号拒绝。
 * 登录成功签发独立于管理端的业主令牌（JWT + Redis owner_tokens:{uuid}，7天有效）。
 *
 * @author hjy
 * @date 2026/9/11
 **/
@RestController
@RequestMapping("/app")
public class AppOwnerController extends BaseController {

    /** 大陆 11 位手机号 */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /** 18 位身份证号（末位数字或 X，大小写均允许） */
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^\\d{17}[\\dXx]$");

    @Resource
    private HjyOwnerService ownerService;

    @Resource
    private AppOwnerTokenService appOwnerTokenService;

    /**
     * 业主注册（新手机号）或存量业主激活（密码为空的已导入业主）
     */
    @PostMapping("/register")
    public BaseResponse register(@RequestBody AppRegisterRequest body) {
        String phone = trimToNull(body.getPhone());
        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            return BaseResponse.fail("手机号格式不正确");
        }
        String password = body.getPassword();
        if (password == null || password.trim().isEmpty() || password.trim().length() < 6) {
            return BaseResponse.fail("密码不能为空且长度不能少于6位");
        }
        String realName = trimToNull(body.getRealName());
        if (realName == null) {
            return BaseResponse.fail("姓名不能为空");
        }
        String idCard = trimToNull(body.getIdCard());
        if (idCard != null && !ID_CARD_PATTERN.matcher(idCard).matches()) {
            return BaseResponse.fail("身份证号格式不正确");
        }

        HjyOwner exist = ownerService.selectOwnerByPhone(phone);
        if (exist != null && exist.getOwnerPassword() != null && !exist.getOwnerPassword().trim().isEmpty()) {
            return BaseResponse.fail("该手机号已注册，请直接登录");
        }

        String encrypted = SecurityUtils.encryptPassword(password);
        if (exist != null) {
            // 存量业主激活：补密码；姓名为空时补填
            HjyOwner update = new HjyOwner();
            update.setOwnerId(exist.getOwnerId());
            update.setOwnerPassword(encrypted);
            if (trimToNull(exist.getOwnerRealName()) == null) {
                update.setOwnerRealName(realName);
            }
            ownerService.updateOwner(update);
            return BaseResponse.success(buildOwnerData(exist.getOwnerId(), realName));
        }

        HjyOwner owner = new HjyOwner();
        owner.setOwnerPhoneNumber(phone);
        owner.setOwnerRealName(realName);
        owner.setOwnerIdCard(idCard);
        owner.setOwnerPassword(encrypted);
        ownerService.insertOwner(owner);
        return BaseResponse.success(buildOwnerData(owner.getOwnerId(), realName));
    }

    /**
     * 业主登录（手机号+密码），返回业主令牌
     */
    @PostMapping("/login")
    public BaseResponse login(@RequestBody AppLoginRequest body) {
        String phone = trimToNull(body.getPhone());
        String password = body.getPassword();
        if (phone == null || password == null || password.trim().isEmpty()) {
            return BaseResponse.fail("手机号和密码不能为空");
        }

        HjyOwner owner = ownerService.selectOwnerByPhone(phone);
        if (owner == null || owner.getOwnerPassword() == null || owner.getOwnerPassword().trim().isEmpty()
                || !new BCryptPasswordEncoder().matches(password, owner.getOwnerPassword())) {
            return BaseResponse.fail("手机号或密码错误");
        }

        String token = appOwnerTokenService.createToken(owner);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("ownerId", String.valueOf(owner.getOwnerId()));
        data.put("realName", owner.getOwnerRealName());
        return BaseResponse.success(data);
    }

    /** ownerId 为雪花ID，序列化为字符串避免小程序端 JS 数值精度丢失 */
    private Map<String, Object> buildOwnerData(Long ownerId, String realName) {
        Map<String, Object> data = new HashMap<>();
        data.put("ownerId", String.valueOf(ownerId));
        data.put("realName", realName);
        return data;
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
