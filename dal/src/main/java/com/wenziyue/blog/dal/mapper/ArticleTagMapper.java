package com.wenziyue.blog.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wenziyue.blog.dal.dto.TagDTO;
import com.wenziyue.blog.dal.entity.ArticleTagEntity;

import java.util.List;

/**
 * @author wenziyue
 */
public interface ArticleTagMapper extends BaseMapper<ArticleTagEntity> {

    List<TagDTO> getEnabledTags(Long id);
}
