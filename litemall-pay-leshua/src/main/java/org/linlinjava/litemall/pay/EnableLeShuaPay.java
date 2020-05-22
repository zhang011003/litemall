package org.linlinjava.litemall.pay;

import org.linlinjava.litemall.pay.properties.LeShuaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({LeShuaConfig.class})
@EnableConfigurationProperties(LeShuaProperties.class)
public @interface EnableLeShuaPay {

}