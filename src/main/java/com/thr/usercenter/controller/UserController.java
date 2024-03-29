package com.thr.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thr.usercenter.common.BaseResponse;
import com.thr.usercenter.common.ErrorCode;
import com.thr.usercenter.common.ResultUtils;
import com.thr.usercenter.exception.BusinessException;
import com.thr.usercenter.model.domain.User;
import com.thr.usercenter.model.domain.request.UserLoginRequest;
import com.thr.usercenter.model.domain.request.UserRegisterRequest;
import com.thr.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.thr.usercenter.constant.UserConstant.ADMIN_ROLE;
import static com.thr.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户控制器
 *
 * @author thr
 */
@RestController
@RequestMapping("/user")
//@CrossOrigin(origins = {"http://user.tang0103.com"}, allowCredentials = "true")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 注册接口
     *
     * @param userRegisterRequest 用户注册请求对象
     * @return 注册结果
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
//            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        return ResultUtils.success(result);
    }

    /**
     * 登录接口
     *
     * @param userLoginRequest 用户登录请求对象
     * @param request          请求
     * @return 登录结果
     */
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    /**
     * 用户注销
     *
     * @param request 请求对象
     * @return 注销结果标识
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录的用户信息
     *
     * @param request 请求
     * @return 用户对象
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        // 从 session 中拿到用户的登录态, 返回用户信息
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }

        // 对于信息频繁变换的系统(如积分), 可以通过再去查一次数据库后返回用户信息(推荐)
        long userId = currentUser.getId();
        // todo 校验用户是否合法(封号?)
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);  // 脱敏
        return ResultUtils.success(safetyUser);
    }


    /**
     * 根据用户名查询用户(仅管理员可查询)
     *
     * @param username 用户名
     * @param request  请求
     * @return 返回查询结果列表
     */
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        // 如果不是管理员
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        // 如果 username 为空, QueryWrapper 不包含任何条件时
        // 相当于执行了一个不带 WHERE 子句的 SELECT 查询, 它会检索目标表中的所有行
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNoneBlank(username)) {
            queryWrapper.like("username", username);
        }

        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    /**
     * 删除用户(仅管理员可删除)
     *
     * @param id      用户 id
     * @param request 请求
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUsers(@RequestBody long id, HttpServletRequest request) {
        // 如果不是管理员
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 是否为管理员
     *
     * @param request 请求
     * @return 是否为管理员
     */
    private boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询和删除
        // 1. 从 session 中拿到用户的登录态, 返回用户信息
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;

        // 2. 如果不是管理员则返回 false
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }
}