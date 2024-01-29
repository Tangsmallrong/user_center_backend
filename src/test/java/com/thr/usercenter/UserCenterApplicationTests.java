package com.thr.usercenter;

import com.thr.usercenter.mapper.UserMapper;
import com.thr.usercenter.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
class UserCenterApplicationTests {
    @Resource
    private UserMapper userMapper;

    @Test
    void contextLoads() {
        System.out.println(("----- selectAll method test ------"));
        // 查询所有
        List<User> userList = userMapper.selectList(null);
        // 断言=我觉得, 如果不是这样就报错
        // 这里判断查出来的是否有五条数据
        Assertions.assertEquals(5, userList.size());
        userList.forEach(System.out::println);
    }

}
