package org.linlinjava.litemall.admin.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.common.collect.Lists;
import org.linlinjava.litemall.db.domain.LitemallAdmin;

import java.util.*;
import java.util.stream.Collectors;

public class JwtHelper {
	// 秘钥
	static final String SECRET = "X-Litemall-Admin-Token";
	// 签名是有谁生成
	static final String ISSUSER = "LITEMALL";
	// 签名的主题
	static final String SUBJECT = "this is litemall token";
	// 签名的观众
	static final String AUDIENCE = "MINIAPP";
	
	
	public static String createToken(LitemallAdmin litemallAdmin){
		try {
			List<String> roleIds = Arrays.stream(litemallAdmin.getRoleIds()).map(String::valueOf).collect(Collectors.toList());
			Algorithm algorithm = Algorithm.HMAC256(SECRET);
		    Map<String, Object> map = new HashMap<String, Object>();
		    Date nowDate = new Date();
		    // 过期时间：2小时
		    Date expireDate = getAfterDate(nowDate,0,0,0,2,0,0);
//		    Date expireDate = getAfterDate(nowDate,0,0,0,0,0,10);
	        map.put("alg", "HS256");
	        map.put("typ", "JWT");
		    String token = JWT.create()
		    	// 设置头部信息 Header
		    	.withHeader(map)
		    	// 设置 载荷 Payload
//				.withClaim("userId", litemallAdmin.getId())
		    	.withClaim("userName", litemallAdmin.getUsername())
//				.withClaim("roleIds", String.join(",", roleIds))
		        .withIssuer(ISSUSER)
		        .withSubject(SUBJECT)
		        .withAudience(AUDIENCE)
		        // 生成签名的时间 
		        .withIssuedAt(nowDate)
		        // 签名过期的时间 
		        .withExpiresAt(expireDate)
		        // 签名 Signature
		        .sign(algorithm);
		    return token;
		} catch (JWTCreationException exception){
			exception.printStackTrace();
		}
		return null;
	}

//	public static Integer getUserId(String token) {
//		return verifyTokenAndGetUserInfo(token, "userId", Integer.class);
//	}

	public static <T> T verifyTokenAndGetUserInfo(String token, String key, Class<T> clazz) {
		try {
		    Algorithm algorithm = Algorithm.HMAC256(SECRET);
		    JWTVerifier verifier = JWT.require(algorithm)
		        .withIssuer(ISSUSER)
		        .build();
		    DecodedJWT jwt = verifier.verify(token);
		    Map<String, Claim> claims = jwt.getClaims();
		    Claim claim = claims.get(key);
		    return claim.as(clazz);
		} catch (JWTVerificationException exception){
//			exception.printStackTrace();
		}
		
		return null;
	}
	
	private static Date getAfterDate(Date date, int year, int month, int day, int hour, int minute, int second){
		if(date == null){
			date = new Date();
		}
		
		Calendar cal = new GregorianCalendar();
		
		cal.setTime(date);
		if(year != 0){
			cal.add(Calendar.YEAR, year);
		}
		if(month != 0){
			cal.add(Calendar.MONTH, month);
		}
		if(day != 0){
			cal.add(Calendar.DATE, day);
		}
		if(hour != 0){
			cal.add(Calendar.HOUR_OF_DAY, hour);
		}
		if(minute != 0){
			cal.add(Calendar.MINUTE, minute);
		}
		if(second != 0){
			cal.add(Calendar.SECOND, second);
		}
		return cal.getTime();
	}
	
}
