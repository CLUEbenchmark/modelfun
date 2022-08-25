package com.wl.xc.modelfun.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wl.xc.modelfun.entities.po.SysUserPO;
import com.wl.xc.modelfun.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @date 2022/4/11 12:55
 */
@Service
public class SysUserService extends ServiceImpl<SysUserMapper, SysUserPO> {

  /**
   * 根据用户手机号查询用户信息，手机号是唯一索引，所以只会返回一条记录
   *
   * @param userPhone 用户手机号
   * @return 用户信息
   */
  public SysUserPO findUserByPhone(String userPhone) {
    return this.baseMapper.selectOne(Wrappers.<SysUserPO>query().eq(SysUserPO.COL_USER_PHONE, userPhone));
  }
}



