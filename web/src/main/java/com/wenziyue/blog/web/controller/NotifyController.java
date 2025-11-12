package com.wenziyue.blog.web.controller;

import com.wenziyue.blog.biz.service.BizNotifyService;
import com.wenziyue.blog.dal.dto.NotifyDTO;
import com.wenziyue.blog.dal.dto.NotifyPageDTO;
import com.wenziyue.framework.annotation.ResponseResult;
import com.wenziyue.mybatisplus.page.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 通知接口
 * @author wenziyue
 */
@RestController
@RequestMapping("/notify")
@Slf4j
@RequiredArgsConstructor
@ResponseResult
@Tag(name = "通知管理", description = "通知相关接口")
public class NotifyController {

    private final BizNotifyService bizNotifyService;

    @Operation(summary = "获取通知分页列表", description = "获取通知分页列表")
    @PostMapping("/pageNotify")
    public PageResult<NotifyDTO> pageArticle(@Parameter(description = "分页参数", required = true) @Valid @RequestBody NotifyPageDTO dto) {
        return bizNotifyService.pageNotify(dto);
    }

    @Operation(summary = "获取通知详情", description = "获取通知详情")
    @GetMapping("/getNotifyById/{id}")
    public NotifyDTO getNotifyById(@Parameter(description = "通知id", required = true) @PathVariable("id") Long id) {
        return bizNotifyService.getNotifyById(id);
    }


}
