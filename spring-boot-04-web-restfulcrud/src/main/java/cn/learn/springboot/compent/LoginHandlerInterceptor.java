package cn.learn.springboot.compent;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 登陆拦截器 检查登陆,要注册到web config中去.
 *
 * @author shaoyijiong
 * @since 2018/7/16
 */
public class LoginHandlerInterceptor implements HandlerInterceptor {

  /**
   * 执行方法前.
   */
  @Override
  public boolean preHandle(HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse, Object o) throws Exception {
    Object user = httpServletRequest.getSession().getAttribute("loginUser");
    if (user == null) {
      //未登陆返回登陆页面
      httpServletRequest.setAttribute("msg","没有权限请先登陆");
      httpServletRequest.getRequestDispatcher("login").forward(httpServletRequest, httpServletResponse);
      return false;
    } else {
      //已登录 放行请求
      return true;
    }
  }

  /**
   * 执行方法后.
   */
  @Override
  public void postHandle(HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) {

  }

  @Override
  public void afterCompletion(HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

  }
}
