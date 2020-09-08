本Demo用来展现客户端如何通过PKCE模式访问受oauth2保护的资源。

首先对概念做下介绍<br>
    什么是PKCE? <a href="https://tools.ietf.org/html/rfc7636" target="_blank">这里</a>有详细的介绍. PKCE是Proof Key for Code Exchange的缩写。简单来说就是获取Token过程中，利用两个数值比较来验证
请求者是否是最终用户，防止被攻击者盗用Token. PKCE提高了公有云环境下的单页面运用或APP获取Token的安全性，也能应用在私有云环境里。我们可利用它来实现SSO。 <br>
  具体步骤简单描述如下：<br>
  第一步：先随机生成一个32位长的code_verifier，然后运用hash算法s256加密得到一个code_challenge。js算法实现可参考<a href="https://tonyxu.io/zh/posts/2018/oauth2-pkce-flow/">这里</a><br>
  第二步：客户端向oauth2认证服务器申请认证码code,并带着code_challenge，oauth2服务器返回一个code,并将code_challenge缓存<br>
  第三步：客户端利用返回的code+code_verifier来向oauth2服务器申请token<br>
  第四步：客户端通过在header里加bearer token就能访问受限资源了<br>
 
<br><br>
接下来对demo使用做下介绍：<br>
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
  authentication服务器端口是30000, 客户端端口是30010. 在chrome浏览器输入http://127.0.0.1:30010,即进入客户端

 

接着讲解下实现过程：
* Oauth2认证
* 资源配置
* client端
