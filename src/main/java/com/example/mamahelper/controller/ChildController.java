package com.example.mamahelper.controller;

import com.example.mamahelper.controller.dto.ChildRequest;
import com.example.mamahelper.controller.dto.ChildResponse;
import com.example.mamahelper.service.ChildService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/child")
public class ChildController {

    private final ChildService childService;

    public ChildController(ChildService childService) {
        this.childService = childService;
    }

    @PostMapping
    public ChildResponse createChild(@Valid @RequestBody ChildRequest request) {
        return ChildResponse.from(childService.createChild(request.toEntity()));
    }

    @GetMapping
    public List<ChildResponse> getAllChildren() {
        return childService.getAllChildren().stream()
                .map(ChildResponse::from)
                .toList();
    }

    @GetMapping("/{childId}")
    public ResponseEntity<ChildResponse> getChildById(@PathVariable Long childId) {
        return ResponseEntity.ok(ChildResponse.from(childService.getChildById(childId)));
    }

    @PutMapping("/{childId}")
    public ResponseEntity<ChildResponse> updateChild(@PathVariable Long childId,
                                                     @Valid @RequestBody ChildRequest request) {
        return ResponseEntity.ok(ChildResponse.from(childService.updateChildInfo(childId, request.toEntity())));
    }

    @DeleteMapping("/{childId}")
    public ResponseEntity<Void> deleteChild(@PathVariable Long childId) {
        childService.deleteChild(childId);

        return ResponseEntity.noContent().build();
    }
}
