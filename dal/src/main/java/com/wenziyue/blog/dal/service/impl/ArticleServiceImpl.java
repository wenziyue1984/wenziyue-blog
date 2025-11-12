package com.wenziyue.blog.dal.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wenziyue.blog.dal.dto.ArticleDTO;
import com.wenziyue.blog.dal.dto.ArticlePageDTO;
import com.wenziyue.blog.dal.dto.FeedPageDTO;
import com.wenziyue.blog.dal.entity.ArticleEntity;
import com.wenziyue.blog.dal.mapper.ArticleMapper;
import com.wenziyue.blog.dal.service.ArticleService;
import com.wenziyue.mybatisplus.page.PageResult;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * @author wenziyue
 */
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, ArticleEntity> implements ArticleService {

    @Override
    public PageResult<ArticleDTO> page(ArticlePageDTO dto) {

        val pageParam = PageDTO.of(dto.getCurrent(), dto.getSize());
        IPage<ArticleEntity> pageResult = baseMapper.page(pageParam, dto);

        // 映射为 DTO
        val dtoList = pageResult.getRecords().stream().map(it -> {
            ArticleDTO a = new ArticleDTO();
            a.setId(it.getId());
            a.setTitle(it.getTitle());
            a.setCoverUrl(it.getCoverUrl());
            a.setViewCount(it.getViewCount());
            a.setLikeCount(it.getLikeCount());
            a.setSlug(it.getSlug());
            a.setIsTop(it.getIsTop());
            a.setStatus(it.getStatus());
            return a;
        }).collect(Collectors.toList());

        return PageResult.<ArticleDTO>builder()
                .records(dtoList)
                .current(pageResult.getCurrent())
                .size(pageResult.getSize())
                .total(pageResult.getTotal())
                .pages(pageResult.getPages())
                .build();
    }

    @Override
    public PageResult<ArticlePageDTO> feed(FeedPageDTO dto, Long id) {
        val pageParam = PageDTO.of(dto.getCurrent(), dto.getSize());
        IPage<ArticlePageDTO> pageResult = baseMapper.feed(pageParam, dto.getLastTime(), id);
        return PageResult.<ArticlePageDTO>builder()
                .records(pageResult.getRecords())
                .current(pageResult.getCurrent())
                .size(pageResult.getSize())
                .total(pageResult.getTotal())
                .pages(pageResult.getPages())
                .build();
    }
}