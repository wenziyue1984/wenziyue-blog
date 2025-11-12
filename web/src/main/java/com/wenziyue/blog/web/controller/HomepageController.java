package com.wenziyue.blog.web.controller;

import com.wenziyue.blog.biz.service.BizHomepageService;
import com.wenziyue.blog.dal.dto.ArticlePageDTO;
import com.wenziyue.blog.dal.dto.FeedPageDTO;
import com.wenziyue.framework.annotation.ResponseResult;
import com.wenziyue.mybatisplus.page.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author wenziyue
 */
@RestController
@RequestMapping("/homepage")
@Slf4j
@RequiredArgsConstructor
@ResponseResult
@Tag(name = "首页管理", description = "首页相关接口")
public class HomepageController {

    private final BizHomepageService bizHomepageService;


    @Operation(summary = "分页查看feed流", description = "分页查看feed流")
    @PostMapping("/pageFeed")
    public PageResult<ArticlePageDTO> pageFeed(@Parameter(description = "分页请求参数", required = true) @Valid @RequestBody FeedPageDTO dto) {
        return bizHomepageService.pageFeed(dto);
    }



}
