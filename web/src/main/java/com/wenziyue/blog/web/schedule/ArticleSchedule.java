package com.wenziyue.blog.web.schedule;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wenziyue.blog.biz.service.BizArticleService;
import com.wenziyue.blog.dal.entity.ArticleEntity;
import com.wenziyue.blog.dal.service.ArticleService;
import com.wenziyue.redis.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import static com.wenziyue.blog.common.constants.RedisConstant.ARTICLE_VERSION_KEY;


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
            if (redisUtils.get(ARTICLE_VERSION_KEY + articleEntity.getId()) != null) {
                log.info("文章{}已加入mq中，无须再次加入", articleEntity.getId());
                continue;
            }
            log.info("定时任务提交前 pool={} queue={}",
                    ((ThreadPoolExecutor) executorService).getActiveCount(),
                    ((ThreadPoolExecutor) executorService).getQueue().size());
            executorService.submit(() -> bizArticleService.sendSummaryMq(articleEntity.getTitle(), articleEntity.getContent(), articleEntity.getUserId(), articleEntity.getId(), articleEntity.getVersion()));
            log.info("定时任务提交后 pool={} queue={}",
                    ((ThreadPoolExecutor) executorService).getActiveCount(),
                    ((ThreadPoolExecutor) executorService).getQueue().size());
        }
    }

}
