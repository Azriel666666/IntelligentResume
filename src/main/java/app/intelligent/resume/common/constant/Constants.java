package app.intelligent.resume.common.constant;

/**
 * 系统常量类
 *
 * @author Intelligent Resume Team
 */
public class Constants {

    /**
     * JWT Token Header
     */
    public static final String TOKEN_HEADER = "Authorization";

    /**
     * JWT Token 前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 用户类型
     */
    public static class UserType {
        /**
         * 管理员
         */
        public static final Integer ADMIN = 1;

        /**
         * HR
         */
        public static final Integer HR = 2;

        /**
         * 求职者
         */
        public static final Integer JOB_SEEKER = 3;
    }

    /**
     * 用户状态
     */
    public static class UserStatus {
        /**
         * 禁用
         */
        public static final Integer DISABLED = 0;

        /**
         * 正常
         */
        public static final Integer NORMAL = 1;

        /**
         * 待审核
         */
        public static final Integer PENDING = 2;
    }

    /**
     * 简历解析状态
     */
    public static class ResumeParseStatus {
        /**
         * 未解析
         */
        public static final Integer NOT_PARSED = 0;

        /**
         * 解析中
         */
        public static final Integer PARSING = 1;

        /**
         * 解析成功
         */
        public static final Integer SUCCESS = 2;

        /**
         * 解析失败
         */
        public static final Integer FAILED = 3;
    }

    /**
     * 岗位状态
     */
    public static class JobStatus {
        /**
         * 已下架
         */
        public static final Integer OFFLINE = 0;

        /**
         * 招聘中
         */
        public static final Integer RECRUITING = 1;

        /**
         * 已暂停
         */
        public static final Integer PAUSED = 2;

        /**
         * 待审核
         */
        public static final Integer PENDING = 3;
    }

    /**
     * 投递状态
     * 对应需求文档4.8.4
     */
    public static class ApplicationStatus {
        /**
         * 待查看
         */
        public static final Integer PENDING = 0;

        /**
         * 已查看
         */
        public static final Integer VIEWED = 1;

        /**
         * 通过筛选
         */
        public static final Integer PASSED = 2;

        /**
         * 不合适
         */
        public static final Integer REJECTED = 3;

        /**
         * 已发offer
         */
        public static final Integer OFFERED = 4;
    }

    /**
     * 逻辑删除
     */
    public static class Deleted {
        /**
         * 未删除
         */
        public static final Integer NOT_DELETED = 0;

        /**
         * 已删除
         */
        public static final Integer DELETED = 1;
    }

    /**
     * 通用状态
     */
    public static class Status {
        /**
         * 禁用
         */
        public static final Integer DISABLED = 0;

        /**
         * 启用
         */
        public static final Integer ENABLED = 1;
    }

    /**
     * 性别
     */
    public static class Gender {
        /**
         * 女
         */
        public static final Integer FEMALE = 0;

        /**
         * 男
         */
        public static final Integer MALE = 1;

        /**
         * 保密
         */
        public static final Integer SECRET = 2;
    }
}
