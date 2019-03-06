package cn.learn.springboot;

import cn.learn.springboot.bean.Game;
import cn.learn.springboot.service.GameService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.Data;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisTests {

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    GameService gameService;

    /**
     * Redis常见的五种数据类型 String(字符串),List(列表),Set(集合),Hash(散列),ZSet(有序集合) 通过以下方式来操作各个类型
     * stringRedisTemplate.opsForValue() stringRedisTemplate.opsForList()
     * stringRedisTemplate.opsForSet() stringRedisTemplate.opsForHash()
     * stringRedisTemplate.opsForZSet()
     */

    /**
     * 测试StringRedisTemplate
     */
    @Test
    public void test01() {
        //stringRedisTemplate 相当于 RedisTemplate<String,String>的一种特殊情况 使用:相当于分组的作用
        stringRedisTemplate.opsForValue().set("NAME:01", "李四");

        //与StringBuilder的append的作用类似
        stringRedisTemplate.opsForValue().append("NAME:01", "张三");

        //这里设置redis的失效时间为3分钟
        stringRedisTemplate.opsForValue().set("NAME:02", "王五", 3, TimeUnit.MINUTES);

        //没有队友的 key 才放进去 set是替换
        boolean exist = stringRedisTemplate.opsForValue().setIfAbsent("NAME:02", "王五");

        //通过map放入多个键值对
        Map<String, String> map = new HashMap<>();
        map.put("NAME:01", "李四");
        map.put("NAME:02", "王五");
        stringRedisTemplate.opsForValue().multiSet(map);

        //拿数据
        String value = stringRedisTemplate.opsForValue().get("NAME:01");
        System.out.println(value);

        //批量拿数据
        List<String> keys = new ArrayList<>();
        keys.add("NAME:01");
        keys.add("NAME:02");
        List<String> values = stringRedisTemplate.opsForValue().multiGet(keys);
        redisTemplate.opsForSet().add("aaa", 10);
        redisTemplate.opsForSet().add("bbb:" + 10, 10);
        System.out.println(value);

        //设置过期时间 10天
        redisTemplate.expire("NAME:01", 10, TimeUnit.DAYS);

    }

    /**
     * 测试保存对象
     */
    @Test
    public void test02() {
        Game game = new Game(1, "王者荣耀", 88.9, new Date(), 9.6, "a");
        //保存对象实现了序列化,序列化的值保存到redis 默认通过jdk序列化方式(字节)
        //redisTemplate.opsForValue().set("game01", game);
        //以json的方式保存到redis
        //1.将数据转换为json 再保存
        //2.修改redis的默认序列化规则
        redisTemplate.opsForValue().set("GAME:1", game);

        JSONObject o;
        if ((o = (JSONObject) redisTemplate.opsForValue().get("GAME:1")) != null) {
            game = o.toJavaObject(Game.class);
        }
        System.out.println(game);
    }

    /**
     * 测试数组
     */
    @Test
    public void test03() {

        // start 表示从0开始  end 偏移量 也可以为负数 -1表示最后一个元素  这样写就是列表的所有元素
        redisTemplate.opsForList().range("LIST:01", 0, -1);
        //修剪现有列表，使其只包含指定的指定范围的元素，起始和停止都是基于0的索引 相当于去掉第一个元素
        redisTemplate.opsForList().trim("LIST:01", 1, -1);

        //list 长度
        redisTemplate.opsForList().size("LIST:01");

        //将指定值插入表的头部
        //redisTemplate.opsForList().leftPush("LIST:01", "java");

        //所有插入
        List<String> listToSet = new ArrayList<>(Arrays.asList("java", "c++", "ios"));
        //redisTemplate.opsForList().leftPushAll("LIST:01", listToSet);

        //插入某个元素左边
        //redisTemplate.opsForList().leftPush("LIST:01", "java", "oc");

        //删除元素 例子为删除第一次出现的java
        //count> 0：删除等于从头到尾移动的值的元素。
        //count <0：删除等于从尾到头移动的值的元素。
        //count = 0：删除等于value的所有元素。
        redisTemplate.opsForList().remove("LIST:01", 1, "java");

        //获得序号为1的元素 index 从0开始
        Object value = redisTemplate.opsForList().index("LIST:01", 1);

        //leftPop 弹出最左边的元素，弹出之后该值在列表中将不复存在

        //与left对应right 插入表的尾部
    }

    /**
     * 测试Hash
     */
    @Test
    public void test04() {
        //删除
        redisTemplate.opsForHash().delete("HASH:01", "key01");

        //取
        redisTemplate.opsForHash().get("HASH:01", "key01");

        //增加
        redisTemplate.opsForHash().put("HASH:01", "key01", "a");

        Boolean a = redisTemplate.opsForSet().isMember("a", 1);
        redisTemplate.opsForSet().add("a", "a");
    }

    /**
     * 测试Spring自带的CacheAble
     */
    @Test
    public void test05() {
        System.out.println(gameService.selectById(2));
    }

    @Data
    static class  A {

        String fromId;
        Date expireDate;

        @Override
        public String toString() {
            return JSON.toJSONString(this);
        }
    }

    @Test
    public void redisTest() {
        String key = "weixin:form:aa";
        //stringRedisTemplate.opsForValue().set(a, "das");
        //7天后过期
        A a1 = new A();
        a1.setFromId("1");
        a1.setExpireDate(DateUtils.addDays(new Date(), 7));

        //已经过期
        A a2 = new A();
        a2.setFromId("2");
        a2.setExpireDate(DateUtils.addDays(new Date(), -7));

        //6天后过期
        A a3 = new A();
        a3.setFromId("3");
        a3.setExpireDate(DateUtils.addDays(new Date(), 6));

        //已经过期
        A a4 = new A();
        a4.setFromId("4");
        a4.setExpireDate(DateUtils.addDays(new Date(), -9));

        //3天后过期
        A a5 = new A();
        a5.setFromId("5");
        a5.setExpireDate(DateUtils.addDays(new Date(), 3));

        stringRedisTemplate.opsForSet()
            .add(key, JSON.toJSONString(a1), JSON.toJSONString(a2), JSON.toJSONString(a3),
                JSON.toJSONString(a4), JSON.toJSONString(a5));

        stringRedisTemplate.expireAt(key, DateUtils.addDays(new Date(), 7));
    }

    @Test
    public void redisTest2() {
        Date date = new Date();
        String key = "weixin:form:aa";
        Set<String> members = stringRedisTemplate.opsForSet().members(key);
        List<A> collect = members.stream().map(m -> JSON.parseObject(m,A.class))
            .filter(n -> n.getExpireDate().compareTo(date) > 0).sorted(
                Comparator.comparingLong(m -> m.getExpireDate().getTime()))
            .collect(Collectors.toList());


        String[] values = collect.stream().skip(1).map(JSON::toJSONString)
            .toArray(String[]::new);
        stringRedisTemplate.delete(key);
        stringRedisTemplate.opsForSet().add(key, values);
        System.out.println(collect);


    }

}
