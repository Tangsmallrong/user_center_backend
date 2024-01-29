package com.thr.usercenter;

import com.thr.usercenter.mapper.UserMapper;
import com.thr.usercenter.model.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SampleTest {

    @Resource
    private UserMapper userMapper;

    @Test
    public void testSelect() {
        System.out.println(("----- selectAll method test ------"));
        // 查询所有
        List<User> userList = userMapper.selectList(null);
        // 断言=我觉得, 如果不是这样就报错
        // 这里判断查出来的是否有五条数据
        Assert.assertEquals(5, userList.size());
//        Assertions.assertEquals(5, userList.size());
        userList.forEach(System.out::println);
    }
}