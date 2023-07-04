package com.atguigu.gmall.model.user;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description="登录对象")
public class LoginVo {

    @ApiModelProperty(value = "用户名称")
    private String loginName;

    @ApiModelProperty(value = "用户密码")
    private String passwd;

}