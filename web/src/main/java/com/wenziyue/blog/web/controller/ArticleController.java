package com.wenziyue.blog.web.controller;

import com.wenziyue.blog.biz.service.BizArticleService;
import com.wenziyue.blog.dal.dto.ArticleDTO;
import com.wenziyue.blog.dal.dto.SlugDTO;
import com.wenziyue.framework.annotation.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 文章接口
 *
 * @author wenziyue
 */
@RestController
@RequestMapping("/article")
@Slf4j
@RequiredArgsConstructor
@ResponseResult
@Tag(name = "文章管理", description = "文章相关接口")
public class ArticleController {

    private final BizArticleService bizArticleService;

    @Operation(summary = "请求生成slug", description = "返回listenKey，用listenKey请求/getSlug方法")
    @PostMapping("/generateSlug")
    public String generateSlug(@Parameter(description = "生成slug参数", required = true) @Valid @RequestBody SlugDTO dto) {
        return bizArticleService.generateSlug(dto);
    }

    @Operation(summary = "获取slug结果", description = "获取slug结果")
    @PostMapping("/getSlug/{listenKey}")
    public String getSlug(@Parameter(description = "生成slug参数", required = true) @PathVariable String listenKey) {
        return bizArticleService.getSlug(listenKey);
    }


    @Operation(summary = "保存文章", description = "返回文章id")
    @PostMapping("/save")
    public String save(@Parameter(description = "保存文章参数", required = true) @Valid @RequestBody ArticleDTO dto) {
        return bizArticleService.save(dto);
    }



}
