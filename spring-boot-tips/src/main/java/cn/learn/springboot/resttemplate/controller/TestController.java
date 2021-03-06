package cn.learn.springboot.resttemplate.controller;

import cn.learn.springboot.resttemplate.Response;
import com.alibaba.fastjson.JSONObject;
import javax.annotation.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * 测试controller.
 *
 * @author shaoyijiong
 * @date 2018/7/26
 */
@RestController
public class TestController {

  @Resource
  private RestTemplate restTemplate;

  private static final String RESPONSE_JSON = "{'code':200,'message':'成功','data':"
      + "{'name':'小明','age':18}}";

  /**
   * 测试. 无json参数,无路径参数
   *
   * @return json格式的数据
   */
  @GetMapping("/get")
  public Response hello() {
    return JSONObject.parseObject(RESPONSE_JSON, Response.class);
  }

  /**
   * 测试. 带json参数,无路径参数
   *
   * @param json json格式的数据
   * @return json格式的数据
   */
  @GetMapping("/get/json")
  public Response hello(@RequestBody String json) {
    JSONObject jsonObject = JSONObject.parseObject(json);
    System.out.println(jsonObject);
    return JSONObject.parseObject(RESPONSE_JSON, Response.class);
  }

  /**
   * 测试. 带json参数,带路径参数
   *
   * @param json json格式的数据
   * @return json格式的数据
   */
  @PostMapping("/get/{id}")
  public Response hello(@PathVariable Integer id, @RequestBody String json) {
    System.out.println(id);
    JSONObject jsonObject = JSONObject.parseObject(json);
    System.out.println(jsonObject);
    return JSONObject.parseObject(RESPONSE_JSON, Response.class);
  }

  /**
   * 测试.
   *
   * @param id 根据id区分
   * @return 返回数据
   */
  @GetMapping("/go/{id}")
  public Response go(@PathVariable Integer id) {
    //请求的所有信息
    ResponseEntity<Response> response0;
    //只包含请求体信息
    Response response1;
    String param = "{'a':'a'}";
    switch (id) {
      case (1):
        //发送一个HTTP GET请求 返回的ResponseEntity包含了响应体所映射成的对象
        response0 = restTemplate.getForEntity("http://localhost:8080/get", Response.class);
        return response0.getBody();
      case (2):
        //发送一个HTTP POST请求，返回的请求体将映射为一个对象,这里的{}相当于url参数,就是id
        //http://localhost:8080/get/1
        response1 = restTemplate
            .postForObject("http://localhost:8080/get/{id}", param, Response.class, id);
        return response1;
      case (3):
        //这个参数param是url参数 json 请求
        response1 = restTemplate.getForObject("http://localhost:8080/get", Response.class, param);
        return response1;
      case (4):
        //这种请求  http://localhost:8080/get?a=a
        response1 = restTemplate
            .getForObject("http://localhost:8080/get?a={a}", Response.class, "param");
        return response1;
      case (5):
        //封装请求参数
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        //封装请求头
        HttpHeaders headers = new HttpHeaders();
        //将请求头与请求参数合并为一个请求体
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        response0 = restTemplate.postForEntity("", request, Response.class);
        return response0.getBody();
      default:
        response0 = restTemplate.getForEntity("http://localhost:8080/get", Response.class);
        return response0.getBody();

      //注意了  提交表单请求 要用HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
    }
  }
}
