package com.bankapp.bankingapp.infrastructure.config;

import com.bankapp.bankingapp.application.interfaces.repository.PermissionRepository;
import com.bankapp.bankingapp.application.interfaces.repository.RoleRepository;
import com.bankapp.bankingapp.application.interfaces.repository.UserRepository;
import com.bankapp.bankingapp.domain.model.Permission;
import com.bankapp.bankingapp.domain.model.Role;
import com.bankapp.bankingapp.domain.model.User;
import com.bankapp.bankingapp.domain.model.enums.UserStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    @org.springframework.beans.factory.annotation.Value("${app.admin.email}")
    private String adminEmail;

    @org.springframework.beans.factory.annotation.Value("${app.admin.password}")
    private String adminPassword;

    public DataInitializer(UserRepository userRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        logger.info("🚀 Starting data initialization...");

        try {
            // Create Roles
            Role userRole = createRoleIfNotExists("USER", "Standard user role");
            Role adminRole = createRoleIfNotExists("ADMIN", "Administrator role");

            // Create Permissions
            Permission readPermission = createPermissionIfNotExists("READ", "Read access");
            Permission writePermission = createPermissionIfNotExists("WRITE", "Write access");
            Permission deletePermission = createPermissionIfNotExists("DELETE", "Delete access");
            Permission manageUsersPermission = createPermissionIfNotExists("MANAGE_USERS", "Manage users");
            Permission manageAccountsPermission = createPermissionIfNotExists("MANAGE_ACCOUNTS", "Manage accounts");
            Permission approveTransactionsPermission = createPermissionIfNotExists("APPROVE_TRANSACTIONS",
                    "Approve transactions");

            // Assign permissions to roles
            userRole.addPermission(readPermission);
            userRole.addPermission(writePermission);
            roleRepository.save(userRole);

            adminRole.addPermission(readPermission);
            adminRole.addPermission(writePermission);
            adminRole.addPermission(deletePermission);
            adminRole.addPermission(manageUsersPermission);
            adminRole.addPermission(manageAccountsPermission);
            adminRole.addPermission(approveTransactionsPermission);
            roleRepository.save(adminRole);

            // Create Admin User
            createAdminUserIfNotExists(adminRole);

            logger.info("✅ Data initialization completed successfully!");
            logger.info("📧 Admin account email: {}", adminEmail);

        } catch (Exception e) {
            logger.error("❌ Error during data initialization: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize data", e);
        }
    }

    private Role createRoleIfNotExists(String roleName, String description) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    logger.info("Creating role: {}", roleName);
                    Role role = new Role(null, roleName);
                    return roleRepository.save(role);
                });
    }

    private Permission createPermissionIfNotExists(String permissionName, String description) {
        return permissionRepository.findByName(permissionName)
                .orElseGet(() -> {
                    logger.info("Creating permission: {}", permissionName);
                    Permission permission = new Permission(null, permissionName, description);
                    return permissionRepository.save(permission);
                });
    }

    private void createAdminUserIfNotExists(Role adminRole) {
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            logger.info("Creating admin user: {}", adminEmail);

            String encodedPassword = passwordEncoder.encode(adminPassword);

            User adminUser = new User(
                    null,
                    "admin",
                    adminEmail,
                    encodedPassword,
                    null,
                    UserStatus.ACTIVE); // Admin luôn ACTIVE ngay (không cần xác thực email)

            adminUser.addRole(adminRole);
            userRepository.save(adminUser);

            logger.info("Admin user created successfully!");
        } else {
            logger.info("Admin user already exists, skipping creation");
        }
    }
}
