本Demo用来展现客户端如何通过PKCE模式访问受oauth2保护的资源。

<h3>首先对概念做下介绍</h3>
  <br>
    什么是PKCE? <a href="https://tools.ietf.org/html/rfc7636" target="_blank">这里</a>有详细的介绍. PKCE是Proof Key for Code Exchange的缩写。简单来说就是获取Token过程中，利用两个数值比较来验证
请求者是否是最终用户，防止被攻击者盗用Token. PKCE提高了公有云环境下的单页面运用或APP获取Token的安全性，也能应用在私有云环境里。我们可利用它来实现SSO。 <br>
  具体步骤简单描述如下：<br>
  第一步：先随机生成一个32位长的code_verifier，然后运用hash算法s256加密得到一个code_challenge。js算法实现可参考<a href="https://tonyxu.io/zh/posts/2018/oauth2-pkce-flow/">这里</a><br>
  第二步：客户端向oauth2认证服务器申请认证码code,并带着code_challenge，oauth2服务器返回一个code,并将code_challenge缓存<br>
  第三步：客户端利用返回的code+code_verifier来向oauth2服务器申请token<br>
  第四步：客户端通过在header里加bearer token就能访问受限资源了<br>
 
<br><br>
<h3>接下来对demo使用做下介绍：</h3>
<br>
代码分两部分，authentication和html. authentication是oauth2认证和资源服务，利用spring security实现; html利用spring boot+thymleaf+vue来模拟一个客户端。<br>
* 准备工作：<br>
  需1.8（含）以上的JDK和Maven, 将Maven的命令mvn配到机器环境的path里,同时在环境里配置JAVA_HOME指向JDK所在目录<br> 

* 运行：<br>
  运行认证和资源服务<br>
  cd authentication <br>
  mvn -DskipTests clean package
  java -jar target/authentication-0.0.1-SNAPSHOT.jar
  <br><br>
  运行客户端<br>
  cd html <br>
  mvn -DskipTests clean package <br>
  java -jar target/ui-0.0.1-SNAPSHOT.jar <br>
  
* 执行
  authentication服务器端口是30000, 客户端端口是30010. 在chrome浏览器输入<font style="text-decoration:none">http://127.0.0.1:30010</font>, 即进入客户端。如下图所示：
<p>
  ![image](https://github.com/jacky-neo/pkce-demo/blob/master/doc/images/p1.png)
</p>

  <br>
  "非认证点击" 按钮演示未获token访问接口的效果，上面会有出错提示。<br>
  "认证点击" 按钮演示PKCE获取token后访问接口的效果，如成功，上面会有一段json信息。<br>
  <br>
  【备注】获取code需表单认证，用户名和密码为tom和sonia 这在authentication里配置
  <br>
  这两个按钮最终都会调用authentication服务里的接口，该接口受oauth2保护。接口定义如下：<br>
  <pre>
    // 在html的com.ghy.vo.authentication.controller.DemoController里
    @RequestMapping(value = "/res/showData")
    public Object showData2(){
        Map<String,String> map = new HashMap<String,String>();
        map.put("t1","this is test1");
        map.put("t2","this is test2");
        return map;
    }

    //这是在authentication的资源配置，在com.ghy.vo.authentication.config.OAuth2Server里将/res/*定义为受oauth2保护
    @Configuration
    @EnableResourceServer
    protected class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {
        ......

        @Override
        public void configure(HttpSecurity http) throws Exception {
            final String[] urlPattern = {"/res/**"};
            http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .requestMatchers().antMatchers(urlPattern)
                .and()
                .authorizeRequests()
                .antMatchers(urlPattern)
                .access("#oauth2.hasScope('read') and hasRole('ROLE_USER')")
            ;
        }

  </pre>

  下图是成功的显示
  <p>
    ![image](https://github.com/jacky-neo/pkce-demo/blob/master/doc/images/p2.png)
    <br>
    如图所示，标红的地方表明了获取code, token和调用接口的network过程
  </p>
 
<br>
<h3>接着讲解下实现过程：</h3>
<p>
  <h4>
Oauth2认证和资源配置
  </h4>
<br>
基于spring security实现的：
<ul>
  <li>
    认证服务：见OAuth2Server类<br>
    通过@EnableAuthorizationServer定义认证服务器，将oauth2认证模式"authorization_code"加载到内存里。如下
    <pre>
      @Override
      public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
          clients.inMemory()
                  .withClient("vo_client_id")
                  .secret("{noop}")
                  .redirectUris("http://127.0.0.1:30010")
                  .authorizedGrantTypes("authorization_code")
                  .scopes("read")
                  .autoApprove(true)
                  ;
      }
    </pre>

  </li>

  <li>
    除了默认的4种oauth2认证模式，还引入PKCE认证模式<br>
    先在OAuth2Server类里进行配置，引入相关服务和tokenGranter,如下
    <pre>
      @Override
      public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
          endpoints
                  .tokenStore(tokenStore)
                  .authorizationCodeServices(new PkceAuthorizationCodeServices(endpoints.getClientDetailsService(), passwordEncoder))
                  .tokenGranter(tokenGranter(endpoints));
      }  
    </pre>
  </li>

  <li>
    PKCE实现见pkce包里，<br>
    CodeChallengeMethod类用来codeVerifier和codeChallenge比较<br>
    PkceAuthorizationCodeServices类用于调度<br>
    PkceAuthorizationCodeTokenGranter类用于从request请求中获取token所需信息<br>
    PkceProtectedAuthentication类定义PKCE认证对象信息<br>
  </li>

  <br>
  <li>
    全局跨域访问+Token调用设置
    <br>
    见CrosFilter类<br>
    <pre>
      @Configuration
      @Order(5)
      public class CrosFilter implements Filter {
      
          @Override
          public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
              HttpServletRequest request = (HttpServletRequest) req;
              HttpServletResponse response = (HttpServletResponse) res;
      
              response.setHeader("Access-Control-Allow-Origin", "*");
              response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, OPTIONS");
              response.setHeader("Access-Control-Allow-Headers", "Accept, Origin, Content-Type, Authorization");
              response.setHeader("Access-Control-Max-Age", "3600");
              response.setHeader("Access-Control-Allow-Credentials", "true");
      
              String method = request.getMethod();
              if ("OPTIONS".equals(method)) {
                  response.setStatus(HttpStatus.OK.value());
              } else {
                  chain.doFilter(req, res);
              }
          }
      }
    </pre>
    
  </li>

  <li>
      <br>
    遇到的一些坑：<br>
    曾配过CorsConfig类和SecurityConfiguration类的corsConfigurationSource()方法，都没效果，
    特别是从client调用token时都不行。经过摸索，最终发现用filter方式是可行的。另外一种方式用
    spring拦截器模式也是可行的，demo里没有描述。我在下面参考列出来，供学习。
    <br>
    filter分两部分，第一部分是跨域设置和对Header等控制，第二部分是当请求是options时，则返回200。
    这是配合axios获取token用的。
  </li>
  
</ul>
</p>

<p>
  <h4>client端</h4>
<br>
   主要实现见test.html, 其中<br>
   * hash加密算法通过crypto-js.min.js来引入实现<br>
   * pkce.js用来生成code_vefivier和code_challenge<br>
   * 获取token,通过Qs实现options的简单调用<br>
   * "认证点击"按钮的调用流程如下图，主要用到了js的异步回调技术：<br><br>
  ![image](https://github.com/jacky-neo/pkce-demo/blob/master/doc/images/p3.png)
</p>


<br><br>
<h3>可改进的地方</h3>
<ul>
  <li>
    认证信息从缓存放DB或redis
  </li>
  <li>
    client端还要加logout等清理token等方法
  </li>

</ul>

<br><br>
<h3>参考</h3>
<ul>
  <li>
    <a href="https://www.jianshu.com/p/1ad4358beff7">跨域设置过滤器方案</a>
  </li>
  <li>
    <a href="https://sultanov.dev/blog/authorization-code-flow-with-pkce-in-spring-security-oauth/">服务器端Pkce代码参考实现</a>
  </li>

</ul>

