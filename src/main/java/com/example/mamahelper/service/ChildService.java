package com.example.mamahelper.service;

import com.example.mamahelper.entity.Child;
import com.example.mamahelper.repository.ChildRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ChildService {

    private final ChildRepository childRepository;

    public ChildService(ChildRepository childRepository) {
        this.childRepository = childRepository;
    }

    public Child createChild(Child child) {
        return childRepository.save(child);
    }

    public Optional<Child> getChildById(Long id) {
        return childRepository.findById(id);
    }

    public Optional<Child> updateChildInfo(Long id, Child updatedChildInfo) {
        return childRepository.findById(id)
                .map(child -> {
                    child.setName(updatedChildInfo.getName());
                    child.setDateOfBirth(updatedChildInfo.getDateOfBirth());

                    return childRepository.save(child);
                });
    }
}
