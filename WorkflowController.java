package com.bhavya.documentworkflowplatform.controller;

import com.bhavya.documentworkflowplatform.entity.Workflow;
import com.bhavya.documentworkflowplatform.service.WorkflowService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping
    public Workflow createWorkflow(
            @RequestBody Map<String, Object> request) {

        Long documentId = Long.valueOf(
                request.get("documentId").toString()
        );

        String assignedTo =
                request.get("assignedTo").toString();

        return workflowService.createWorkflow(
                documentId,
                assignedTo
        );
    }

    @GetMapping
    public List<Workflow> getAllWorkflows() {
        return workflowService.getAllWorkflows();
    }

    @PutMapping("/{id}/approve")
    public Workflow approve(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> request) {

        String comments = "";

        if (request != null && request.get("comment") != null) {
            comments = request.get("comment");
        }

        return workflowService.approveWorkflow(
                id,
                comments
        );
    }

    @PutMapping("/{id}/reject")
    public Workflow reject(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> request) {

        String comments = "";

        if (request != null && request.get("comment") != null) {
            comments = request.get("comment");
        }

        return workflowService.rejectWorkflow(
                id,
                comments
        );
    }
}