package com.wl.xc.modelfun.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wl.xc.modelfun.entities.po.TaskInfoPO;
import com.wl.xc.modelfun.entities.vo.TaskInfoVO;
import com.wl.xc.modelfun.mapper.TaskInfoMapper;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @date 2022/4/11 12:55
 */
@Service
public class TaskInfoService extends ServiceImpl<TaskInfoMapper, TaskInfoPO> {

  public List<TaskInfoVO> getTaskInfoListByNameAndDes(TaskInfoPO taskInfoPO) {
    return baseMapper.getTaskInfoListByNameAndDes(taskInfoPO);
  }

  public boolean logicDelete(Long taskId, String updatePeople) {
    return baseMapper.logicDelete(taskId, updatePeople) > 0;
  }

  public TaskInfoPO getTaskTemplateByType(int taskType) {
    return baseMapper.getTaskTemplateByType(taskType);
  }

}



