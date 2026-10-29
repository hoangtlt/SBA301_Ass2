/**
 * @author HoangTLT
 */

package com.funews;

import com.fasterxml.jackson.databind.*;
import com.funews.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "debug=false") @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class NewsApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired AccountRepository accounts;
    @Autowired NewsRepository news;
    @Autowired CategoryRepository categories;
    @Autowired TagRepository tags;

    record User(MockHttpSession session, String csrf, int id) { }
    User login(String email, String password) throws Exception {
        var result = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isOk()).andReturn();
        var data = json(result);
        return new User((MockHttpSession) result.getRequest().getSession(false), data.get("csrfToken").asText(), data.get("account").get("accountId").asInt());
    }
    JsonNode json(MvcResult result) throws Exception { return mapper.readTree(result.getResponse().getContentAsString()); }
    ResultActions create(String url, Object data, User user) throws Exception {
        return mvc.perform(post(url).session(user.session).header("X-CSRF-Token", user.csrf)
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(data)));
    }
    ResultActions update(String url, Object data, User user) throws Exception {
        return mvc.perform(put(url).session(user.session).header("X-CSRF-Token", user.csrf)
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(data)));
    }
    ResultActions remove(String url, User user) throws Exception {
        return mvc.perform(delete(url).session(user.session).header("X-CSRF-Token", user.csrf));
    }
    int categoryId() { return categories.findAll().get(0).getCategoryId(); }
    Map<String, Object> article(String title, int status, int categoryId) {
        return new HashMap<>(Map.of("newsTitle", title, "headline", "Nội dung tóm tắt", "newsContent", "Nội dung kiểm thử tiếng Việt.",
                "newsSource", "Test", "categoryId", categoryId, "newsStatus", status,
                "tags", List.of(Map.of("tagName", "Java", "note", "Tag kiểm thử"))));
    }
    Map<String, Object> category(String name) {
        return new HashMap<>(Map.of("categoryName", name, "categoryDescription", "Mô tả danh mục", "isActive", true));
    }

    @Test void publicReadsOnlyActiveArticlesAndNeverLeaksPasswords() throws Exception {
        var result = mvc.perform(get("/api/public/news")).andExpect(status().isOk()).andReturn();
        var data = json(result);
        assertThat(data).hasSize(2);
        for (var row : data) assertThat(row.get("newsStatus").asInt()).isEqualTo(1);
        assertThat(result.getResponse().getContentAsString()).doesNotContain("accountPassword");
        int draftId = news.findAll().stream().filter(n -> n.getNewsStatus() == 0).findFirst().orElseThrow().getNewsArticleId();
        mvc.perform(get("/api/public/news/" + draftId)).andExpect(status().isNotFound());
        mvc.perform(get("/api/public/news/999999")).andExpect(status().isNotFound());
    }

    @Test void authenticationRoleChecksAndCsrfProtectMutations() throws Exception {
        mvc.perform(get("/api/accounts")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/news")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"staff@funews.com\",\"password\":\"wrong\"}")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("email", "staff@funews.com", "password", "密".repeat(40)))))
                .andExpect(status().isUnauthorized());
        var admin = login("admin@funews.com", "Admin@123");
        mvc.perform(get("/api/categories").session(admin.session)).andExpect(status().isForbidden());
        var staff = login("staff@funews.com", "Staff@123");
        mvc.perform(get("/api/accounts").session(staff.session)).andExpect(status().isForbidden());
        mvc.perform(post("/api/categories").session(staff.session).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(category("CSRF")))).andExpect(status().isForbidden());
        mvc.perform(post("/api/auth/logout").session(staff.session).header("X-CSRF-Token", staff.csrf)).andExpect(status().isNoContent());
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test void adminCanCrudSearchAccountsWithoutExposingPasswords() throws Exception {
        var admin = login("admin@funews.com", "Admin@123");
        var form = new HashMap<String, Object>(Map.of("accountName", "Test Account", "accountEmail", "test@funews.com", "accountRole", 2, "password", "Test@123"));
        var result = create("/api/accounts", form, admin).andExpect(status().isCreated()).andReturn();
        int id = json(result).get("accountId").asInt();
        assertThat(result.getResponse().getContentAsString()).doesNotContain("password", "Test@123", "$2a$");
        assertThat(accounts.findById(id).orElseThrow().getAccountPassword()).startsWith("$2a$");
        form.put("accountName", "Changed Account"); form.put("password", "");
        update("/api/accounts/" + id, form, admin).andExpect(status().isOk()).andExpect(jsonPath("$.accountName").value("Changed Account"));
        mvc.perform(get("/api/accounts").param("q", "changed").session(admin.session)).andExpect(jsonPath("$.length()").value(1));
        create("/api/accounts", form, admin).andExpect(status().isConflict());
        remove("/api/accounts/" + id, admin).andExpect(status().isNoContent());
        mvc.perform(get("/api/accounts/" + id).session(admin.session)).andExpect(status().isNotFound());
    }

    @Test void cannotDeleteAnAuthorEvenWhenTheirArticlesAreInactive() throws Exception {
        var admin = login("admin@funews.com", "Admin@123");
        var author = accounts.findByAccountEmailIgnoreCase("staff@funews.com").orElseThrow();
        news.findAll().forEach(n -> n.setNewsStatus(0));
        remove("/api/accounts/" + author.getAccountId(), admin).andExpect(status().isConflict());
        remove("/api/accounts/" + admin.id, admin).andExpect(status().isBadRequest());
    }

    @Test void categoryCrudSearchPreventsDeletionWhenUsedAndRejectsCycles() throws Exception {
        var staff = login("staff@funews.com", "Staff@123");
        var parentForm = category("Test parent");
        int parent = json(create("/api/categories", parentForm, staff).andExpect(status().isCreated()).andReturn()).get("categoryId").asInt();
        var childForm = category("Test child"); childForm.put("parentCategoryId", parent);
        int child = json(create("/api/categories", childForm, staff).andExpect(status().isCreated()).andReturn()).get("categoryId").asInt();
        parentForm.put("parentCategoryId", child);
        update("/api/categories/" + parent, parentForm, staff).andExpect(status().isBadRequest());
        remove("/api/categories/" + parent, staff).andExpect(status().isConflict());
        mvc.perform(get("/api/categories").param("q", "Test child").session(staff.session)).andExpect(jsonPath("$.length()").value(1));
        remove("/api/categories/" + child, staff).andExpect(status().isNoContent());
        remove("/api/categories/" + parent, staff).andExpect(status().isNoContent());
        int usedCategory = news.findAll().get(0).getCategory().getCategoryId();
        remove("/api/categories/" + usedCategory, staff).andExpect(status().isConflict());
    }

    @Test void articleCrudIncludesTagsAuditFieldsSearchAndOwnHistory() throws Exception {
        var staff = login("staff2@funews.com", "Staff@123");
        long oldTagCount = tags.count();
        var form = article("Unique draft", 0, categoryId());
        var created = json(create("/api/news", form, staff).andExpect(status().isCreated()).andReturn());
        int id = created.get("newsArticleId").asInt();
        assertThat(created.get("createdById").asInt()).isEqualTo(staff.id);
        assertThat(created.get("createdDate").asText()).isNotBlank();
        assertThat(tags.count()).isEqualTo(oldTagCount + 1);
        mvc.perform(get("/api/public/news/" + id)).andExpect(status().isNotFound());
        mvc.perform(get("/api/news/history").session(staff.session)).andExpect(jsonPath("$.length()").value(1));
        var other = login("staff@funews.com", "Staff@123");
        mvc.perform(get("/api/news/history").param("q", "Unique draft").session(other.session)).andExpect(jsonPath("$.length()").value(0));
        form.put("newsTitle", "Updated draft"); form.put("newsStatus", 1);
        form.put("tags", List.of(Map.of("tagName", "React", "note", "Edited"), Map.of("tagName", "IntegrationTag", "note", "")));
        update("/api/news/" + id, form, other).andExpect(status().isOk())
                .andExpect(jsonPath("$.updatedById").value(other.id)).andExpect(jsonPath("$.createdById").value(staff.id))
                .andExpect(jsonPath("$.modifiedDate").isNotEmpty()).andExpect(jsonPath("$.tags.length()").value(2));
        assertThat(tags.count()).isEqualTo(oldTagCount + 2);
        mvc.perform(get("/api/public/news/" + id)).andExpect(status().isOk());
        mvc.perform(get("/api/news").param("q", "IntegrationTag").session(staff.session)).andExpect(jsonPath("$.length()").value(1));
        remove("/api/news/" + id, staff).andExpect(status().isNoContent());
        assertThat(tags.count()).isEqualTo(oldTagCount);
        mvc.perform(get("/api/news/" + id).session(staff.session)).andExpect(status().isNotFound());
    }

    @Test void inactiveCategoryAndInvalidTagsAreRejected() throws Exception {
        var staff = login("staff@funews.com", "Staff@123");
        var category = category("Inactive"); category.put("isActive", false);
        int id = json(create("/api/categories", category, staff).andExpect(status().isCreated()).andReturn()).get("categoryId").asInt();
        create("/api/news", article("Rejected", 1, id), staff).andExpect(status().isBadRequest());
        var form = article("Repeated tags", 1, categoryId());
        form.put("tags", List.of(Map.of("tagName", "Java"), Map.of("tagName", " java ")));
        create("/api/news", form, staff).andExpect(status().isBadRequest());
    }

    @Test void profileRequiresCurrentPasswordAndCannotChangeRole() throws Exception {
        var staff = login("staff2@funews.com", "Staff@123");
        var form = new HashMap<String, Object>(Map.of("accountName", "New Staff Name", "accountEmail", "changed@funews.com", "currentPassword", "wrong", "newPassword", "NewPass@123"));
        update("/api/profile", form, staff).andExpect(status().isBadRequest());
        form.put("currentPassword", "Staff@123");
        update("/api/profile", form, staff).andExpect(status().isOk()).andExpect(jsonPath("$.accountRole").value(2));
        login("changed@funews.com", "NewPass@123");
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"changed@funews.com\",\"password\":\"Staff@123\"}")).andExpect(status().isUnauthorized());
        form.put("accountRole", 1);
        update("/api/profile", form, staff).andExpect(status().isBadRequest());
    }

    @Test void fieldValidationRejectsEmptyWrongTypeOutOfRangeAndOversizedData() throws Exception {
        var admin = login("admin@funews.com", "Admin@123");
        create("/api/accounts", Map.of("accountName", " ", "accountEmail", "invalid", "accountRole", 3, "password", "short"), admin)
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.accountName").exists()).andExpect(jsonPath("$.errors.accountRole").exists());
        var staff = login("staff@funews.com", "Staff@123");
        var form = article("x".repeat(201), 1, categoryId());
        create("/api/news", form, staff).andExpect(status().isBadRequest());
        form.put("newsTitle", "Valid"); form.put("newsStatus", 0.5);
        create("/api/news", form, staff).andExpect(status().isBadRequest());
        form.put("newsStatus", 3);
        create("/api/news", form, staff).andExpect(status().isBadRequest());
        form.put("newsStatus", 1); form.put("tags", null);
        create("/api/news", form, staff).andExpect(status().isBadRequest());
        create("/api/categories", Map.of("categoryName", "Test", "categoryDescription", "Test", "isActive", "wrong"), staff).andExpect(status().isBadRequest());
    }
}
