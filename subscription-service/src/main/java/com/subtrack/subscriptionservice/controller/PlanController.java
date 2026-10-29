package com.subtrack.subscriptionservice.controller;

import com.subtrack.subscriptionservice.dto.PlanDto;
import com.subtrack.subscriptionservice.service.PlanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
public class PlanController {

    private final PlanService planService;

    @Autowired
    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping
    public ResponseEntity<List<PlanDto>> getActivePlans() {
        return ResponseEntity.ok(planService.getAllActivePlans());
    }

    @GetMapping("/all")
    public ResponseEntity<List<PlanDto>> getAllPlans() {
        return ResponseEntity.ok(planService.getAllPlans());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanDto> getPlanById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(planService.getPlanById(id));
    }

    @PostMapping
    public ResponseEntity<PlanDto> createPlan(@Valid @RequestBody PlanDto planDto) {
        PlanDto createdPlan = planService.createPlan(planDto);
        return new ResponseEntity<>(createdPlan, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlanDto> updatePlan(@PathVariable("id") Long id, @Valid @RequestBody PlanDto planDto) {
        PlanDto updatedPlan = planService.updatePlan(id, planDto);
        return ResponseEntity.ok(updatedPlan);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable("id") Long id) {
        planService.deletePlan(id);
        return ResponseEntity.noContent().build();
    }
}
