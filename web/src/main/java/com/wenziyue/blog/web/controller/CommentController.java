package com.wenziyue.blog.web.controller;

import com.wenziyue.blog.biz.security.AuthHelper;
import com.wenziyue.blog.biz.service.BizCommentService;
import com.wenziyue.blog.dal.dto.CommentDTO;
import com.wenziyue.blog.dal.dto.CommentPageDTO;
import com.wenziyue.framework.annotation.ResponseResult;
import com.wenziyue.mybatisplus.page.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author wenziyue
 */
@RestController
@RequestMapping("/comment")
@Slf4j
@RequiredArgsConstructor
@ResponseResult
@Tag(name = "评论管理", description = "评论相关接口")
public class CommentController {

    private final BizCommentService bizCommentService;
    private final AuthHelper authHelper;

    @Operation(summary = "发布评论", description = "发布评论")
    @PostMapping("/postComment")
    @PreAuthorize("hasAuthority('USER')")
    public Long postComment(@Parameter(description = "发布评论参数", required = true) @Valid @RequestBody CommentDTO dto) {
        return bizCommentService.postComment(dto, authHelper.getCurrentUser().getId());
    }

    @Operation(summary = "点赞评论", description = "点赞评论")
    @GetMapping("/likeComment/{commentId}")
    @PreAuthorize("hasAuthority('USER')")
    public void likeComment(@Parameter(description = "点赞评论参数", required = true) @PathVariable Long commentId) {
        bizCommentService.likeComment(commentId, authHelper.getCurrentUser().getId());
    }

    @Operation(summary = "取消点赞评论", description = "取消点赞评论")
    @GetMapping("/cancelLikeComment/{commentId}")
    @PreAuthorize("hasAuthority('USER')")
    public void cancelLikeComment(@Parameter(description = "点赞评论参数", required = true) @PathVariable Long commentId) {
        bizCommentService.cancelLikeComment(commentId);
    }

    @Operation(summary = "一级评论分页列表", description = "一级评论分页列表，带两条二级评论")
    @PostMapping("/pageOneLevelComment")
    public PageResult<CommentDTO> pageOneLevelComment(@Parameter(description = "分页参数", required = true) @Valid @RequestBody CommentPageDTO dto) {
        return bizCommentService.pageOneLevelComment(dto);
    }

    @Operation(summary = "二级评论分页列表", description = "二级评论分页列表")
    @PostMapping("/pageTwoLevelComment")
    public PageResult<CommentDTO> pageTwoLevelComment(@Parameter(description = "分页参数", required = true) @Valid @RequestBody CommentPageDTO dto) {
        return bizCommentService.pageTwoLevelComment(dto);
    }



}
