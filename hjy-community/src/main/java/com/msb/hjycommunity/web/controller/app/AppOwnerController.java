package com.msb.hjycommunity.web.controller.app;

import com.msb.hjycommunity.common.core.controller.BaseController;
import com.msb.hjycommunity.common.core.domain.BaseResponse;
import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.framework.security.service.AppOwnerTokenService;
import com.msb.hjycommunity.property.domain.HjyOwner;
import com.msb.hjycommunity.property.domain.dto.AppLoginRequest;
import com.msb.hjycommunity.property.domain.dto.AppRegisterRequest;
import com.msb.hjycommunity.property.service.HjyOwnerService;
import com.msb.hjycommunity.property.service.OwnerActivationService;
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
 * 新手机号可注册；存量档案禁止通过公开注册认领，须走可信身份核验流程。
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
    private OwnerActivationService ownerActivationService;

    @Resource
    private AppOwnerTokenService appOwnerTokenService;

    /**
     * 新手机号注册；可信激活流程上线前禁止认领存量业主档案。
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

        if (exist != null) {
            if (trimToNull(body.getActivationCode()) == null) {
                return BaseResponse.fail("该手机号已有居民档案，请联系物业核验并获取激活码");
            }
            if (!realName.equals(trimToNull(exist.getOwnerRealName()))) {
                return BaseResponse.fail("档案信息或激活码不正确，请联系物业核验");
            }
            boolean activated = ownerActivationService.activate(exist, body.getActivationCode(),
                    SecurityUtils.encryptPassword(password));
            return activated ? BaseResponse.success(buildOwnerData(exist.getOwnerId(), exist.getOwnerRealName()))
                    : BaseResponse.fail("激活码错误或已过期，请联系物业重新获取");
        }

        if (trimToNull(body.getActivationCode()) != null) {
            return BaseResponse.fail("未找到待激活档案，请核对手机号或联系物业");
        }
        String encrypted = SecurityUtils.encryptPassword(password);
        HjyOwner owner = new HjyOwner();
        owner.setOwnerPhoneNumber(phone);
        owner.setOwnerRealName(realName);
        owner.setOwnerIdCard(idCard);
        owner.setOwnerPassword(encrypted);
        // 数据库该列无默认值；注册账号可登录，但不代表通过房屋身份核验。
        owner.setOwnerStatus("Enable");
        // 性别/年龄（选填，白名单校验后写入）
        String gender = trimToNull(body.getOwnerGender());
        if ("Male".equals(gender) || "Female".equals(gender)) {
            owner.setOwnerGender(gender);
        }
        Integer age = body.getOwnerAge();
        if (age != null && age > 0 && age < 150) {
            owner.setOwnerAge(age);
        }
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
        if (owner == null || !"Enable".equals(owner.getOwnerStatus())
                || owner.getOwnerPassword() == null || owner.getOwnerPassword().trim().isEmpty()
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
