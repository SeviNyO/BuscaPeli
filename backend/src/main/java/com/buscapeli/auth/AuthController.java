package com.buscapeli.auth;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.web.csrf.CsrfToken;
import java.util.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwords;
    public AuthController(JdbcTemplate jdbc,PasswordEncoder passwords){this.jdbc=jdbc;this.passwords=passwords;}
    public record Credentials(@Email @NotBlank String email,@NotBlank String password){}
    public record Registration(@Email @NotBlank String email,@NotBlank String name,@NotBlank @Size(min=8) String password){}

    @GetMapping("/csrf") public Map<String,String> csrf(CsrfToken token){return Map.of("token",token.getToken());}

    @PostMapping("/register") public Map<String,Object> register(@Valid @RequestBody Registration input,HttpSession session,HttpServletRequest request){
        UUID id=UUID.randomUUID();
        try { jdbc.update("INSERT INTO app_users(user_id,email,display_name,password_hash) VALUES (?,?,?,?)",id,input.email().toLowerCase(Locale.ROOT),input.name().trim(),passwords.encode(input.password())); }
        catch(DuplicateKeyException e){throw new ResponseStatusException(HttpStatus.CONFLICT,"Ese correo ya está registrado");}
        request.changeSessionId();
        session.setAttribute("userId",id.toString()); session.setAttribute("displayName",input.name().trim());
        return Map.of("id",id,"email",input.email().toLowerCase(Locale.ROOT),"name",input.name().trim());
    }
    @PostMapping("/login") public Map<String,Object> login(@Valid @RequestBody Credentials input,HttpSession session,HttpServletRequest request){
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT user_id,email,display_name,password_hash FROM app_users WHERE email=?",input.email().toLowerCase(Locale.ROOT));
        if(rows.isEmpty()||!passwords.matches(input.password(),(String)rows.get(0).get("password_hash"))) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Correo o contraseña incorrectos");
        Map<String,Object> user=rows.get(0); request.changeSessionId(); session.setAttribute("userId",user.get("user_id").toString()); session.setAttribute("displayName",user.get("display_name"));
        return Map.of("id",user.get("user_id"),"email",user.get("email"),"name",user.get("display_name"));
    }
    @PostMapping("/logout") public Map<String,String> logout(HttpSession session){session.invalidate();return Map.of("status","ok");}
    @GetMapping("/me") public Map<String,Object> me(HttpSession session){
        if(session.getAttribute("userId")==null) return Map.of("authenticated",false);
        String id=session.getAttribute("userId").toString();
        List<Map<String,Object>> users=jdbc.queryForList("SELECT email,display_name FROM app_users WHERE user_id=?",UUID.fromString(id));
        if(users.isEmpty()) { session.invalidate(); return Map.of("authenticated",false); }
        return Map.of("authenticated",true,"id",id,"name",users.get(0).get("display_name"),"email",users.get(0).get("email"));
    }
}
