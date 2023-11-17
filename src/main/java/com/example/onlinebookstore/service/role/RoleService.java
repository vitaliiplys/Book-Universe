package com.example.onlinebookstore.service.role;

import com.example.onlinebookstore.model.Role;

public interface RoleService {
    Role findByName(Role.RoleName name);
}
