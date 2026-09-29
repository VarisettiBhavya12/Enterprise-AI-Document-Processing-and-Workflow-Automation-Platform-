package com.bhavya.documentworkflowplatform.service;

import com.bhavya.documentworkflowplatform.entity.Workflow;
import com.bhavya.documentworkflowplatform.repository.WorkflowRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkflowService {

    private final WorkflowRepository workflowRepository;

    public WorkflowService(WorkflowRepository workflowRepository) {
        this.workflowRepository = workflowRepository;
    }

    public Workflow createWorkflow(Long documentId,
                                   String assignedTo) {

        Workflow workflow = new Workflow();

        workflow.setDocumentId(documentId);
        workflow.setAssignedTo(assignedTo);
        workflow.setStatus("PENDING");
        workflow.setCreatedAt(LocalDateTime.now());

        return workflowRepository.save(workflow);
    }

    public List<Workflow> getAllWorkflows() {
        return workflowRepository.findAll();
    }

    public Workflow approveWorkflow(Long id,
                                    String comments) {

        Workflow workflow = workflowRepository
                .findById(id)
                .orElse(null);

        if (workflow != null) {
            workflow.setStatus("APPROVED");
            workflow.setComments(comments);

            return workflowRepository.save(workflow);
        }

        return null;
    }

    public Workflow rejectWorkflow(Long id,
                                   String comments) {

        Workflow workflow = workflowRepository
                .findById(id)
                .orElse(null);

        if (workflow != null) {
            workflow.setStatus("REJECTED");
            workflow.setComments(comments);

            return workflowRepository.save(workflow);
        }

        return null;
    }
}
