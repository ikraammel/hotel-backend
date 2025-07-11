package com.ikram.hotel.security.jwt;

import com.ikram.hotel.security.user.HotelUserDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
    @Value("${hotel.app.jwtSecret}")
    private String jwtSecret;

    @Value("${hotel.app.jwtExpirationMs}")
    private int jwtExpirationTime;

    //  Crée un token JWT
    public String generateJwtTokenForUser(Authentication authentication){
        HotelUserDetails userPrincipal = (HotelUserDetails) authentication.getPrincipal(); //	Récupère l’utilisateur connecté (UserDetails)
        List<String> roles = userPrincipal.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority).toList();
        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .claim("roles",roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime()+jwtExpirationTime))
                .signWith(key(), SignatureAlgorithm.HS256).compact();
    }

    //  Convertit la clé secrète en Key utilisable
    private Key key(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
    //  Extrait l'email depuis un Token
    public String getUsernameFromToken(String token){
        return Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token).getBody().getSubject();
    }
    public boolean validateToken(String token){
        try{
            Jwts.parserBuilder().setSigningKey(key()).build().parse(token);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid jwt token : {}",e.getMessage());
        } catch (ExpiredJwtException e){
            logger.error("Expired token : {}",e.getMessage());
        } catch (UnsupportedJwtException e){
            logger.error("This token is not supported : {} ",e.getMessage());
        } catch (IllegalArgumentException e){
            logger.error("No claims found : {}",e.getMessage());
        }
        return false;
    }
}
