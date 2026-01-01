package app.intelligent.resume.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 分页信息
 *
 * @author Intelligent Resume Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Long pages;

    /**
     * 当前页码
     */
    private Long current;

    /**
     * 每页数量
     */
    private Long size;
}
