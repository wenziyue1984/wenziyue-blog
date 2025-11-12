package com.wenziyue.blog.biz.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wenziyue.blog.biz.security.AuthHelper;
import com.wenziyue.blog.biz.service.BizNotifyService;
import com.wenziyue.blog.dal.dto.NotifyDTO;
import com.wenziyue.blog.dal.dto.NotifyPageDTO;
import com.wenziyue.blog.dal.entity.NotifyInboxEntity;
import com.wenziyue.blog.dal.service.NotifyInboxService;
import com.wenziyue.framework.exception.ApiException;
import com.wenziyue.mybatisplus.page.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

import static com.wenziyue.blog.common.exception.BlogResultCode.NOTIFY_NOT_EXIST;

/**
 * @author wenziyue
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BizNotifyServiceImpl implements BizNotifyService {

    private final NotifyInboxService notifyInboxService;
    private final AuthHelper authHelper;


    @Override
    public PageResult<NotifyDTO> pageNotify(NotifyPageDTO dto) {
        val page = notifyInboxService.page(dto, Wrappers.<NotifyInboxEntity>lambdaQuery()
                .eq(NotifyInboxEntity::getRecipientUserId, authHelper.getCurrentUser().getId())
                .eq(dto.getRead() != null, NotifyInboxEntity::getStatus, dto.getRead()));

        val collect = page.getRecords().stream().map(NotifyDTO::new).collect(Collectors.toList());

        return PageResult.<NotifyDTO>builder()
                .records(collect)
                .current(page.getCurrent())
                .size(page.getSize())
                .total(page.getTotal())
                .pages(page.getPages())
                .build();
    }

    @Override
    public NotifyDTO getNotifyById(Long id) {
        val notifyInboxEntity = notifyInboxService.getById(id);
        if (notifyInboxEntity == null) {
            throw new ApiException(NOTIFY_NOT_EXIST);
        }
        return new NotifyDTO(notifyInboxEntity);
    }
}
