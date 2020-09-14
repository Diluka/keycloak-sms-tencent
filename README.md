keycloak 短信验证

2020-03-25
默认手机号当验证码 手机号使用 attr 中的 phone 可以修改用户名了

---

## 新增
注册时，
1. 增加手机号码非空验证（2020-07-27）
2. 增加手机号已注册验证
3. 重写用户名邮件登录模块，增加手机号作为用户名登录的场景
4. 重写用户名邮件登录模块，增加手机号作为用户名登录的场景

# 使用说明
# keycloak使用和功能扩展

## 手机验证码登录集成

1. 下载代码`git@github.com:zhangzhenling/keycloak-sms-tencent.git`

   ```sh
   $ git clone git@github.com:zhangzhenling/keycloak-sms-tencent.git
   ```

2. 执行打包命令：`mvn install`

3. 将`target`文件夹中的`keycloak-sms-auth-spi-10.0.2-jar-with-dependencies.jar`拷贝到keycloak项目的`providers`文件夹中

4. 将主题样式文件`themes`合并到`keycloak`的`themes`目录中

5. 在keycloak Admin Console管理控制台页面，选择相应域下的`Authentication`菜单

6. 创建flows

   1. 登录流程，复制Browser，然后修改成如下图所示：

      ![image-20200731165958184](keycloak.assets/image-20200731165958184.png)

   2. 注册流程，复制Registration，修改为如下图所示：

      ![image-20200731170101049](keycloak.assets/image-20200731170101049.png)

   3. 重置密码流程，复制Reset Credentials，修改如下图所示：

      ![image-20200731171848566](keycloak.assets/image-20200731171848566.png)

7. 在Bindings中进行操作绑定

   ![image-20200731172020264](keycloak.assets/image-20200731172020264.png)


## 2020-09-14
v1.0+ 版本需要搭配 修改keycloak源码 自行编译打包后的系统，才能使用！！！（修改keycloak源码，将phoneNumber加入User_Entity表中）