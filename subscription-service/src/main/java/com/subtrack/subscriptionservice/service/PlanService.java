package com.subtrack.subscriptionservice.service;

import com.subtrack.subscriptionservice.dto.PlanDto;

import java.util.List;

public interface PlanService {
    List<PlanDto> getAllActivePlans();
    List<PlanDto> getAllPlans();
    PlanDto getPlanById(Long id);
    PlanDto createPlan(PlanDto planDto);
    PlanDto updatePlan(Long id, PlanDto planDto);
    void deletePlan(Long id);
}
