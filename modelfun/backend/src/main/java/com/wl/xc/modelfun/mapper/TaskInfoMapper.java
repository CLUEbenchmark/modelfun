package com.wl.xc.modelfun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wl.xc.modelfun.entities.po.TaskInfoPO;
import com.wl.xc.modelfun.entities.vo.TaskInfoVO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * @version 1.0
 * @date 2022/4/11 16:13
 */
public interface TaskInfoMapper extends BaseMapper<TaskInfoPO> {

  List<TaskInfoVO> getTaskInfoListByNameAndDes(TaskInfoPO taskInfoPO);

  int logicDelete(@Param("taskId") Long taskId, @Param("updatePeople") String updatePeople);

  TaskInfoPO getTaskTemplateByType(int taskType);
}