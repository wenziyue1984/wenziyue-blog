package com.wenziyue.blog.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wenziyue.blog.dal.dto.ArticlePageDTO;
import com.wenziyue.blog.dal.entity.ArticleEntity;
import org.apache.ibatis.annotations.Param;

/**
 * @author wenziyue
 */
public interface ArticleMapper extends BaseMapper<ArticleEntity>{

    IPage<ArticleEntity> page(Page<?> page, @Param("dto") ArticlePageDTO dto);
}
