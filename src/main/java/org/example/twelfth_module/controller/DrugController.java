package org.example.twelfth_module.controller;

import org.example.twelfth_module.entity.Drug;
import org.example.twelfth_module.mapper.DrugMapper;
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

    @GetMapping("/{id}")
    public Map<String, Object> getDrugById(@PathVariable Long id) {
        Drug drug = drugMapper.selectById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", drug);
        return result;
    }
}
