package cn.learn.springboot.spring.produce;

import cn.learn.springboot.spring.domain.OrderPaidEvent;
import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Resource;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

/**
 * CommandLineRunner
 *
 * @author shaoyijiong
 * @date 2019/3/25
 */
@Service
public class ProduceRun implements CommandLineRunner {

  private static final String TX_PGROUP_NAME = "myTxProducerGroup";
  @Resource
  private RocketMQTemplate rocketMQTemplate;
  @Value("${demo.rocketmq.transTopic}")
  private String springTransTopic;
  @Value("${demo.rocketmq.topic}")
  private String springTopic;
  @Value("${demo.rocketmq.orderTopic}")
  private String orderPaidTopic;
  @Value("${demo.rocketmq.msgExtTopic}")
  private String msgExtTopic;


  @Override
  public void run(String... args) {
    // 发送普通的同步消息
    SendResult sendResult = rocketMQTemplate.syncSend(springTopic, "Hello, World!");
    System.out.printf("syncSend1 to topic %s sendResult=%s %n", springTopic, sendResult);

    // 通过messageBuilder发送消息
    sendResult = rocketMQTemplate.syncSend(springTopic,
        MessageBuilder.withPayload("Hello, World! I'm from spring message").build());
    System.out.printf("syncSend2 to topic %s sendResult=%s %n", springTopic, sendResult);
    // 发送异步消息 对象的方式
    rocketMQTemplate.asyncSend(orderPaidTopic, new OrderPaidEvent("T_001", new BigDecimal("88.00")),
        new SendCallback() {
          @Override
          public void onSuccess(SendResult var1) {
            System.out.printf("async onSucess SendResult=%s %n", var1);
          }

          @Override
          public void onException(Throwable var1) {
            System.out.printf("async onException Throwable=%s %n", var1);
          }

        });

    //相同的hashkey发送到同一个队列中去 顺序发送
    rocketMQTemplate.syncSendOrderly(springTopic, "你好","01");
    // 指定tag发送
    rocketMQTemplate.convertAndSend(msgExtTopic + ":tag0", "I'm from tag0");
    System.out.printf("syncSend topic %s tag %s %n", msgExtTopic, "tag0");
    rocketMQTemplate.convertAndSend(msgExtTopic + ":tag1", "I'm from tag1");
    System.out.printf("syncSend topic %s tag %s %n", msgExtTopic, "tag1");

    // 发送事务消息
    testTransaction();
  }


  private void testTransaction() throws MessagingException {
    String[] tags = new String[]{"TagA", "TagB", "TagC", "TagD", "TagE"};
    for (int i = 0; i < 10; i++) {
      try {

        Message msg = MessageBuilder.withPayload("Hello RocketMQ " + i)
            .setHeader(RocketMQHeaders.KEYS, "KEY_" + i).build();
        SendResult sendResult = rocketMQTemplate.sendMessageInTransaction(TX_PGROUP_NAME,
            springTransTopic + ":" + tags[i % tags.length], msg, null);
        System.out.printf("------ send Transactional msg body = %s , sendResult=%s %n",
            msg.getPayload(), sendResult.getSendStatus());

        Thread.sleep(10);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  @RocketMQTransactionListener(txProducerGroup = TX_PGROUP_NAME)
  class TransactionListenerImpl implements RocketMQLocalTransactionListener {

    private AtomicInteger transactionIndex = new AtomicInteger(0);

    private ConcurrentHashMap<String, Integer> localTrans = new ConcurrentHashMap<>();

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
      String transId = (String) msg.getHeaders().get(RocketMQHeaders.TRANSACTION_ID);
      System.out.printf("#### executeLocalTransaction is executed, msgTransactionId=%s %n",
          transId);
      int value = transactionIndex.getAndIncrement();
      int status = value % 3;
      localTrans.put(transId, status);
      if (status == 0) {
        // Return local transaction with success(commit), in this case,
        // this message will not be checked in checkLocalTransaction()
        System.out.printf(
            "    # COMMIT # Simulating msg %s related local transaction exec succeeded! ### %n",
            msg.getPayload());
        return RocketMQLocalTransactionState.COMMIT;
      }

      if (status == 1) {
        // Return local transaction with failure(rollback) , in this case,
        // this message will not be checked in checkLocalTransaction()
        System.out
            .printf("    # ROLLBACK # Simulating %s related local transaction exec failed! %n",
                msg.getPayload());
        return RocketMQLocalTransactionState.ROLLBACK;
      }

      System.out.printf("    # UNKNOW # Simulating %s related local transaction exec UNKNOWN! \n");
      return RocketMQLocalTransactionState.UNKNOWN;
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
      String transId = (String) msg.getHeaders().get(RocketMQHeaders.TRANSACTION_ID);
      RocketMQLocalTransactionState retState = RocketMQLocalTransactionState.COMMIT;
      Integer status = localTrans.get(transId);
      if (null != status) {
        switch (status) {
          case 0:
            retState = RocketMQLocalTransactionState.UNKNOWN;
            break;
          case 1:
            retState = RocketMQLocalTransactionState.COMMIT;
            break;
          case 2:
            retState = RocketMQLocalTransactionState.ROLLBACK;
            break;
          default:

        }
      }
      System.out.printf("------ !!! checkLocalTransaction is executed once,"
              + " msgTransactionId=%s, TransactionState=%s status=%s %n",
          transId, retState, status);
      return retState;
    }
  }

}
