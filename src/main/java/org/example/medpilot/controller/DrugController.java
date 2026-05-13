package org.example.medpilot.controller;

import org.example.medpilot.entity.Drug;
import org.example.medpilot.mapper.DrugMapper;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drugs")
@CrossOrigin(origins = "*")
public class DrugController {

    private final DrugMapper drugMapper;

    public DrugController(DrugMapper drugMapper) {
        this.drugMapper = drugMapper;
    }

    @GetMapping
    public Map<String, Object> getAllDrugs() {
        List<Drug> drugs = drugMapper.selectAll();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", drugs);
        result.put("total", drugs.size());
        return result;
    }

}
