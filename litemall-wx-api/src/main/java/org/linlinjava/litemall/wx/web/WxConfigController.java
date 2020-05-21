package org.linlinjava.litemall.wx.web;

import org.linlinjava.litemall.core.system.SystemConfig;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/wx/config")
public class WxConfigController {
    @GetMapping("{key}")
    public Object getValue(@PathVariable("key") String key) {
        Map<Object, Object> data = new HashMap<Object, Object>();
        data.put(key, SystemConfig.getValue(key));
        return ResponseUtil.ok(data);
    }
}
