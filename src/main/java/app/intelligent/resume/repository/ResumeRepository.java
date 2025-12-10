package app.intelligent.resume.repository;

import app.intelligent.resume.entity.Resume;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 简历Repository接口
 *
 * @author Intelligent Resume Team
 */
@Mapper
public interface ResumeRepository extends BaseMapper<Resume> {

    /**
     * 根据用户ID查询简历列表
     */
    @Select("SELECT * FROM resume WHERE user_id = #{userId} AND deleted = 0 ORDER BY create_time DESC")
    List<Resume> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户的默认简历
     */
    @Select("SELECT * FROM resume WHERE user_id = #{userId} AND is_default = 1 AND deleted = 0 LIMIT 1")
    Resume selectDefaultByUserId(@Param("userId") Long userId);
}
