package org.example.medpilot.controller;

import org.example.medpilot.entity.MedicationPlan;
import org.example.medpilot.mapper.MedicationPlanMapper;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plans")
@CrossOrigin(origins = "*")
public class PlanController {

    private final MedicationPlanMapper medicationPlanMapper;

    public PlanController(MedicationPlanMapper medicationPlanMapper) {
        this.medicationPlanMapper = medicationPlanMapper;
    }

    @GetMapping
    public Map<String, Object> getActivePlans(@RequestParam Long userId) {
        List<MedicationPlan> plans = medicationPlanMapper.selectActivePlansByUserId(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", plans);
        result.put("total", plans.size());
        return result;
    }
}
