package com.thr.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thr.usercenter.model.domain.User;
import com.thr.usercenter.model.domain.request.UserLoginRequest;
import com.thr.usercenter.model.domain.request.UserRegisterRequest;
import com.thr.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
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
    public Long userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            return null;
        }

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            return null;
        }

        return userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
    }

    /**
     * 登录接口
     *
     * @param userLoginRequest 用户登录请求对象
     * @param request          请求
     * @return 登录结果
     */
    @PostMapping("/login")
    public User userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            return null;
        }

        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }

        return userService.userLogin(userAccount, userPassword, request);
    }

    /**
     * 用户注销
     * @param request 请求对象
     * @return 注销结果标识
     */
    @PostMapping("/logout")
    public Integer userLogout(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        return userService.userLogout(request);
    }

    /**
     * 获取当前登录的用户信息
     *
     * @param request 请求
     * @return 用户对象
     */
    @GetMapping("/current")
    public User getCurrentUser(HttpServletRequest request) {
        // 从 session 中拿到用户的登录态, 返回用户信息
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            return null;
        }

        // 对于信息频繁变换的系统(如积分), 可以通过再去查一次数据库后返回用户信息(推荐)
        long userId = currentUser.getId();
        // todo 校验用户是否合法(封号?)
        User user = userService.getById(userId);
        return userService.getSafetyUser(user);  // 脱敏
    }


    /**
     * 根据用户名查询用户(仅管理员可查询)
     *
     * @param username 用户名
     * @param request  请求
     * @return 返回查询结果列表
     */
    @GetMapping("/search")
    public List<User> searchUsers(String username, HttpServletRequest request) {
        // 如果不是管理员
        if (!isAdmin(request)) {
            return new ArrayList<>();
        }

        // 如果 username 为空, QueryWrapper 不包含任何条件时
        // 相当于执行了一个不带 WHERE 子句的 SELECT 查询, 它会检索目标表中的所有行
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNoneBlank(username)) {
            queryWrapper.like("username", username);
        }

        List<User> userList = userService.list(queryWrapper);
        return userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
    }

    /**
     * 删除用户(仅管理员可删除)
     *
     * @param id      用户 id
     * @param request 请求
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    public boolean deleteUsers(@RequestBody long id, HttpServletRequest request) {
        // 如果不是管理员
        if (!isAdmin(request)) {
            return false;
        }

        if (id <= 0) {
            return false;
        }

        return userService.removeById(id);
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
