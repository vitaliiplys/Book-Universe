package com.example.onlinebookstore.service.role;

import com.example.onlinebookstore.exception.EntityNotFoundException;
import com.example.onlinebookstore.model.Role;
import com.example.onlinebookstore.repository.role.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;

    @Override
    public Role findByName(Role.RoleName role) {
        return roleRepository.findByName(role).orElseThrow(
                () -> new EntityNotFoundException("Can`t find role" + role)
        );
    }
}
