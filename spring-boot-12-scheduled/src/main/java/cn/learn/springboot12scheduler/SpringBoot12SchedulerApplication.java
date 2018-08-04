package cn.learn.springboot12scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 开启定时任务.
 * @author shaoyijiong
 */
@EnableScheduling
@SpringBootApplication
public class SpringBoot12SchedulerApplication {

  public static void main(String[] args) {
    SpringApplication.run(SpringBoot12SchedulerApplication.class, args);
  }
}
