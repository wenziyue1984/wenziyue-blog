package com.wenziyue.blog.web.controller;

import com.wenziyue.blog.biz.service.BizArticleService;
import com.wenziyue.blog.dal.dto.*;
import com.wenziyue.blog.dal.entity.TagEntity;
import com.wenziyue.framework.annotation.ResponseResult;
import com.wenziyue.mybatisplus.page.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDateTime;

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

//    @Operation(summary = "请求生成slug", description = "返回listenKey，用listenKey请求/getSlug方法")
//    @PostMapping("/generateSlug")
//    public String generateSlug(@Parameter(description = "生成slug参数", required = true) @Valid @RequestBody SlugDTO dto) {
//        return bizArticleService.generateSlug(dto);
//    }
//
//    @Operation(summary = "获取slug结果", description = "获取slug结果")
//    @PostMapping("/getSlug/{listenKey}")
//    public String getSlug(@Parameter(description = "生成slug参数", required = true) @PathVariable String listenKey) {
//        return bizArticleService.getSlug(listenKey);
//    }

    @Operation(summary = "获取文章标签", description = "有则返回，无则新建")
    @GetMapping("/getTag")
    public TagDTO getTag(@Parameter(description = "标签参数", required = true) @Valid TagDTO dto) {
        return bizArticleService.getTag(dto);
    }

    @Operation(summary = "文章标签分页列表", description = "文章标签分页列表")
    @PostMapping("/pageArticleTag")
    public PageResult<TagEntity> pageArticleTag(@Parameter(description = "分页参数", required = true) @Valid @RequestBody TagPageDTO dto) {
        return bizArticleService.pageArticleTag(dto);
    }

    @Operation(summary = "修改文章标签状态", description = "启动/禁用")
    @PostMapping("/changeTagStatus")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void changeTagStatus(@Parameter(description = "标签id", required = true) @RequestBody TagDTO dto) {
        bizArticleService.changeTagStatus(dto);
    }

    @Operation(summary = "保存/修改文章", description = "返回文章id")
    @PostMapping("/saveOrUpdate")
    @PreAuthorize("hasAuthority('USER')")
    public String saveOrUpdate(@Parameter(description = "保存/修改文章参数", required = true) @Valid @RequestBody ArticleDTO dto) {
        return bizArticleService.saveOrUpdate(dto);
    }

    @Operation(summary = "文章分页列表", description = "文章分页列表")
    @PostMapping("/pageArticle")
    public PageResult<ArticleDTO> pageArticle(@Parameter(description = "分页参数", required = true) @Valid @RequestBody ArticlePageDTO dto) {
        return bizArticleService.pageArticle(dto);
    }

    @Operation(summary = "获取文章详情", description = "获取文章详情")
    @GetMapping("/getArticle/{id}")
    public ArticleDTO getArticleDetail(@Parameter(description = "文章id", required = true) @PathVariable Long id) {
        return bizArticleService.getArticleDetail(id);
    }

    @Operation(summary = "隐藏文章", description = "隐藏文章")
    @GetMapping("/hideArticle/{id}")
    public void hideArticle(@Parameter(description = "文章id", required = true) @PathVariable Long id) {
        bizArticleService.hideArticle(id);
    }

    @Operation(summary = "删除文章", description = "删除文章")
    @DeleteMapping("/deleteArticle/{id}")
    public void deleteArticle(@Parameter(description = "文章id", required = true) @PathVariable Long id) {
        bizArticleService.deleteArticle(id);
    }

    @Operation(summary = "文章置顶", description = "文章置顶")
    @GetMapping("/setTopArticle/{id}")
    public void setTopArticle(@Parameter(description = "文章id", required = true) @PathVariable Long id) {
        bizArticleService.setTopArticle(id);
    }

    @Operation(summary = "文章取消置顶", description = "文章取消置顶")
    @GetMapping("/cancelTopArticle/{id}")
    public void cancelTopArticle(@Parameter(description = "文章id", required = true) @PathVariable Long id) {
        bizArticleService.cancelTopArticle(id);
    }

    @Operation(summary = "文章收藏", description = "文章收藏")
    @PostMapping("/favoritesArticle")
    @PreAuthorize("hasAuthority('USER')")
    public void favoritesArticle(@Parameter(description = "文章id", required = true) @RequestBody @Valid FavoritesArticleDTO dto) {
        bizArticleService.favoritesArticle(dto);
    }

    @Operation(summary = "取消文章收藏", description = "取消文章收藏")
    @PostMapping("/cancelFavoritesArticle")
    @PreAuthorize("hasAuthority('USER')")
    public void cancelFavoritesArticle(@Parameter(description = "文章id", required = true) @RequestBody @Valid FavoritesArticleDTO dto) {
        bizArticleService.cancelFavoritesArticle(dto);
    }

    @Operation(summary = "文章点赞", description = "文章点赞")
    @GetMapping("/likeArticle/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public void likeArticle(@Parameter(description = "文章id", required = true) @PathVariable Long id) {
        bizArticleService.likeArticle(id);
    }

    @Operation(summary = "取消文章点赞", description = "取消文章点赞")
    @GetMapping("/cancelLikeArticle/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public void cancelLikeArticle(@Parameter(description = "文章id", required = true) @PathVariable Long id) {
        bizArticleService.cancelLikeArticle(id);
    }

    @Operation(summary = "文章pv统计", description = "文章pv统计")
    @PostMapping("/pv")
    public void pv(@Parameter(description = "文章pv统计参数", required = true) @Valid @RequestBody ArticlePvDTO dto, HttpServletRequest request) {
        dto.setIp(request.getRemoteAddr());
        dto.setUserAgent(request.getHeader("User-Agent"));
        dto.setTimestamp(LocalDateTime.now().toString());
        bizArticleService.pv(dto);
    }

}
