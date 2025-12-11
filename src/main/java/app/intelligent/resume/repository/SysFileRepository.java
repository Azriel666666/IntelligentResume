package app.intelligent.resume.repository;

import app.intelligent.resume.entity.SysFile;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统文件Repository
 *
 * @author Intelligent Resume Team
 */
@Mapper
public interface SysFileRepository extends BaseMapper<SysFile> {
}