package com.qingyun.community.base.utils;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-05-05 09:21
 **/
public class RedisKeyUtils {
    private static final String SPLIT = ":";  // 分隔符
    private static final String PREFIX_KAPTCHA = "kaptcha";  // 验证码前缀
    private static final String PREFIX_ENTITY_LIKE = "like:entity";  // 点赞前缀
    private static final String PREFIX_USER_LIKE = "like:user";  // 用户收到赞的个数的前缀
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_UV = "uv";  // 访问量
    private static final String PREFIX_DAU = "dau";  // 活跃用户数量
    private static final String PREFIX_POST_SCORE = "post:score";  // 得分变化的帖子集合
    private static final String PREFIX_POST_INDEX = "post:index";  // 按热度排行的首页前缀
    private static final String PREFIX_POST_INDEX_LOCK = "post:index:lock";  // 热度排行首页的redis互斥锁
    private static final String PREFIX_POST_DETAIL = "post:detail";  // 帖子详情的前缀
    private static final String PREFIX_POST_DETAIL_LOCK = "post:detail:lock";  // 加载帖子详情到缓存时的互斥锁


    /**
     * 生成具体图片验证码的key
     * @param owner
     * @return
     */
    public static  String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    /**
     * 生成具体点赞的key
     * @param entityType
     * @param EntityId
     * @return
     */
    public static String getEntityLikeKey(int entityType, int EntityId){
        return PREFIX_ENTITY_LIKE+SPLIT+entityType+SPLIT+EntityId;
    }

    /**
     * 用户收到赞的个数
     * @param userId
     * @return
     */
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    // 某个用户关注的entity目标
    // followee:userId:entityType -> zset(entityId,now)
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    // 某个entity拥有的粉丝
    // follower:entityType:entityId -> zset(userId,now)
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    // 单日UV
    public static String getUVKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    // 区间UV
    public static String getUVKey(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    // 单日活跃用户
    public static String getDAUKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    // 区间活跃用户
    public static String getDAUKey(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    // 分数发生变化的帖子集合
    public static String getPostScoreKey() {
        return PREFIX_POST_SCORE;
    }

    // 按热度排行的首页帖子列表
    public static String getPostIndex(int current) {
        return PREFIX_POST_INDEX + SPLIT + current;
    }

    // 加载按热度排行的首页帖子列表到缓存中使用的分布式锁
    public static String getPostIndexLock(int current) {
        return PREFIX_POST_INDEX_LOCK + SPLIT + current;
    }

    // 某个帖子的详情
    public static String getPostDetail(int id) {
        return PREFIX_POST_DETAIL + SPLIT + id;
    }

    // 加载某个帖子详情到缓存时的互斥锁
    public static String getPostDetailLock(int id) {
        return PREFIX_POST_DETAIL_LOCK + SPLIT + id;
    }
}
