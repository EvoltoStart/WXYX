package com.wx.YX.client.user;

import com.wx.YX.vo.user.LeaderAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("service-user")
public interface UserFeignClient {

    @GetMapping("/api/user/leader/inner/getUserAddressByUserId/{userId}")
    public LeaderAddressVo getUserAddressByUserId(@PathVariable(value = "userId") Long userId);
}
