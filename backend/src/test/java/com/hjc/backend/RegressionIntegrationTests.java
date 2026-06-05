package com.hjc.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.io.ByteArrayInputStream;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "cesd.upload.root-dir=target/test-uploads",
        "cesd.upload.max-file-size=8",
        "cesd.upload.allowed-extensions=pdf,png"
})
@AutoConfigureMockMvc
@Transactional
class RegressionIntegrationTests {

    private static final String PASSWORD = "123456";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String suffix = "it" + System.nanoTime();

    private Long studentRoleId;

    private Long reviewerRoleId;

    private Long adminRoleId;

    @BeforeEach
    void setUpPermissions() {
        ensureLogTables();
        studentRoleId = ensureRole("STUDENT");
        reviewerRoleId = ensureRole("REVIEWER");
        adminRoleId = ensureRole("ADMIN");

        grant(studentRoleId, "menu:student:applications", "MENU", "/student/applications");
        grant(studentRoleId, "menu:student:application-create", "MENU", "/student/applications/create");
        grant(studentRoleId, "menu:student:score", "MENU", "/student/score");
        grant(studentRoleId, "student:application:view", "BUTTON", null);
        grant(studentRoleId, "student:application:create", "BUTTON", null);
        grant(studentRoleId, "student:application:submit", "BUTTON", null);
        grant(studentRoleId, "student:application:withdraw", "BUTTON", null);
        grant(studentRoleId, "student:score:view", "BUTTON", null);

        grant(reviewerRoleId, "menu:audit:pending", "MENU", "/audit/pending");
        grant(reviewerRoleId, "audit:pending:view", "BUTTON", null);
        grant(reviewerRoleId, "audit:application:approve", "BUTTON", null);
        grant(reviewerRoleId, "audit:application:reject", "BUTTON", null);

        grant(adminRoleId, "menu:admin:scores", "MENU", "/admin/scores");
        grant(adminRoleId, "admin:score:recalculate", "BUTTON", null);
        grant(adminRoleId, "admin:score:export", "BUTTON", null);
        grant(adminRoleId, "admin:material:export", "BUTTON", null);
        grant(adminRoleId, "admin:user:view", "BUTTON", null);
        grant(adminRoleId, "admin:user:create", "BUTTON", null);
        grant(adminRoleId, "admin:user:update", "BUTTON", null);
        grant(adminRoleId, "admin:user:delete", "BUTTON", null);
        grant(adminRoleId, "admin:user:reset-password", "BUTTON", null);
        grant(adminRoleId, "admin:permission:view", "BUTTON", null);
        grant(adminRoleId, "admin:system-config:view", "BUTTON", null);
        grant(adminRoleId, "admin:system-config:create", "BUTTON", null);
        grant(adminRoleId, "admin:system-config:update", "BUTTON", null);
        grant(adminRoleId, "admin:system-config:delete", "BUTTON", null);
        grant(adminRoleId, "admin:operation-log:view", "BUTTON", null);
        grant(adminRoleId, "admin:login-log:view", "BUTTON", null);
    }

    @Test
    void authenticationAndRolePermissionBoundaries() throws Exception {
        Long majorId = insertMajor("auth");
        Long classId = insertClass("auth", majorId, "2026");
        Long studentUserId = insertUser("student-auth", studentRoleId);
        insertStudent("student-auth", studentUserId, majorId, classId, "2026");
        Long reviewerUserId = insertUser("reviewer-auth", reviewerRoleId);
        insertReviewerScope(reviewerUserId, majorId, null, null);
        insertUser("admin-auth", adminRoleId);

        String studentToken = login("student-auth");
        String reviewerToken = login("reviewer-auth");
        String adminToken = login("admin-auth");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username("student-auth"), "password", "bad-password"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));

        mockMvc.perform(get("/api/frontend/my-applications/page"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        mockMvc.perform(get("/api/frontend/my-applications/page")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        mockMvc.perform(get("/api/auth/permissions").with(auth(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.permissionCodes.length()").value(greaterThan(0)))
                .andExpect(jsonPath("$.data.menus.length()").value(greaterThan(0)))
                .andExpect(jsonPath("$.data.buttons.length()").value(greaterThan(0)));

        mockMvc.perform(get("/api/frontend/my-applications/page").with(auth(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/frontend/audit/pending/page").with(auth(studentToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(get("/api/users/page").with(auth(reviewerToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(get("/api/scores/page").with(auth(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void adminCanQueryLogsAndStudentIsRejected() throws Exception {
        insertUser("log-user", studentRoleId);
        String token = login("log-user");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username("log-user"), "password", "bad-password"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));

        mockMvc.perform(post("/api/auth/logout").with(auth(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        org.assertj.core.api.Assertions.assertThat(countLoginLogs(username("log-user"), "LOGIN", "SUCCESS")).isGreaterThan(0);
        org.assertj.core.api.Assertions.assertThat(countLoginLogs(username("log-user"), "LOGIN", "FAIL")).isGreaterThan(0);
        org.assertj.core.api.Assertions.assertThat(countLoginLogs(username("log-user"), "LOGOUT", "SUCCESS")).isGreaterThan(0);

        String adminToken = loginForNewAdmin("log-admin");

        mockMvc.perform(get("/api/login-logs/page").with(auth(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/operation-logs/page").with(auth(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/login-logs/page").with(auth(token)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(get("/api/operation-logs/page").with(auth(token)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void currentUserCanChangePasswordAndLogoutEndpointRequiresLogin() throws Exception {
        insertUser("self-password", studentRoleId);
        String token = login("self-password");
        String newPassword = "NewPwd123!";

        mockMvc.perform(put("/api/auth/password")
                        .with(auth(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "oldPassword", PASSWORD,
                                "newPassword", newPassword,
                                "confirmPassword", newPassword))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username("self-password"), "password", PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));

        String newToken = loginWithPassword("self-password", newPassword);

        mockMvc.perform(put("/api/auth/password")
                        .with(auth(newToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "oldPassword", "wrong-password",
                                "newPassword", "AnotherPwd123!",
                                "confirmPassword", "AnotherPwd123!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));

        mockMvc.perform(put("/api/auth/password")
                        .with(auth(newToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "oldPassword", newPassword,
                                "newPassword", "NextPwd123!",
                                "confirmPassword", "MismatchPwd123!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(200)));

        mockMvc.perform(put("/api/auth/password")
                        .with(auth(newToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "oldPassword", newPassword,
                                "newPassword", newPassword,
                                "confirmPassword", newPassword))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(200)));

        mockMvc.perform(put("/api/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "oldPassword", newPassword,
                                "newPassword", "NoTokenPwd123!",
                                "confirmPassword", "NoTokenPwd123!"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        mockMvc.perform(post("/api/auth/logout").with(auth(newToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void adminCanManageUsersAndSystemConfigsWhileOtherRolesAreRejected() throws Exception {
        Long majorId = insertMajor("admin-page");
        Long classId = insertClass("admin-page", majorId, "2026");
        Long studentUserId = insertUser("student-admin-page", studentRoleId);
        insertStudent("admin-page", studentUserId, majorId, classId, "2026");
        Long reviewerUserId = insertUser("reviewer-admin-page", reviewerRoleId);
        insertReviewerScope(reviewerUserId, majorId, null, null);
        String studentToken = login("student-admin-page");
        String reviewerToken = login("reviewer-admin-page");
        String adminToken = loginForNewAdmin("admin-page");

        mockMvc.perform(get("/api/users/page").with(auth(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].passwordHash").doesNotExist());

        mockMvc.perform(get("/api/users/page").with(auth(studentToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(get("/api/users/page").with(auth(reviewerToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));

        String managedUsernameSeed = "managed-user";
        String createdResponse = mockMvc.perform(post("/api/users")
                        .with(auth(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", username(managedUsernameSeed),
                                "passwordHash", "initial123",
                                "realName", "Managed User",
                                "roleId", studentRoleId,
                                "status", 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long managedUserId = objectMapper.readTree(createdResponse).path("data").path("id").asLong();
        loginWithPassword(managedUsernameSeed, "initial123");

        mockMvc.perform(put("/api/users/{id}/password/reset", managedUserId)
                        .with(auth(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("newPassword", "reset123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());
        loginWithPassword(managedUsernameSeed, "reset123");
        org.assertj.core.api.Assertions.assertThat(countOperationLogs("USER", "RESET_PASSWORD")).isGreaterThan(0);
        assertOperationLogsDoNotContainSensitiveValues();

        mockMvc.perform(get("/api/system-configs/page").with(auth(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/api/system-configs")
                        .with(auth(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "configKey", "it.student.denied." + suffix,
                                "configValue", "1",
                                "description", "denied",
                                "status", 1))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(post("/api/system-configs")
                        .with(auth(reviewerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "configKey", "it.reviewer.denied." + suffix,
                                "configValue", "1",
                                "description", "denied",
                                "status", 1))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));

        String configResponse = mockMvc.perform(post("/api/system-configs")
                        .with(auth(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "configKey", "it.config." + suffix,
                                "configValue", "enabled",
                                "description", "integration config",
                                "status", 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long configId = objectMapper.readTree(configResponse).path("data").path("id").asLong();

        mockMvc.perform(put("/api/system-configs/{id}", configId)
                        .with(auth(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "configKey", "it.config." + suffix,
                                "configValue", "disabled",
                                "description", "updated integration config",
                                "status", 0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.configValue").value("disabled"));

        mockMvc.perform(delete("/api/system-configs/{id}", configId).with(auth(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void studentCanCreateSubmitAndWithdrawMaterial() throws Exception {
        TestDataset dataset = createStudentDataset("flow", "2026");
        String studentToken = login("student-flow");

        Long materialId = createMaterial(studentToken, dataset.itemId(), "Flow Material", new BigDecimal("6.50"));

        mockMvc.perform(post("/api/material-applications/{id}/submit", materialId).with(auth(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.data.submitCount").value(1));

        mockMvc.perform(post("/api/material-applications/{id}/withdraw", materialId)
                        .with(auth(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("reason", "integration withdraw"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    void materialAttachmentUploadDownloadAndDeleteRules() throws Exception {
        TestDataset ownerDataset = createStudentDataset("attach-owner", "2026");
        TestDataset otherDataset = createStudentDataset("attach-other", "2026");
        String ownerToken = login("student-attach-owner");
        String otherToken = login("student-attach-other");

        Long materialId = createMaterial(ownerToken, ownerDataset.itemId(), "Attachment Material", new BigDecimal("6.50"));
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "evidence.pdf",
                "application/pdf",
                new byte[]{1, 2, 3, 4});

        String uploadResponse = mockMvc.perform(multipart("/api/material-attachments/upload")
                        .file(file)
                        .param("materialId", materialId.toString())
                        .with(auth(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.originalName").value("evidence.pdf"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long attachmentId = objectMapper.readTree(uploadResponse).path("data").path("id").asLong();

        mockMvc.perform(get("/api/material-attachments/{id}/download", attachmentId).with(auth(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("evidence.pdf")));

        mockMvc.perform(multipart("/api/material-attachments/upload")
                        .file(new MockMultipartFile("file", "other.pdf", "application/pdf", new byte[]{1, 2, 3}))
                        .param("materialId", materialId.toString())
                        .with(auth(otherToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(multipart("/api/material-attachments/upload")
                        .file(new MockMultipartFile("file", "bad.exe", "application/octet-stream", new byte[]{1}))
                        .param("materialId", materialId.toString())
                        .with(auth(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(200)));

        mockMvc.perform(multipart("/api/material-attachments/upload")
                        .file(new MockMultipartFile("file", "large.pdf", "application/pdf", new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9}))
                        .param("materialId", materialId.toString())
                        .with(auth(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(200)));

        Long submittedMaterialId = createMaterial(ownerToken, ownerDataset.itemId(), "Submitted Attachment Material", new BigDecimal("6.50"));
        submit(ownerToken, submittedMaterialId);
        mockMvc.perform(multipart("/api/material-attachments/upload")
                        .file(new MockMultipartFile("file", "submitted.pdf", "application/pdf", new byte[]{1, 2, 3}))
                        .param("materialId", submittedMaterialId.toString())
                        .with(auth(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(200)));

        mockMvc.perform(delete("/api/material-attachments/{id}", attachmentId).with(auth(otherToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(delete("/api/material-attachments/{id}", attachmentId).with(auth(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(multipart("/api/material-attachments/upload")
                        .file(new MockMultipartFile("file", "anonymous.pdf", "application/pdf", new byte[]{1, 2, 3}))
                        .param("materialId", materialId.toString()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void reviewerCanApproveRejectAndInvalidStateTransitionsAreBlocked() throws Exception {
        TestDataset dataset = createStudentDataset("review", "2026");
        Long reviewerUserId = insertUser("reviewer-review", reviewerRoleId);
        insertReviewerScope(reviewerUserId, dataset.majorId(), null, null);

        String studentToken = login("student-review");
        String reviewerToken = login("reviewer-review");

        Long approveMaterialId = createMaterial(studentToken, dataset.itemId(), "Approve Material", new BigDecimal("8.00"));
        Long rejectMaterialId = createMaterial(studentToken, dataset.itemId(), "Reject Material", new BigDecimal("5.00"));
        submit(studentToken, approveMaterialId);
        submit(studentToken, rejectMaterialId);

        mockMvc.perform(post("/api/material-applications/{id}/approve", approveMaterialId)
                        .with(auth(reviewerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("reviewScore", 8.75, "reviewComment", "approved by integration test"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("APPROVED"))
                .andExpect(jsonPath("$.data.finalScore").value(8.75));
        org.assertj.core.api.Assertions.assertThat(countOperationLogs("MATERIAL_REVIEW", "APPROVE")).isGreaterThan(0);

        Integer approveRecordCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM review_record WHERE material_id = ? AND reviewer_id = ? AND review_result = 'APPROVED'",
                Integer.class,
                approveMaterialId,
                reviewerUserId);
        org.assertj.core.api.Assertions.assertThat(approveRecordCount).isEqualTo(1);

        mockMvc.perform(post("/api/material-applications/{id}/approve", approveMaterialId)
                        .with(auth(reviewerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("reviewScore", 9.00))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(200)));

        mockMvc.perform(post("/api/material-applications/{id}/reject", rejectMaterialId)
                        .with(auth(reviewerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("rejectReason", "missing proof", "reviewComment", "please resubmit"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("REJECTED"))
                .andExpect(jsonPath("$.data.rejectReason").value("missing proof"));

        mockMvc.perform(post("/api/material-applications/{id}/withdraw", rejectMaterialId)
                        .with(auth(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(200)))
                .andExpect(jsonPath("$.message").value(containsString("withdraw")));

        mockMvc.perform(post("/api/material-applications/{id}/approve", rejectMaterialId)
                        .with(auth(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("reviewScore", 3.00))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void approveAutomaticallyRecalculatesStudentScoreAndRejectDoesNotIncreaseScore() throws Exception {
        Long majorId = insertMajor("auto-score");
        Long classId = insertClass("auto-score", majorId, "2026");
        Long categoryId = insertCategory("auto-score", new BigDecimal("10.00"));
        Long itemA = insertItem("auto-score-a", categoryId, new BigDecimal("10.00"));
        Long itemB = insertItem("auto-score-b", categoryId, new BigDecimal("10.00"));
        Long studentId = insertStudent("auto-score", insertUser("student-auto-score", studentRoleId), majorId, classId, "2026");
        Long reviewerUserId = insertUser("reviewer-auto-score", reviewerRoleId);
        insertReviewerScope(reviewerUserId, majorId, null, null);

        String studentToken = login("student-auto-score");
        String reviewerToken = login("reviewer-auto-score");

        Long firstMaterialId = createMaterial(studentToken, itemA, "Auto Score First", new BigDecimal("6.00"));
        Long secondMaterialId = createMaterial(studentToken, itemB, "Auto Score Second", new BigDecimal("8.00"));
        Long rejectedMaterialId = createMaterial(studentToken, itemA, "Auto Score Rejected", new BigDecimal("9.00"));
        submit(studentToken, firstMaterialId);
        submit(studentToken, secondMaterialId);
        submit(studentToken, rejectedMaterialId);

        mockMvc.perform(post("/api/material-applications/{id}/approve", firstMaterialId)
                        .with(auth(reviewerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("reviewScore", 6.00, "reviewComment", "auto recalculate first"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        assertScore(studentId, "6.00", 1, 1);
        assertCategoryScore(studentId, categoryId, "6.00");

        mockMvc.perform(get("/api/frontend/my-score").with(auth(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalScore").value(6.0));

        mockMvc.perform(post("/api/material-applications/{id}/approve", secondMaterialId)
                        .with(auth(reviewerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("reviewScore", 8.00, "reviewComment", "auto recalculate second"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        assertScore(studentId, "10.00", 1, 1);
        assertCategoryScore(studentId, categoryId, "10.00");

        mockMvc.perform(post("/api/material-applications/{id}/reject", rejectedMaterialId)
                        .with(auth(reviewerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("rejectReason", "not counted", "reviewComment", "reject should not recalculate up"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("REJECTED"));

        assertScore(studentId, "10.00", 1, 1);
        assertCategoryScore(studentId, categoryId, "10.00");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void approveRollsBackWhenAutomaticScoreRecalculationFails() throws Exception {
        Long majorId = null;
        Long classId = null;
        Long categoryId = null;
        Long itemId = null;
        Long studentUserId = null;
        Long studentId = null;
        Long reviewerUserId = null;
        Long materialId = null;
        try {
            majorId = insertMajor("auto-score-rollback");
            classId = insertClass("auto-score-rollback", majorId, "2026");
            categoryId = insertCategory("auto-score-rollback", new BigDecimal("100.00"));
            itemId = insertItem("auto-score-rollback", categoryId, new BigDecimal("10.00"));
            studentUserId = insertUser("student-auto-score-rollback", studentRoleId);
            studentId = insertStudent("auto-score-rollback", studentUserId, majorId, classId, "2026");
            reviewerUserId = insertUser("reviewer-auto-score-rollback", reviewerRoleId);
            insertReviewerScope(reviewerUserId, majorId, null, null);

            String studentToken = login("student-auto-score-rollback");
            String reviewerToken = login("reviewer-auto-score-rollback");
            materialId = createMaterial(studentToken, itemId, "Auto Score Rollback", new BigDecimal("7.00"));
            submit(studentToken, materialId);

            jdbcTemplate.update("UPDATE evaluation_item SET deleted = 1 WHERE id = ?", itemId);

            mockMvc.perform(post("/api/material-applications/{id}/approve", materialId)
                            .with(auth(reviewerToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(Map.of("reviewScore", 7.00, "reviewComment", "force score recalculation failure"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)))
                    .andExpect(jsonPath("$.message").value(containsString("Evaluation item")));

            Map<String, Object> material = jdbcTemplate.queryForMap(
                    "SELECT status, final_score, review_time FROM material_application WHERE id = ?",
                    materialId);
            org.assertj.core.api.Assertions.assertThat(material.get("status")).isEqualTo("SUBMITTED");
            org.assertj.core.api.Assertions.assertThat(material.get("final_score")).isNull();
            org.assertj.core.api.Assertions.assertThat(material.get("review_time")).isNull();

            Integer reviewRecordCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM review_record WHERE material_id = ?",
                    Integer.class,
                    materialId);
            org.assertj.core.api.Assertions.assertThat(reviewRecordCount).isZero();

            Integer scoreSummaryCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM score_summary WHERE student_id = ? AND deleted = 0",
                    Integer.class,
                    studentId);
            org.assertj.core.api.Assertions.assertThat(scoreSummaryCount).isZero();
        } finally {
            cleanupRollbackScenario(materialId, studentId, reviewerUserId, studentUserId, itemId, categoryId, classId, majorId);
        }
    }

    @Test
    void manualScoreRecalculationUpdatesTotalsAndCompetitionRanks() throws Exception {
        Long majorId = insertMajor("score");
        Long classId = insertClass("score", majorId, "2026");
        Long categoryId = insertCategory("score", new BigDecimal("100.00"));
        Long itemA = insertItem("score-a", categoryId, new BigDecimal("100.00"));
        Long itemB = insertItem("score-b", categoryId, new BigDecimal("100.00"));

        Long studentOne = insertStudent("score-one", insertUser("student-score-one", studentRoleId), majorId, classId, "2026");
        Long studentTwo = insertStudent("score-two", insertUser("student-score-two", studentRoleId), majorId, classId, "2026");
        Long studentThree = insertStudent("score-three", insertUser("student-score-three", studentRoleId), majorId, classId, "2026");
        insertUser("admin-score", adminRoleId);
        String adminToken = login("admin-score");

        insertMaterial(studentOne, itemA, "APPROVED", new BigDecimal("70.00"), new BigDecimal("70.00"));
        insertMaterial(studentOne, itemB, "APPROVED", new BigDecimal("60.00"), new BigDecimal("60.00"));
        insertMaterial(studentTwo, itemA, "APPROVED", new BigDecimal("100.00"), new BigDecimal("100.00"));
        insertMaterial(studentThree, itemA, "APPROVED", new BigDecimal("50.00"), new BigDecimal("50.00"));
        insertMaterial(studentThree, itemB, "SUBMITTED", new BigDecimal("99.00"), null);

        mockMvc.perform(post("/api/scores/classes/{classId}/recalculate", classId).with(auth(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        assertScore(studentOne, "100.00", 1, 1);
        assertScore(studentTwo, "100.00", 1, 1);
        assertScore(studentThree, "50.00", 3, 3);

        mockMvc.perform(get("/api/scores/classes/{classId}/ranking", classId).with(auth(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].classRank").value(1))
                .andExpect(jsonPath("$.data[1].classRank").value(1))
                .andExpect(jsonPath("$.data[2].classRank").value(3));

        mockMvc.perform(get("/api/scores/majors/{majorId}/ranking", majorId).with(auth(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].majorRank").value(1))
                .andExpect(jsonPath("$.data[1].majorRank").value(1))
                .andExpect(jsonPath("$.data[2].majorRank").value(3));
    }

    @Test
    void adminCanExportExcelAndNonAdminsAreRejected() throws Exception {
        TestDataset dataset = createStudentDataset("export", "2026");
        String studentToken = login("student-export");
        Long reviewerUserId = insertUser("reviewer-export", reviewerRoleId);
        insertReviewerScope(reviewerUserId, dataset.majorId(), null, null);
        String reviewerToken = login("reviewer-export");
        String adminToken = loginForNewAdmin("admin-export");

        Long materialId = createMaterial(studentToken, dataset.itemId(), "Export Material", new BigDecimal("9.00"));
        submit(studentToken, materialId);
        mockMvc.perform(post("/api/material-applications/{id}/approve", materialId)
                        .with(auth(reviewerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("reviewScore", 9.00, "reviewComment", "export score"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        byte[] scoreBytes = expectExcel("/api/scores/export", adminToken, "score-summary");
        assertWorkbookHasHeader(scoreBytes);
        org.assertj.core.api.Assertions.assertThat(countOperationLogs("EXPORT", "SCORE_SUMMARY")).isGreaterThan(0);

        mockMvc.perform(get("/api/scores/export").with(auth(studentToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(get("/api/scores/export").with(auth(reviewerToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));

        assertWorkbookHasHeader(expectExcel("/api/scores/classes/" + dataset.classId() + "/ranking/export", adminToken, "class-ranking"));
        assertWorkbookHasHeader(expectExcel("/api/scores/majors/" + dataset.majorId() + "/ranking/export", adminToken, "major-ranking"));
        assertWorkbookHasHeader(expectExcel("/api/material-applications/export", adminToken, "material-applications"));
        assertWorkbookHasHeader(expectExcel("/api/material-applications/export?keyword=NO_SUCH_EXPORT_DATA", adminToken, "material-applications"));
    }

    @Test
    void reviewerScopeFiltersPendingApplicationsConservatively() throws Exception {
        Long majorA = insertMajor("scope-a");
        Long majorB = insertMajor("scope-b");
        Long classA = insertClass("scope-a", majorA, "2026");
        Long classB = insertClass("scope-b", majorB, "2027");
        Long categoryId = insertCategory("scope", new BigDecimal("100.00"));
        Long itemId = insertItem("scope", categoryId, new BigDecimal("10.00"));
        Long studentA = insertStudent("scope-a", insertUser("student-scope-a", studentRoleId), majorA, classA, "2026");
        Long studentB = insertStudent("scope-b", insertUser("student-scope-b", studentRoleId), majorB, classB, "2027");
        insertMaterial(studentA, itemId, "SUBMITTED", new BigDecimal("10.00"), null);
        insertMaterial(studentB, itemId, "SUBMITTED", new BigDecimal("10.00"), null);

        insertUser("reviewer-scope-none", reviewerRoleId);
        String reviewerToken = login("reviewer-scope-none");
        String adminToken = loginForNewAdmin("admin-scope");

        mockMvc.perform(get("/api/frontend/audit/pending/page").with(auth(reviewerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(0));

        Long majorReviewerId = insertUser("reviewer-scope-major", reviewerRoleId);
        insertReviewerScope(majorReviewerId, majorA, null, null);
        mockMvc.perform(get("/api/frontend/audit/pending/page").with(auth(login("reviewer-scope-major"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].studentId").value(studentA));

        Long classReviewerId = insertUser("reviewer-scope-class", reviewerRoleId);
        insertReviewerScope(classReviewerId, null, classB, null);
        mockMvc.perform(get("/api/frontend/audit/pending/page").with(auth(login("reviewer-scope-class"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].studentId").value(studentB));

        Long gradeReviewerId = insertUser("reviewer-scope-grade", reviewerRoleId);
        insertReviewerScope(gradeReviewerId, null, null, "2026");
        mockMvc.perform(get("/api/frontend/audit/pending/page").with(auth(login("reviewer-scope-grade"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].studentId").value(studentA));

        Long emptyReviewerId = insertUser("reviewer-scope-empty", reviewerRoleId);
        insertReviewerScope(emptyReviewerId, null, null, null);
        mockMvc.perform(get("/api/frontend/audit/pending/page").with(auth(login("reviewer-scope-empty"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));

        mockMvc.perform(get("/api/frontend/audit/pending/page").with(auth(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(greaterThanOrEqualTo(2)));
    }

    private String loginForNewAdmin(String usernameSeed) throws Exception {
        insertUser(usernameSeed, adminRoleId);
        return login(usernameSeed);
    }

    private TestDataset createStudentDataset(String seed, String grade) {
        Long majorId = insertMajor(seed);
        Long classId = insertClass(seed, majorId, grade);
        Long studentUserId = insertUser("student-" + seed, studentRoleId);
        Long studentId = insertStudent(seed, studentUserId, majorId, classId, grade);
        Long categoryId = insertCategory(seed, new BigDecimal("100.00"));
        Long itemId = insertItem(seed, categoryId, new BigDecimal("10.00"));
        return new TestDataset(majorId, classId, studentId, categoryId, itemId);
    }

    private Long ensureRole(String roleCode) {
        Long existing = queryLong("SELECT id FROM sys_role WHERE role_code = ? AND deleted = 0", roleCode);
        if (existing != null) {
            return existing;
        }
        return insertAndReturnId(
                "INSERT INTO sys_role (role_name, role_code, description, status, deleted) VALUES (?, ?, ?, 1, 0)",
                roleCode + " Role",
                roleCode,
                "Integration test role");
    }

    private void grant(Long roleId, String code, String type, String routePath) {
        Long permissionId = ensurePermission(code, type, routePath);
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_role_permission WHERE role_id = ? AND permission_id = ? AND deleted = 0",
                Integer.class,
                roleId,
                permissionId);
        if (count == null || count == 0) {
            jdbcTemplate.update(
                    "INSERT INTO sys_role_permission (role_id, permission_id, deleted) VALUES (?, ?, 0)",
                    roleId,
                    permissionId);
        }
    }

    private Long ensurePermission(String code, String type, String routePath) {
        Long existing = queryLong("SELECT id FROM sys_permission WHERE permission_code = ? AND deleted = 0", code);
        if (existing != null) {
            return existing;
        }
        return insertAndReturnId("""
                        INSERT INTO sys_permission
                        (permission_name, permission_code, permission_type, route_path, sort_order, status, deleted)
                        VALUES (?, ?, ?, ?, 0, 1, 0)
                        """,
                code,
                code,
                type,
                routePath);
    }

    private Long insertUser(String seed, Long roleId) {
        return insertAndReturnId("""
                        INSERT INTO sys_user
                        (username, password_hash, real_name, role_id, status, deleted)
                        VALUES (?, ?, ?, ?, 1, 0)
                        """,
                username(seed),
                passwordEncoder.encode(PASSWORD),
                "IT " + seed,
                roleId);
    }

    private Long insertMajor(String seed) {
        return insertAndReturnId("""
                        INSERT INTO major_info
                        (major_name, major_code, college_name, status, deleted)
                        VALUES (?, ?, 'Integration College', 1, 0)
                        """,
                "IT Major " + seed,
                "IT-MAJOR-" + seed + "-" + suffix);
    }

    private Long insertClass(String seed, Long majorId, String grade) {
        return insertAndReturnId("""
                        INSERT INTO class_info
                        (class_name, class_code, major_id, grade, counselor_name, status, deleted)
                        VALUES (?, ?, ?, ?, 'IT Counselor', 1, 0)
                        """,
                "IT Class " + seed,
                "IT-CLASS-" + seed + "-" + suffix,
                majorId,
                grade);
    }

    private Long insertStudent(String seed, Long userId, Long majorId, Long classId, String grade) {
        return insertAndReturnId("""
                        INSERT INTO student
                        (user_id, student_no, name, grade, major_id, class_id, status, deleted)
                        VALUES (?, ?, ?, ?, ?, ?, 1, 0)
                        """,
                userId,
                "ITS" + shortCode(seed) + suffix.substring(Math.max(0, suffix.length() - 12)),
                "IT Student " + seed,
                grade,
                majorId,
                classId);
    }

    private String shortCode(String seed) {
        String normalized = seed.replaceAll("[^A-Za-z0-9]", "");
        return normalized.length() <= 8 ? normalized : normalized.substring(0, 8);
    }

    private Long insertCategory(String seed, BigDecimal maxScore) {
        return insertAndReturnId("""
                        INSERT INTO evaluation_category
                        (category_name, category_code, max_score, sort_no, status, deleted)
                        VALUES (?, ?, ?, 0, 1, 0)
                        """,
                "IT Category " + seed,
                "IT-CAT-" + seed + "-" + suffix,
                maxScore);
    }

    private Long insertItem(String seed, Long categoryId, BigDecimal score) {
        return insertAndReturnId("""
                        INSERT INTO evaluation_item
                        (category_id, item_name, item_code, score_type, score, max_score, need_attachment, sort_no, status, deleted)
                        VALUES (?, ?, ?, 'MANUAL', ?, ?, 0, 0, 1, 0)
                        """,
                categoryId,
                "IT Item " + seed,
                "IT-ITEM-" + seed + "-" + suffix,
                score,
                score);
    }

    private Long insertMaterial(Long studentId, Long itemId, String status, BigDecimal applyScore, BigDecimal finalScore) {
        return insertAndReturnId("""
                        INSERT INTO material_application
                        (student_id, item_id, title, description, apply_score, final_score, status, submit_count, submit_time, review_time, deleted)
                        VALUES (?, ?, ?, 'integration material', ?, ?, ?, 1, ?, ?, 0)
                        """,
                studentId,
                itemId,
                "IT Material " + status + " " + suffix,
                applyScore,
                finalScore,
                status,
                "SUBMITTED".equals(status) || "APPROVED".equals(status) || "REJECTED".equals(status) ? LocalDateTime.now() : null,
                "APPROVED".equals(status) || "REJECTED".equals(status) ? LocalDateTime.now() : null);
    }

    private void insertReviewerScope(Long reviewerId, Long majorId, Long classId, String grade) {
        jdbcTemplate.update("""
                        INSERT INTO reviewer_scope
                        (reviewer_id, major_id, class_id, grade, deleted)
                        VALUES (?, ?, ?, ?, 0)
                        """,
                reviewerId,
                majorId,
                classId,
                grade);
    }

    private void clearReviewerScopes(Long reviewerId) {
        jdbcTemplate.update("DELETE FROM reviewer_scope WHERE reviewer_id = ?", reviewerId);
    }

    private Long createMaterial(String token, Long itemId, String title, BigDecimal applyScore) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("itemId", itemId);
        request.put("title", title);
        request.put("description", "integration test material");
        request.put("applyScore", applyScore);
        request.put("status", "DRAFT");
        String response = mockMvc.perform(post("/api/material-applications")
                        .with(auth(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).path("data").path("id").asLong();
    }

    private void submit(String token, Long materialId) throws Exception {
        mockMvc.perform(post("/api/material-applications/{id}/submit", materialId).with(auth(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));
    }

    private String login(String usernameSeed) throws Exception {
        return loginWithPassword(usernameSeed, PASSWORD);
    }

    private String loginWithPassword(String usernameSeed, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username(usernameSeed), "password", password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode root = objectMapper.readTree(response);
        return root.path("data").path("token").asText();
    }

    private void assertScore(Long studentId, String totalScore, int classRank, int majorRank) {
        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT total_score, class_rank, major_rank FROM score_summary WHERE student_id = ? AND deleted = 0",
                studentId);
        org.assertj.core.api.Assertions.assertThat(new BigDecimal(row.get("total_score").toString()))
                .isEqualByComparingTo(totalScore);
        org.assertj.core.api.Assertions.assertThat(((Number) row.get("class_rank")).intValue()).isEqualTo(classRank);
        org.assertj.core.api.Assertions.assertThat(((Number) row.get("major_rank")).intValue()).isEqualTo(majorRank);
    }

    private void assertCategoryScore(Long studentId, Long categoryId, String categoryScore) {
        BigDecimal actual = jdbcTemplate.queryForObject(
                "SELECT category_score FROM score_category_summary WHERE student_id = ? AND category_id = ? AND deleted = 0",
                BigDecimal.class,
                studentId,
                categoryId);
        org.assertj.core.api.Assertions.assertThat(actual).isEqualByComparingTo(categoryScore);
    }

    private byte[] expectExcel(String url, String token, String filenamePart) throws Exception {
        MvcResult result = mockMvc.perform(get(url).with(auth(token)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")))
                .andExpect(header().string("Content-Disposition", containsString(filenamePart)))
                .andReturn();
        byte[] content = result.getResponse().getContentAsByteArray();
        org.assertj.core.api.Assertions.assertThat(content).isNotEmpty();
        return content;
    }

    private void assertWorkbookHasHeader(byte[] content) throws Exception {
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(content))) {
            org.assertj.core.api.Assertions.assertThat(workbook.getNumberOfSheets()).isGreaterThan(0);
            org.assertj.core.api.Assertions.assertThat(workbook.getSheetAt(0).getRow(0)).isNotNull();
            org.assertj.core.api.Assertions.assertThat(workbook.getSheetAt(0).getRow(0).getPhysicalNumberOfCells()).isGreaterThan(0);
        }
    }

    private void cleanupRollbackScenario(Long materialId, Long studentId, Long reviewerUserId, Long studentUserId,
                                         Long itemId, Long categoryId, Long classId, Long majorId) {
        if (materialId != null) {
            jdbcTemplate.update("DELETE FROM review_record WHERE material_id = ?", materialId);
            jdbcTemplate.update("DELETE FROM material_attachment WHERE material_id = ?", materialId);
            jdbcTemplate.update("DELETE FROM material_application WHERE id = ?", materialId);
        }
        if (studentId != null) {
            jdbcTemplate.update("DELETE FROM score_category_summary WHERE student_id = ?", studentId);
            jdbcTemplate.update("DELETE FROM score_summary WHERE student_id = ?", studentId);
            jdbcTemplate.update("DELETE FROM student WHERE id = ?", studentId);
        }
        if (reviewerUserId != null) {
            jdbcTemplate.update("DELETE FROM reviewer_scope WHERE reviewer_id = ?", reviewerUserId);
            jdbcTemplate.update("DELETE FROM sys_user WHERE id = ?", reviewerUserId);
        }
        if (studentUserId != null) {
            jdbcTemplate.update("DELETE FROM sys_user WHERE id = ?", studentUserId);
        }
        if (itemId != null) {
            jdbcTemplate.update("DELETE FROM evaluation_item WHERE id = ?", itemId);
        }
        if (categoryId != null) {
            jdbcTemplate.update("DELETE FROM evaluation_category WHERE id = ?", categoryId);
        }
        if (classId != null) {
            jdbcTemplate.update("DELETE FROM class_info WHERE id = ?", classId);
        }
        if (majorId != null) {
            jdbcTemplate.update("DELETE FROM major_info WHERE id = ?", majorId);
        }
    }

    private void ensureLogTables() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS operation_log (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    user_id BIGINT DEFAULT NULL,
                    username VARCHAR(50) DEFAULT NULL,
                    real_name VARCHAR(50) DEFAULT NULL,
                    role_code VARCHAR(50) DEFAULT NULL,
                    operation_type VARCHAR(50) NOT NULL,
                    operation_module VARCHAR(100) NOT NULL,
                    operation_desc VARCHAR(500) DEFAULT NULL,
                    request_method VARCHAR(20) DEFAULT NULL,
                    request_uri VARCHAR(255) DEFAULT NULL,
                    request_params TEXT,
                    ip_address VARCHAR(100) DEFAULT NULL,
                    user_agent VARCHAR(500) DEFAULT NULL,
                    result VARCHAR(20) NOT NULL,
                    error_message VARCHAR(1000) DEFAULT NULL,
                    operation_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    KEY idx_operation_user_id (user_id),
                    KEY idx_operation_username (username),
                    KEY idx_operation_time (operation_time),
                    KEY idx_operation_module (operation_module),
                    KEY idx_operation_result (result)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS login_log (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    user_id BIGINT DEFAULT NULL,
                    username VARCHAR(50) DEFAULT NULL,
                    real_name VARCHAR(50) DEFAULT NULL,
                    role_code VARCHAR(50) DEFAULT NULL,
                    login_type VARCHAR(20) NOT NULL,
                    result VARCHAR(20) NOT NULL,
                    ip_address VARCHAR(100) DEFAULT NULL,
                    user_agent VARCHAR(500) DEFAULT NULL,
                    error_message VARCHAR(1000) DEFAULT NULL,
                    login_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    KEY idx_login_user_id (user_id),
                    KEY idx_login_username (username),
                    KEY idx_login_time (login_time),
                    KEY idx_login_type (login_type),
                    KEY idx_login_result (result)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
    }

    private int countLoginLogs(String username, String loginType, String result) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM login_log WHERE username = ? AND login_type = ? AND result = ?",
                Integer.class,
                username,
                loginType,
                result);
        return count == null ? 0 : count;
    }

    private int countOperationLogs(String module, String type) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM operation_log WHERE operation_module = ? AND operation_type = ? AND result = 'SUCCESS'",
                Integer.class,
                module,
                type);
        return count == null ? 0 : count;
    }

    private void assertOperationLogsDoNotContainSensitiveValues() {
        String joined = jdbcTemplate.queryForObject("""
                        SELECT COALESCE(GROUP_CONCAT(CONCAT_WS(' ', request_params, error_message) SEPARATOR ' '), '')
                        FROM operation_log
                        """,
                String.class);
        org.assertj.core.api.Assertions.assertThat(joined)
                .doesNotContain("newPassword")
                .doesNotContain("oldPassword")
                .doesNotContain("Authorization")
                .doesNotContain("token")
                .doesNotContain("reset123");
    }

    private Long queryLong(String sql, Object... args) {
        return jdbcTemplate.query(sql, rs -> rs.next() ? rs.getLong(1) : null, args);
    }

    private Long insertAndReturnId(String sql, Object... args) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        org.assertj.core.api.Assertions.assertThat(key).as("generated key").isNotNull();
        return key.longValue();
    }

    private String username(String seed) {
        return seed + "-" + suffix;
    }

    private RequestPostProcessor auth(String token) {
        return request -> {
            request.addHeader("Authorization", "Bearer " + token);
            return request;
        };
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private record TestDataset(Long majorId, Long classId, Long studentId, Long categoryId, Long itemId) {
    }
}
