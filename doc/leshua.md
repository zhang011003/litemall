# 启用乐刷支付

## 1 配置文件

在application-core.yml中增加如下配置

```yaml
litemall:
  leshua:
    url: https://paygate.leshuazf.com/   #乐刷url
    merchant_id: 4014116132              #乐刷商户id
    key: EE085A0ABA9D22493B7971B2E1AEB4DF #调用乐刷接口对数据加密用到的key
    notify-key: A79CE8B83C9BAA666353B7CA80F4616B #乐刷通知接口对数据解密用到的key
    pay-notify-url: http://20b75fd6.ngrok.io/wx/order/pay-notify-leshua #支付完成后的通知地址
    refund-notify-url: http://20b75fd6.ngrok.io/admin/order/refund-notify-leshua #退款完成后的通知地址
```

##2 pom文件

pom.xml中引用

```xml
<dependency>
    <groupId>org.linlinjava</groupId>
   <artifactId>litemall-pay-leshua</artifactId>
</dependency>
```

##3 代码
    
在启动类中增加注解`@EnableLeShuaPay`
