package com.wenziyue.blog.dal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wenziyue.blog.dal.dto.CommentLikeMqDTO;
import com.wenziyue.blog.dal.entity.CommentLikeEntity;
import com.wenziyue.blog.dal.mapper.CommentLikeMapper;
import com.wenziyue.blog.dal.service.CommentLikeService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wenziyue
 */
@Service
public class CommentLikeServiceImpl extends ServiceImpl<CommentLikeMapper, CommentLikeEntity> implements CommentLikeService {

    @Override
    public void insertIgnoreBatch(List<CommentLikeMqDTO> likes) {
        if (likes == null || likes.isEmpty()) {
            return;
        }
        baseMapper.insertIgnoreBatch(likes.stream()
                .map(item -> CommentLikeEntity.builder().commentId(item.getCommentId()).userId(item.getUserId()).build())
                .collect(Collectors.toList()));
    }

    @Override
    public void deleteBatch(List<CommentLikeMqDTO> cancels) {
        if (cancels == null || cancels.isEmpty()) {
            return;
        }
        baseMapper.deleteBatch(cancels.stream()
                .map(item -> CommentLikeEntity.builder().commentId(item.getCommentId()).userId(item.getUserId()).build())
                .collect(Collectors.toList()));
    }

    @Override
    public List<CommentLikeEntity> selectExistingPairs(List<CommentLikeMqDTO> likeList) {
        if (likeList == null || likeList.isEmpty()) {
            return Collections.emptyList();
        }
        return baseMapper.selectExistingPairs(likeList);
    }
}
