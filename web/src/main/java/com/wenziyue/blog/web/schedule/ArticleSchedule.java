package com.wenziyue.blog.web.schedule;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wenziyue.blog.biz.service.BizArticleService;
import com.wenziyue.blog.common.constants.RedisConstant;
import com.wenziyue.blog.dal.entity.ArticleEntity;
import com.wenziyue.blog.dal.service.ArticleService;
import com.wenziyue.redis.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import static com.wenziyue.blog.common.constants.RedisConstant.*;


/**
 * @author wenziyue
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleSchedule {

    private final ArticleService articleService;
    private final ExecutorService executorService;
    private final BizArticleService bizArticleService;
    private final RedisUtils redisUtils;

    /**
     * 生成摘要和slug
     */
    @Scheduled(cron = "0 0 * * * ?")
    void generateSummaryAndSlug() {
        log.info("开始生成摘要和slug");
        val articleEntityList = articleService.list(Wrappers.<ArticleEntity>lambdaQuery()
                .or().isNull(ArticleEntity::getSummary).isNull(ArticleEntity::getSlug));
        if (articleEntityList.isEmpty()) {
            log.info("没有文章需要生成摘要和slug");
            return;
        }
        for (ArticleEntity articleEntity : articleEntityList) {
            if (redisUtils.get(ARTICLE_UPDATE_TIME_KEY + articleEntity.getId()) != null) {
                log.info("文章{}已加入mq中，无须再次加入", articleEntity.getId());
                continue;
            }
            log.info("定时任务提交前 pool={} queue={}",
                    ((ThreadPoolExecutor) executorService).getActiveCount(),
                    ((ThreadPoolExecutor) executorService).getQueue().size());
            executorService.submit(() -> bizArticleService.sendSummaryMq(articleEntity.getTitle(), articleEntity.getContent(), articleEntity.getUserId(), articleEntity.getId(), articleEntity.getUpdateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            log.info("定时任务提交后 pool={} queue={}",
                    ((ThreadPoolExecutor) executorService).getActiveCount(),
                    ((ThreadPoolExecutor) executorService).getQueue().size());
        }
    }

    /**
     * 同步文章点赞数
     * 每天凌晨3点同步一次即可
     */
    @Scheduled(cron = "0 0 3 * * ?")
    void syncArticleLikeCount() {
        log.info("开始同步文章点赞数和浏览量");
        List<ArticleEntity> articleEntityList;
        int pageSize = 500;
        int offset = 0;
        do {
            articleEntityList = articleService.list(Wrappers.<ArticleEntity>lambdaQuery()
                    .select(ArticleEntity::getId, ArticleEntity::getLikeCount, ArticleEntity::getViewCount)
                    .orderByAsc(ArticleEntity::getId)
                    .last("limit " + offset + "," + pageSize));
            offset += pageSize;
            for (ArticleEntity articleEntity : articleEntityList) {
                try {
                    val likeCount = redisUtils.get(RedisConstant.ARTICLE_LIKE_COUNT_KEY + articleEntity.getId(), Integer.class);
                    val pvCount = redisUtils.get(RedisConstant.ARTICLE_PV_COUNT_KEY + articleEntity.getId(), Integer.class);
                    if ((likeCount != null && !likeCount.equals(articleEntity.getLikeCount())) || (pvCount != null && !pvCount.equals(articleEntity.getViewCount()))) {
                        articleService.update(Wrappers.<ArticleEntity>lambdaUpdate()
                                .eq(ArticleEntity::getId, articleEntity.getId())
                                .set(ArticleEntity::getLikeCount, likeCount)
                                .set(ArticleEntity::getViewCount, pvCount));
                    }
                } catch (Exception e) {
                    log.error("同步文章点赞数和浏览量失败，文章id:{}", articleEntity.getId(), e);
                }
            }
        } while (!articleEntityList.isEmpty());
    }


}
