package com.bhavya.documentworkflowplatform.repository;

import com.bhavya.documentworkflowplatform.entity.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkflowRepository
        extends JpaRepository<Workflow, Long> {

    List<Workflow> findByDocumentId(Long documentId);
}
