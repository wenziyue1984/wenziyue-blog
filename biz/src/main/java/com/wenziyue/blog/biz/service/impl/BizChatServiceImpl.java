package com.wenziyue.blog.biz.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wenziyue.blog.biz.security.AuthHelper;
import com.wenziyue.blog.biz.service.BizChatService;
import com.wenziyue.blog.common.enums.ChatSessionMuteEnum;
import com.wenziyue.blog.common.exception.BlogResultCode;
import com.wenziyue.blog.common.utils.BlogUtils;
import com.wenziyue.blog.dal.dto.*;
import com.wenziyue.blog.dal.entity.ChatRecordEntity;
import com.wenziyue.blog.dal.entity.ChatSessionEntity;
import com.wenziyue.blog.dal.entity.ChatSessionStatusEntity;
import com.wenziyue.blog.dal.entity.UserEntity;
import com.wenziyue.blog.dal.service.ChatRecordService;
import com.wenziyue.blog.dal.service.ChatSessionService;
import com.wenziyue.blog.dal.service.ChatSessionStatusService;
import com.wenziyue.blog.dal.dto.SessionUnreadDTO;
import com.wenziyue.framework.common.CommonCode;
import com.wenziyue.framework.exception.ApiException;
import com.wenziyue.mybatisplus.page.PageResult;
import com.wenziyue.redis.lock.DistLockFactory;
import com.wenziyue.redis.utils.RedisUtils;
import com.wenziyue.uid.core.IdGen;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.security.Principal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.wenziyue.blog.common.constants.RedisConstant.CHAT_SESSION_MSG_LAST_TIME_KEY;
import static com.wenziyue.blog.common.constants.RocketMqTopic.ChatRecordsSaveTopic;

/**
 * @author wenziyue
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BizChatServiceImpl implements BizChatService {

    private final ChatSessionService chatSessionService;
    private final ChatRecordService chatRecordService;
    private final ChatSessionStatusService chatSessionStatusService;
    private final RedisUtils redisUtils;
    private final IdGen idGen;
    private final AuthHelper authHelper;
    private final TransactionTemplate transactionTemplate;
    private final DistLockFactory distLockFactory;
    private final RedisScript<Long> getNextSeqIfPresent;
    private final RedisScript<List> nextOrElectRebuild;
    private final RedisScript<Long> initFromBaseAndNext;
    private final RedisScript<List> lastOrElectRebuild;
    private final RedisScript<Long> initFromBase;
    private final StringRedisTemplate stringRedisTemplate;
    private final SimpMessagingTemplate messagingTemplate; // 用它来推送消息
    private final RocketMQTemplate rocketMQTemplate;

    private static final String CHAT_DESTINATION = "/queue/chat";

    @Value("${blog.chat-last-time-zSet-size}")
    private String chatLastTimeZSetSize;

    @Override
    public void send(ChatMsgDTO msg, Principal me) {
        // 当前登录用户（需要保证 Principal#getName() 是 userId 字符串）
        if (me == null || me.getName() == null) {
            throw new AccessDeniedException("Unauthenticated");
        }
        Long fromUserId = Long.valueOf(me.getName());

        // 这里做最基本的健壮性检查
        if (msg == null || msg.getToUserId() == null) return;
        if (msg.getContent() == null || msg.getContent().trim().isEmpty()) return;

        // 判断消息是否重复发送
        val idArray = BlogUtils.compareIdSize(fromUserId, msg.getToUserId());
        String todaySetKey = BlogUtils.getTodayChatMsgIdSetKey(idArray[0], idArray[1]);
        String yesterdaySetKey = BlogUtils.getYesterdayChatMsgIdSetKey(idArray[0], idArray[1]);
        // - 判断todaySetKey是否存在，不存在表示今天没发送过聊天信息，不存在重复问题
        if (redisUtils.hasKey(todaySetKey) && redisUtils.sIsMember(todaySetKey, msg.getTimestamp())) {
            // 已存在，忽略这条消息
            return;
        }
        // - 查看昨天的set
        if (redisUtils.hasKey(yesterdaySetKey) && redisUtils.sIsMember(yesterdaySetKey, msg.getTimestamp())) {
            // 已存在，忽略这条消息
            return;
        }
        // - 当这条消息没有重复，在set中添加（如果set不存在会直接创建）
        redisUtils.sAdd(todaySetKey, msg.getTimestamp());

        // 获取这条消息seq，发送给对方时带着这个seq
        val seq = getSessionNextSeq(idArray[0], idArray[1]);

        // 推给“目标用户”的专属收件箱：/user/{toUserId}/queue/chat
        messagingTemplate.convertAndSendToUser(
                msg.getToUserId().toString(),
                CHAT_DESTINATION,
                new ChatViewDTO(fromUserId, msg.getContent(), seq, System.currentTimeMillis())
        );
        // 也发给自己（方便多端登录情况）
        messagingTemplate.convertAndSendToUser(
                fromUserId.toString(),
                CHAT_DESTINATION,
                new ChatViewDTO(fromUserId, msg.getContent(), seq, System.currentTimeMillis())
        );

        // 发送mq消息，将聊天记录入库
        val mqDto = new ChatRecordsSaveMqDTO(msg, fromUserId, seq);

        val sendResult = rocketMQTemplate.syncSendOrderly(ChatRecordsSaveTopic, mqDto, idArray[0] + ":" + idArray[1]);
        if (sendResult == null || !sendResult.getSendStatus().equals(SendStatus.SEND_OK)) {
            log.error("ChatRecordsSaveTopic加入mq失败:{}, dto:{}", sendResult, mqDto);
        }

        // 更新最后一条消息的时间
        // TODO: 2025/9/25  需要进行判断，当对方离线时才更新
//        String zSetKey = CHAT_SESSION_MSG_LAST_TIME_KEY + msg.getToUserId();
//        redisUtils.zAdd(zSetKey, fromUserId, System.currentTimeMillis());
    }

    /**
     * 获取会话下一个seq
     * @param smallUserId 相对小的用户id
     * @param bigUserId 相对大的用户id
     */
    private Long getSessionNextSeq(Long smallUserId, Long bigUserId) {
        String seqKey = BlogUtils.getChatSessionSeqKey(smallUserId, bigUserId);
        String rebuildKey = BlogUtils.getChatSessionSeqRebuildKey(smallUserId, bigUserId);
        Long ttlSec = TimeUnit.DAYS.toSeconds(30L);
        Long rebuildLockMs = TimeUnit.SECONDS.toMillis(10L);
        int maxAttempts = 50;
        long sleepMs = 50;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                // 执行脚本 nextOrElectRebuild.lua
                List<String> keys = Arrays.asList(seqKey, rebuildKey);
                Object[] argvA = {String.valueOf(ttlSec), String.valueOf(rebuildLockMs)};
                List<?> res = stringRedisTemplate.execute(nextOrElectRebuild, keys, argvA);

                // 脚本异常或 Redis 暂时不可用：短暂退避重试
                if (res == null || res.size() < 2) {
                    BlogUtils.sleepJitter(sleepMs);
                    sleepMs = Math.min(sleepMs * 2, 800); // 指数退避封顶 800ms
                    continue;
                }

                // 元素一般就是 Long，但为保险起见按 Number 取值
                long code = ((Number) res.get(0)).longValue();
                long value = ((Number) res.get(1)).longValue();

                if (code == 1L) {
                    return value;
                }
                if (code == 3L) {
                    // 他人重建中，短暂等待后重试
                    Thread.sleep(100);
                    continue;
                }

                // code == 2：我当选重建者 → 去 DB 查最大 seq（base）
                val lastSeq = chatRecordService.getSessionLastSeq(smallUserId, bigUserId);
                if (lastSeq == null) {
                    // 没有聊天记录的情况下第一条记录的seq为1
                    return redisUtils.increment(seqKey, 1L, 30L, TimeUnit.DAYS);
                }

                // 调用脚本 initFromBaseAndNext.lua 执行“max 初始化 + INCR”
                List<String> argsB = Arrays.asList(String.valueOf(lastSeq), String.valueOf(ttlSec));
                return stringRedisTemplate.execute(initFromBaseAndNext, keys, argsB.toArray());
            } catch (Exception e) {
                // 不抛异常，重试即可
                log.error("获取会话last_seq失败", e);
            }
        }
        // 到这里说明 50 次都失败了：抛错或走降级
        throw new ApiException(BlogResultCode.CHAT_SESSION_LAST_SEQ_ERROR);
    }

    /**
     * 获取会话last_seq
     * @param smallUserId 较小的用户id
     * @param bigUserId 较大的用户id
     */
    private Long getSessionLastSeq(Long smallUserId, Long bigUserId) {
        String seqKey = BlogUtils.getChatSessionSeqKey(smallUserId, bigUserId);
        String rebuildKey = BlogUtils.getChatSessionSeqRebuildKey(smallUserId, bigUserId);

        final long ttlSec = TimeUnit.DAYS.toSeconds(30);
        final long rebuildLockMs = TimeUnit.SECONDS.toMillis(5);
        final int maxAttempts = 50;
        long sleepMs = 50;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                // 执行脚本 rebuildLockMs.lua，
                List<String> keys = Arrays.asList(seqKey, rebuildKey);
                Object[] argvA = {String.valueOf(ttlSec), String.valueOf(rebuildLockMs)};
                List<?> res = stringRedisTemplate.execute(lastOrElectRebuild, keys, argvA);

                // 脚本异常或 Redis 暂时不可用：短暂退避重试
                if (res == null || res.size() < 2) {
                    BlogUtils.sleepJitter(sleepMs);
                    sleepMs = Math.min(sleepMs * 2, 800);
                    continue;
                }

                long code = ((Number) res.get(0)).longValue();
                long value = ((Number) res.get(1)).longValue();

                if (code == 1L) {
                    // Redis 已有当前 seq，已续期，直接返回
                    return value;
                }
                if (code == 3L) {
                    // 他人重建中
                    BlogUtils.sleepJitter(sleepMs);
                    sleepMs = Math.min(sleepMs * 2, 800);
                    continue;
                }

                // code == 2：我当选重建者 → DB 查最大 seq（base）
                Long lastSeq = chatRecordService.getSessionLastSeq(smallUserId, bigUserId); // null 表示还没有消息
                long base = (lastSeq == null ? 0L : lastSeq);

                Object[] argvB = {String.valueOf(base), String.valueOf(ttlSec)};
                Long cur = stringRedisTemplate.execute(initFromBase, keys, argvB);
                if (cur != null) return cur;

                // 极端失败：退避重试
                BlogUtils.sleepJitter(sleepMs);
                sleepMs = Math.min(sleepMs * 2, 800);
            } catch (Exception e) {
                log.warn("getSessionLastSeq attempt failed, smallUserId={}, bigUserId={}, attempt={}", smallUserId, bigUserId, attempt, e);
                BlogUtils.sleepJitter(sleepMs);
                sleepMs = Math.min(sleepMs * 2, 800);
            }
        }

        // 走到这里说明 Redis/DB 出现了较长时间的异常：抛错交给上层重试或降级
        throw new ApiException(BlogResultCode.CHAT_SESSION_LAST_SEQ_ERROR);
    }

    @Override
    public void read(Long sessionId, Long seq) {
        val chatSessionEntity = chatSessionService.getById(sessionId);
        if (chatSessionEntity == null) {
            throw new ApiException(BlogResultCode.CHAT_SESSION_EMPTY);
        }
        // 将聊天状态中的最后一次读取的序列号设置为当前序列号
        val userId = authHelper.getCurrentUser().getId();
        chatSessionStatusService.update(Wrappers.<ChatSessionStatusEntity>lambdaUpdate()
                .set(ChatSessionStatusEntity::getLastReadSeq, seq)
                .eq(ChatSessionStatusEntity::getSessionId, sessionId)
                .eq(ChatSessionStatusEntity::getUserId, userId));

        // 将聊天记录中小于等于seq的都标为已读
        chatRecordService.update(Wrappers.<ChatRecordEntity>lambdaUpdate()
                .set(ChatRecordEntity::getReadStatus, true)
                .eq(ChatRecordEntity::getSessionId, sessionId)
                .eq(ChatRecordEntity::getToUserId, userId).le(ChatRecordEntity::getSeq, seq));
    }

    @Override
    public Long getUnreadCount(Long sessionId) {
        val chatSessionStatusEntity = chatSessionStatusService.getOne(Wrappers.<ChatSessionStatusEntity>lambdaQuery()
                .eq(ChatSessionStatusEntity::getSessionId, sessionId)
                .eq(ChatSessionStatusEntity::getUserId, authHelper.getCurrentUser().getId()));
        if (chatSessionStatusEntity == null) {
            log.error("sessionId:{}的会话状态不存在", sessionId);
            return null;
        }
        val idArray = BlogUtils.compareIdSize(chatSessionStatusEntity.getUserId(), chatSessionStatusEntity.getOtherUserId());
        val seq = redisUtils.get(BlogUtils.getChatSessionSeqKey(idArray[0], idArray[1]), Long.class);
        if (seq == null) {
            log.error("sessionId:{}的会话seq不存在", sessionId);
            return null;
        }
        return seq - chatSessionStatusEntity.getLastReadSeq();
    }

    @Override
    public Map<Long, Long> getUnreadCount(ChatUnreadCountDTO dto) {
        
        return null;
    }

    @Override
    public void setTopChat(Long sessionId) {
        val userId = authHelper.getCurrentUser().getId();
        // 获取用户聊天最大的置顶值
        int maxTopValue = chatSessionStatusService.getMaxTopValue(userId);
        // 将此聊天置顶值+1
        chatSessionStatusService.update(Wrappers.<ChatSessionStatusEntity>lambdaUpdate()
                .set(ChatSessionStatusEntity::getTop, maxTopValue + 1)
                .eq(ChatSessionStatusEntity::getUserId, userId)
                .eq(ChatSessionStatusEntity::getSessionId, sessionId));
    }

    @Override
    public void cancelTopChat(Long sessionId) {
        chatSessionStatusService.update(Wrappers.<ChatSessionStatusEntity>lambdaUpdate()
                .set(ChatSessionStatusEntity::getTop, 0)
                .eq(ChatSessionStatusEntity::getSessionId, sessionId));
    }

    @Override
    public void muteChat(Long sessionId) {
        chatSessionStatusService.update(Wrappers.<ChatSessionStatusEntity>lambdaUpdate()
                .set(ChatSessionStatusEntity::getMute, ChatSessionMuteEnum.HIDDEN.getCode())
                .eq(ChatSessionStatusEntity::getSessionId, sessionId));
    }

    @Override
    public void cancelMuteChat(Long sessionId) {
        chatSessionStatusService.update(Wrappers.<ChatSessionStatusEntity>lambdaUpdate()
                .set(ChatSessionStatusEntity::getMute, ChatSessionMuteEnum.NORMAL.getCode())
                .eq(ChatSessionStatusEntity::getSessionId, sessionId));
    }

    @Override
    public PageResult<ChatRecordEntity> getChatRecordsPage(ChatRecordPageDTO dto) {
        //
        return chatRecordService.page(dto, Wrappers.<ChatRecordEntity>lambdaQuery()
                .eq(ChatRecordEntity::getSessionId, dto.getSessionId())
                .orderByDesc(ChatRecordEntity::getSeq));// 聊天记录要按照倒序排序
    }

    @Override
    public Long getSessionId(Long toUserId) {
        val idArray = BlogUtils.compareIdSize(authHelper.getCurrentUser().getId(), toUserId);
        val chatSessionEntity = chatSessionService.getOne(Wrappers.<ChatSessionEntity>lambdaQuery()
                .eq(ChatSessionEntity::getSmallUserId, idArray[0])
                .eq(ChatSessionEntity::getBigUserId, idArray[1]));
        return chatSessionEntity == null ? null : chatSessionEntity.getId();
    }

    @Override
    public PageResult<ChatUnreadMessageDTO> getUnreadMessagePage(ChatUnreadRecordPageDTO dto) {
        if (dto == null) {
            throw new ApiException(CommonCode.ILLEGAL_PARAMS);
        }
        List<Long> sessionIdList = getNextPageSessionIds(dto);
        if (sessionIdList.isEmpty()) {
            return new PageResult<>(Collections.emptyList(), 0, dto.getSize(), dto.getCurrent(), 0);
        }
        return unreadMessagePage(dto, sessionIdList);
    }

    private List<Long> getNextPageSessionIds(ChatUnreadRecordPageDTO dto) {
        List<Long> result = new ArrayList<>((int) dto.getSize());

        // 从zset中获取会话
        Long userId = authHelper.getCurrentUser().getId();
        String zSetKey = CHAT_SESSION_MSG_LAST_TIME_KEY + userId;
        if (dto.getLastSessionId() != null && dto.getLastTimeStamp() != null) {
            // - 不是第一页的话 先检查是否有和上一页最后一个会话时间戳相同的会话
            Set<Object> scoreEqSet = redisUtils.zRangeByScoreEq(zSetKey, dto.getLastTimeStamp());
            if (scoreEqSet != null && scoreEqSet.size() > 1) {
                // -- 有score重复的会话
                // --- 将set中元素排序，找出 dto.getLastSessionId()之后的元素
                List<Long> sameScoreDescNextSessionIds = scoreEqSet.stream()
                        .filter(Objects::nonNull)
                        .map(String::valueOf)
                        .sorted(Comparator.reverseOrder()) // 字典序倒序，和zRangeByScore中倒序获取的顺序一致
                        .map(Long::parseLong) // 转为Long方便后续操作
                        .filter(sessionId -> sessionId < dto.getLastSessionId()) // 倒序的情况下小于dto.getLastSessionId()的元素是我们这次需要的
                        .collect(Collectors.toList());
                if (!sameScoreDescNextSessionIds.isEmpty()) {
                    if (sameScoreDescNextSessionIds.size() >= dto.getSize()) {
                        // --- 当同分的下一页元素多与dto.getSize()时，直接返回sameScoreAsc中的元素即可
                        result = sameScoreDescNextSessionIds.stream()
                                .limit(dto.getSize())
                                .collect(Collectors.toList());
                        // 直接处理nextPageSessionIds
                        return result;
                    } else {
                        // --- 当同分的下一页元素小于dto.getSize()时，将它们全部装入nextPageSessionIds，然后还需继续补全剩余元素
                        result = sameScoreDescNextSessionIds;
                    }
                }
            }
        }

        // - 获取下一页会话id
        Set<Object> zRangeByScore = redisUtils.zRangeByScore(zSetKey,
                0, // min
                dto.getLastTimeStamp() - 1, // max，因你用毫秒整型，ts-1 就是“严格小于 ts”
                0, dto.getSize() - result.size(), // 此次获取数量为每页数量-score重复的元素数量
                true); // reverse=true 表示倒序
        // - 如果redis中没有，看看是否超过了zset最大容量，如果超过了就从mysql中获取数据
        if (zRangeByScore.isEmpty()) {
            val size = redisUtils.zSize(zSetKey);
            if (size >= Long.parseLong(chatLastTimeZSetSize)) {
                // TODO: 2025/9/15  从mysql中获取数据
            }
            // 没有超过最大容量，说明没有数据
            return result;
        }

        val sessionIdSet = zRangeByScore.stream().map(it -> Long.parseLong(it.toString())).collect(Collectors.toSet());
        result.addAll(sessionIdSet);

        return result;
    }

    /**
     * 获取每个会话的20条未读消息、最后一条消息、未读消息数
     */
    private PageResult<ChatUnreadMessageDTO> unreadMessagePage(ChatUnreadRecordPageDTO dto, List<Long> sessionIdList) {
        if (sessionIdList == null || sessionIdList.isEmpty()) {
            return new PageResult<>(Collections.emptyList(), 0, dto.getSize(), dto.getCurrent(), 0);
        }
        // 查询每个会话的20条未读信息
        List<ChatRecordEntity> unreadRecords = chatRecordService.getUnreadMsgForSession(authHelper.getCurrentUser().getId(), sessionIdList, 20);
        if (unreadRecords.isEmpty()) {
            return new PageResult<>(Collections.emptyList(), 0, dto.getSize(), dto.getCurrent(), 0);
        }
        // 获取每个会话的未读消息数量
        List<SessionUnreadDTO> unreadCounts = chatRecordService.getSessionUnreadCount(authHelper.getCurrentUser().getId(), sessionIdList);
        // 如果会话未读消息数<=20,那么直接从unreadRecords中获取最后一条消息，否则再查询一次
        List<ChatRecordEntity> lastRecords = new ArrayList<>();
        List<Long> sessionIds = unreadCounts.stream()
                .filter(it -> it.getUnreadCount() > 20)
                .map(SessionUnreadDTO::getSessionId)
                .collect(Collectors.toList());
        if (!sessionIds.isEmpty()) {
            // 获取会话的最后一条消息
            lastRecords = chatRecordService.getLastMsgForSession(sessionIds);
        }
        // 获取聊天对象的用户信息
        List<UserEntity> users = chatSessionService.getUserInfo(authHelper.getCurrentUser().getId(), sessionIdList);

        // 组装数据
        List<ChatUnreadMessageDTO> records = new ArrayList<>();
        for (SessionUnreadDTO sessionUnreadDTO : unreadCounts) {
            ChatUnreadMessageDTO cumDTO = new ChatUnreadMessageDTO();
            cumDTO.setSessionId(sessionUnreadDTO.getSessionId());
            cumDTO.setUnreadCount(sessionUnreadDTO.getUnreadCount());
            // 20条消息
            val collect = unreadRecords.stream()
                    .filter(it -> it.getSessionId().equals(sessionUnreadDTO.getSessionId()))
                    .sorted(Comparator.comparing(ChatRecordEntity::getCreateTime))
                    .collect(Collectors.toList());
            cumDTO.setRecords(collect);
            // 用户信息
            Long fromUserId = collect.get(0).getFromUserId();
            cumDTO.setUser(new UserInfoDTO(users.stream().filter(it -> it.getId().equals(fromUserId)).findFirst().orElse(null)));
            // 最后一条消息
            if (sessionUnreadDTO.getUnreadCount() > 20) {
                cumDTO.setLastRecord(collect.get(collect.size() - 1));
            } else {
                cumDTO.setLastRecord(lastRecords.stream().filter(it -> it.getSessionId().equals(sessionUnreadDTO.getSessionId())).findFirst().orElse(null));
            }
        }

        return new PageResult<>(records, 0, dto.getSize(), dto.getCurrent(), 0);
    }
}

