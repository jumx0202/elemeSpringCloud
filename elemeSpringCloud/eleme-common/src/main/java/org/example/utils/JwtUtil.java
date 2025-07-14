package org.example.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.example.constants.CommonConstants;

import java.util.Date;

/**
 * JWT工具类
 */
public class JwtUtil {

    /**
     * JWT密钥
     */
    private static final String SECRET = "eleme-springcloud-secret-key";

    /**
     * 生成JWT Token
     *
     * @param userId 用户ID
     * @param userType 用户类型
     * @return JWT Token
     */
    public static String generateToken(String userId, String userType) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + CommonConstants.TOKEN_EXPIRE * 1000);
        
        return JWT.create()
                .withIssuer("eleme-springcloud")
                .withSubject(userId)
                .withClaim("userType", userType)
                .withIssuedAt(now)
                .withExpiresAt(expireDate)
                .sign(Algorithm.HMAC256(SECRET));
    }

    /**
     * 生成用户Token
     *
     * @param phoneNumber 用户手机号
     * @return JWT Token
     */
    public static String generateUserToken(String phoneNumber) {
        return generateToken(phoneNumber, "user");
    }

    /**
     * 生成商家Token
     *
     * @param businessId 商家ID
     * @return JWT Token
     */
    public static String generateBusinessToken(String businessId) {
        return generateToken(businessId, "business");
    }

    /**
     * 验证JWT Token
     *
     * @param token JWT Token
     * @return 是否有效
     */
    public static boolean verifyToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("eleme-springcloud")
                    .build();
            verifier.verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取Token中的用户ID
     *
     * @param token JWT Token
     * @return 用户ID
     */
    public static String getUserId(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getSubject();
        } catch (JWTDecodeException e) {
            return null;
        }
    }

    /**
     * 获取Token中的用户类型
     *
     * @param token JWT Token
     * @return 用户类型
     */
    public static String getUserType(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("userType").asString();
        } catch (JWTDecodeException e) {
            return null;
        }
    }

    /**
     * 获取Token的过期时间
     *
     * @param token JWT Token
     * @return 过期时间
     */
    public static Date getExpirationDate(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getExpiresAt();
        } catch (JWTDecodeException e) {
            return null;
        }
    }

    /**
     * 判断Token是否过期
     *
     * @param token JWT Token
     * @return 是否过期
     */
    public static boolean isTokenExpired(String token) {
        Date expiration = getExpirationDate(token);
        return expiration != null && expiration.before(new Date());
    }
} 