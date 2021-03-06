package cn.learn.springboot.listener;

import java.util.Arrays;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * SpringBoot启动配置.
 *
 * @author shaoyijiong
 * @date 2018/7/24
 */
@Component
public class HelloCommandLineRunner implements CommandLineRunner {

  @Override
  public void run(String... strings) {
    System.out.println("CommandLineRunner" + Arrays.asList(strings));
  }
}
