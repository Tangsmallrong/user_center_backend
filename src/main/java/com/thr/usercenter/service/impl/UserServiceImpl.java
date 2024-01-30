package com.thr.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.thr.usercenter.service.UserService;
import com.thr.usercenter.model.domain.User;
import com.thr.usercenter.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author thr
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2024-01-30 13:58:05
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

}




