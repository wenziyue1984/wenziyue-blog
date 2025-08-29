package com.wenziyue.blog.biz.mq.listener;

import com.wenziyue.blog.common.utils.BlogUtils;
import com.wenziyue.blog.dal.dto.ArticlePvDTO;
import com.wenziyue.blog.dal.entity.UserEntity;
import com.wenziyue.redis.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.wenziyue.blog.common.constants.RedisConstant.*;
import static com.wenziyue.blog.common.constants.RocketMqTopic.ArticlePvTopic;

/**
 * @author wenziyue
 */
@Slf4j
@RequiredArgsConstructor
@Component
@RocketMQMessageListener(topic = ArticlePvTopic, consumerGroup = "article-pv-consumer-group",
        consumeMode = ConsumeMode.CONCURRENTLY, // 消费模式，默认并发模式，消费顺序消息使用顺序模式
        maxReconsumeTimes = 2, consumeThreadNumber = 1, // 设置线程数为1
        enableMsgTrace = true)
public class ArticlePvListener implements RocketMQListener<ArticlePvDTO> {

    private final RedisUtils redisUtils;
    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<Long> countPV;
    private static final long BF_CAPACITY = 1000000;
    private static final double BF_ERROR_RATE = 0.01;
    private static final long BF_THRESHOLD = 900000;

    @Override
    public void onMessage(ArticlePvDTO dto) {
        log.info("pv:{}", dto);
        if (dto == null || dto.getArticleId() == null || dto.getArticleId().isEmpty()) {
            return;
        }
        String item ;
        if (dto.getToken() != null && !dto.getToken().isEmpty()) {
            val userEntity = redisUtils.get(LOGIN_TOKEN_KEY + dto.getToken(), UserEntity.class);
            if (userEntity == null) {
                // token失效
                item = BlogUtils.fp(dto.getIp(), dto.getUserAgent());
            } else {
                item = BlogUtils.fp(userEntity.getId().toString());
            }
        } else {
            item = BlogUtils.fp(dto.getIp(), dto.getUserAgent());
        }
//        countPvByJava(dto.getArticleId(), item);
        countPvByLua(dto.getArticleId(), item);
    }

    /**
     * 最初的处理逻辑，后来使用Lua脚本处理，这个就用不到了，但是保留一下，记录下思路
     */
    private void countPvByJava(String articleId, String item) {
        // 先获取该文章有几个布隆过滤器
        String setKey = ARTICLE_PV_BLOOM_NUM_SET_KEY + articleId;
        Set<Object> filterNumberSet;
        if (!redisUtils.hasKey(setKey)) {
            // 如果没有，则创建
            createNewBFAndIncrement(articleId, item, setKey, 1);
            return;
        } else {
            filterNumberSet = redisUtils.sMembers(setKey);
        }
        // 过滤器序号排序
        List<Integer> filterNumberList = new ArrayList<>();
        filterNumberSet.forEach(it -> filterNumberList.add(Integer.parseInt(it.toString())));
        filterNumberList.sort(Integer::compareTo);

        for (int i = 0; i < filterNumberList.size(); i++) {
            int num = filterNumberList.get(i);
            String bfKey = ARTICLE_PV_BLOOM_KEY + articleId + "_" + num;
            // 过滤器是否已过期，如果已过期则删除setkey中的该序号
            if (!redisUtils.hasKey(bfKey)) {
                redisUtils.sRemove(setKey, num);
                // 如果是最后一个过滤器也过期，则创建新的过滤器
                if (i == filterNumberList.size() - 1) {
                    createNewBFAndIncrement(articleId, item, setKey, num+1);
                }
                continue;
            }
            if (redisUtils.bfExists(bfKey, item)) {
                return;
            }
            // bfKey 存在，并且 item 不存在，并且是最后一个过滤器
            if (i == filterNumberList.size() - 1) {
                val capacity = redisUtils.bfCard(bfKey);
                // 超过阈值，则创建新的布隆过滤器
                if (capacity > BF_THRESHOLD) {
                    createNewBFAndIncrement(articleId, item, setKey, num+1);
                    return;
                }
                // 没超过阈值在这个过滤器上添加
                redisUtils.bfAdd(bfKey, item);
                redisUtils.increment(ARTICLE_PV_COUNT_KEY + articleId, 1L);
            }

        }
    }

    private void createNewBFAndIncrement(String articleId, String item, String setKey, int newNum) {
        redisUtils.sAddAndExpire(setKey, 1, TimeUnit.DAYS, newNum);
        String newBfKey = ARTICLE_PV_BLOOM_KEY + articleId + "_" + newNum;
        redisUtils.bfReserve(newBfKey,BF_ERROR_RATE, BF_CAPACITY, 1, TimeUnit.DAYS);
        redisUtils.bfAdd(newBfKey, item);
        redisUtils.increment(ARTICLE_PV_COUNT_KEY + articleId, 1L);
    }

    /**
     * countPvByJava的Lua版本
     */
    private boolean countPvByLua(String articleId, String fp) {
        List<String> keys = Arrays.asList(
                ARTICLE_PV_BLOOM_NUM_SET_KEY + articleId,
                ARTICLE_PV_BLOOM_KEY         + articleId + "_",
                ARTICLE_PV_COUNT_KEY         + articleId
        );
        Long added = stringRedisTemplate.execute(
                countPV,
                keys,
                fp,
                String.valueOf(BF_CAPACITY),
                String.valueOf(BF_ERROR_RATE),
                String.valueOf(BF_THRESHOLD),
                String.valueOf(TimeUnit.DAYS.toSeconds(1))
        );
        return added != null && added == 1L;
    }

}
