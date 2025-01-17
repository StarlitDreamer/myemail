package com.java.email.controller;

import com.java.email.common.Result;
import com.java.email.entity.Template;
import com.java.email.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/templates")
public class TemplateController {

    @Autowired
    private TemplateService templateService;

    /**
     * 根据模板 ID 获取模板内容
     *
     * @param templateId 模板 ID
     * @return HTML 内容字符串
     */
    @GetMapping("/useTemplate/{templateId}")
    public Result getTemplateContent(@PathVariable String templateId) {
        return Result.success(templateService.getTemplateContentById(templateId));
    }

    /**
     * 根据条件筛选模板
     *
     * @param ownerUserIds  所属用户ID列表
     * @param creator       创建人
     * @param creatorId     创建人ID
     * @param status        模板状态
     * @param templateName  模板名称
     * @param templateType  模板类型
     * @return 符合条件的模板列表
     */
    @GetMapping("/search")
    public Result<List<Template>> searchTemplates(
            @RequestParam(required = false) List<String> ownerUserIds,
            @RequestParam(required = false) String creator,
            @RequestParam(required = false) String creatorId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String templateName,
            @RequestParam(required = false) Integer templateType) {
        return templateService.findTemplatesByCriteria(ownerUserIds, creator, creatorId, status, templateName, templateType);
    }

    /**
     * 分页查询模板数据
     *
     * @param pageNum  当前页码（从 0 开始）
     * @param pageSize 每页大小
     * @return 当前页的数据列表
     */
    @GetMapping
    public Result<List<Template>> getTemplates(
            @RequestParam(defaultValue = "0") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        List<Template> templates = templateService.getTemplates(pageNum, pageSize);
        return Result.success(templates);
    }
}