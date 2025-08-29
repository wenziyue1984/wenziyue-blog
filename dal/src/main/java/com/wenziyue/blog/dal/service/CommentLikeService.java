package com.wenziyue.blog.dal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wenziyue.blog.dal.dto.CommentLikeMqDTO;
import com.wenziyue.blog.dal.entity.CommentLikeEntity;

import java.util.List;

/**
 * @author wenziyue
 */
public interface CommentLikeService extends IService<CommentLikeEntity> {


    void insertIgnoreBatch(List<CommentLikeMqDTO> likes);

    void deleteBatch(List<CommentLikeMqDTO> cancels);

    List<CommentLikeEntity> selectExistingPairs(List<CommentLikeMqDTO> likeList);
}
