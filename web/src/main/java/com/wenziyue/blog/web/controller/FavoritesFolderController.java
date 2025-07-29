package com.wenziyue.blog.web.controller;

import com.wenziyue.blog.biz.service.BizFavoritesFolderService;
import com.wenziyue.blog.dal.dto.ArticleDTO;
import com.wenziyue.blog.dal.dto.FavoritesArticlePageDTO;
import com.wenziyue.blog.dal.dto.FavoritesFolderDTO;
import com.wenziyue.blog.dal.dto.FavoritesFolderPageDTO;
import com.wenziyue.blog.dal.entity.FavoritesFolderEntity;
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
@RequestMapping("/favoritesFolder")
@Slf4j
@RequiredArgsConstructor
@ResponseResult
@Tag(name = "收藏夹", description = "收藏夹相关接口")
public class FavoritesFolderController {

    private final BizFavoritesFolderService bizFavoritesFolderService;

    @Operation(summary = "创建收藏夹", description = "创建收藏夹")
    @PostMapping("/createFavoritesFolder")
    @PreAuthorize("hasAuthority('USER')")
    public String createFavoritesFolder(@Parameter(description = "文章id", required = true) @RequestBody FavoritesFolderDTO dto) {
        return bizFavoritesFolderService.createFavoritesFolder(dto);
    }

    @Operation(summary = "修改收藏夹", description = "修改收藏夹")
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('USER')")
    public String updateFavoritesFolder(@Parameter(description = "文章id", required = true) @RequestBody FavoritesFolderDTO dto) {
        return bizFavoritesFolderService.updateFavoritesFolder(dto);
    }

    @Operation(summary = "删除收藏夹", description = "删除收藏夹")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public void deleteFavoritesFolder(@Parameter(description = "文章id", required = true) @PathVariable String id) {
        bizFavoritesFolderService.deleteFavoritesFolder(id);
    }

    @Operation(summary = "收藏夹分页列表", description = "收藏夹分页列表")
    @PostMapping("/favoritesFolderPage")
    public PageResult<FavoritesFolderEntity> favoritesFolderPage(@Parameter(description = "分页参数", required = true) @RequestBody @Valid FavoritesFolderPageDTO dto) {
        return bizFavoritesFolderService.favoritesFolderPage(dto);
    }

    @Operation(summary = "收藏夹中文章分页列表", description = "收藏夹中文章分页列表")
    @PostMapping("/favoritesArticlePage")
    public PageResult<ArticleDTO> favoritesArticlePage(@Parameter(description = "分页参数", required = true) @RequestBody @Valid FavoritesArticlePageDTO dto) {
        return bizFavoritesFolderService.favoritesArticlePage(dto);
    }



}
