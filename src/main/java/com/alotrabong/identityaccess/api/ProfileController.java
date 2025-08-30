package com.alotrabong.identityaccess.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Profile")
@RestController
public class ProfileController {

  @Operation(summary = "Thông tin cá nhân hiện tại")
  @GetMapping("/u/profile")
  public Map<String,Object> me(){
    return Map.of("Vinh","Ngu vai ca dai");
  }

  @Operation(summary = "cập nhật thông tin cá nhân")
  @PutMapping("/u/profile")
  public Map<String, Object> updateProifile(@RequestBody Map<String, Object> requestBody){
    return requestBody;
  }
  
}
