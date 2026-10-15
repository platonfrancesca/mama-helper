package com.example.mamahelper.controller;

import com.example.mamahelper.entity.Child;
import com.example.mamahelper.service.ChildService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/child")
public class ChildController {

    private final ChildService childService;

    public ChildController(ChildService childService) {
        this.childService = childService;
    }

    @PostMapping
    public Child createChild(@Valid @RequestBody Child child) {
        return childService.createChild(child);
    }

    @GetMapping("/{childId}")
    public ResponseEntity<Child> getChildById(@PathVariable Long childId) {
        return childService.getChildById(childId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{childId}")
    public ResponseEntity<Child> updateReminder(@PathVariable Long childId,
                                                @Valid @RequestBody Child updatedChildInfo) {
        return childService.updateChildInfo(childId, updatedChildInfo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
