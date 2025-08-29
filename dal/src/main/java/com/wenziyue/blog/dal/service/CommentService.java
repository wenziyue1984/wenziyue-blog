package com.wenziyue.blog.dal.service;

import com.wenziyue.blog.dal.dto.CommentDTO;
import com.wenziyue.blog.dal.dto.CommentLikeDeltaDTO;
import com.wenziyue.blog.dal.dto.CommentPageDTO;
import com.wenziyue.blog.dal.entity.CommentEntity;
import com.wenziyue.mybatisplus.base.PageExtendService;
import com.wenziyue.mybatisplus.page.PageRequest;
import com.wenziyue.mybatisplus.page.PageResult;

import java.util.List;

/**
 * @author wenziyue
 */
public interface CommentService extends PageExtendService<CommentEntity> {

    PageResult<CommentDTO> oneLevelCommentPage(PageRequest dto, Long articleId, Integer sort);
    List<CommentDTO> getTwoLevelCommentForOneLevelComment(List<Long> oneLevelCommentIdList, int limit);
    PageResult<CommentDTO> twoLevelCommentPage(CommentPageDTO dto, Long articleId, Long oneLevelCommentId);

    int batchApplyLikeDeltas(List<CommentLikeDeltaDTO> items);
}
