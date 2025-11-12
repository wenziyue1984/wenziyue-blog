package com.wenziyue.blog.biz.service;

import com.wenziyue.blog.dal.dto.NotifyDTO;
import com.wenziyue.blog.dal.dto.NotifyPageDTO;
import com.wenziyue.mybatisplus.page.PageResult;

/**
 * @author wenziyue
 */
public interface BizNotifyService {
    PageResult<NotifyDTO> pageNotify(NotifyPageDTO dto);

    NotifyDTO getNotifyById(Long id);
}
