package com.subtrack.subscriptionservice.service;

import com.subtrack.subscriptionservice.dto.PlanDto;
import com.subtrack.subscriptionservice.entity.Plan;
import com.subtrack.subscriptionservice.exception.ResourceNotFoundException;
import com.subtrack.subscriptionservice.repository.PlanRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;

    @Autowired
    public PlanServiceImpl(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @PostConstruct
    public void seedInitialPlans() {
        if (planRepository.count() == 0) {
            planRepository.save(new Plan(null, "Basic Monthly", "Standard access for individual subscribers", new BigDecimal("199.00"), 30, true));
            planRepository.save(new Plan(null, "Pro Quarterly", "Advanced features & multi-device sync for power users", new BigDecimal("499.00"), 90, true));
            planRepository.save(new Plan(null, "Enterprise Annual", "Unlimited access with 24/7 priority support", new BigDecimal("1499.00"), 365, true));
        }
    }

    @Override
    public List<PlanDto> getAllActivePlans() {
        return planRepository.findByActiveTrue()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PlanDto> getAllPlans() {
        return planRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public PlanDto getPlanById(Long id) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with ID: " + id));
        return mapToDto(plan);
    }

    @Override
    public PlanDto createPlan(PlanDto planDto) {
        Plan plan = new Plan();
        plan.setName(planDto.getName().trim());
        plan.setDescription(planDto.getDescription() != null ? planDto.getDescription().trim() : "");
        plan.setPrice(planDto.getPrice());
        plan.setDurationDays(planDto.getDurationDays());
        plan.setActive(planDto.isActive());

        Plan savedPlan = planRepository.save(plan);
        return mapToDto(savedPlan);
    }

    @Override
    public PlanDto updatePlan(Long id, PlanDto planDto) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with ID: " + id));

        plan.setName(planDto.getName().trim());
        plan.setDescription(planDto.getDescription() != null ? planDto.getDescription().trim() : "");
        plan.setPrice(planDto.getPrice());
        plan.setDurationDays(planDto.getDurationDays());
        plan.setActive(planDto.isActive());

        Plan updatedPlan = planRepository.save(plan);
        return mapToDto(updatedPlan);
    }

    @Override
    public void deletePlan(Long id) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with ID: " + id));
        planRepository.delete(plan);
    }

    private PlanDto mapToDto(Plan plan) {
        return new PlanDto(
                plan.getId(),
                plan.getName(),
                plan.getDescription(),
                plan.getPrice(),
                plan.getDurationDays(),
                plan.isActive()
        );
    }
}
