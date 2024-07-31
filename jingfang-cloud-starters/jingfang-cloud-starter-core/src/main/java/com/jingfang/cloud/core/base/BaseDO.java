package com.jingfang.cloud.core.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * BaseEntity
 *
 * @author Jin
 */
@Getter
@Setter
public abstract class BaseDO implements Serializable {

    private static final long serialVersionUID = -2694074995776393995L;

    @TableField(fill = FieldFill.INSERT)
    private Serializable createUser;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.UPDATE)
    private Serializable updateUser;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @JsonIgnore
    @TableLogic(value = "0",delval = "1")
    private Boolean deletedFlag;

}
