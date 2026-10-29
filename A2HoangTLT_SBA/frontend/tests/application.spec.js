/**
 * @author HoangTLT
 */

import { test, expect } from "@playwright/test";

const password = "Browser@123";
async function login(page, email, pass = password) {
  await page.goto("/login");
  await page.getByLabel("Email", { exact: true }).fill(email);
  await page.getByLabel("Mật khẩu", { exact: true }).fill(pass);
  await page.getByRole("button", { name: "Đăng nhập →" }).click();
  await expect(page).toHaveURL(
    email === "admin@funews.com" ? /\/admin\/accounts/ : /\/staff\/news/,
  );
}
async function adminApi(playwright) {
  const api = await playwright.request.newContext({
    baseURL: "http://localhost:5173",
  });
  const response = await api.post("/api/auth/login", {
    data: { email: "admin@funews.com", password: "Admin@123" },
  });
  expect(response.ok()).toBeTruthy();
  const { csrfToken } = await response.json();
  return { api, headers: { "X-CSRF-Token": csrfToken } };
}
async function search(page, query) {
  await page.getByRole("searchbox", { name: "Tìm kiếm" }).fill(query);
  const response = page.waitForResponse(
    (r) => r.url().includes("q=") && r.request().method() === "GET",
  );
  await page.getByRole("button", { name: "Tìm kiếm", exact: true }).click();
  await response;
}

test("login survives reload and an expired session returns to login", async ({ page }) => {
  await login(page, "admin@funews.com", "Admin@123");
  await page.reload();
  await expect(page.getByRole("heading", { name: "Quản lý tài khoản" })).toBeVisible();
  await expect(page.getByRole("button", { name: "Đăng xuất", exact: true })).toBeVisible();

  // Cookie bị mất trong khi App vẫn đang giữ account: request tiếp theo trả 401.
  await page.context().clearCookies();
  await page.getByRole("link", { name: "Bảng tin công khai", exact: true }).click();
  await page.getByRole("link", { name: "Trang quản lý", exact: true }).click();
  await expect(page).toHaveURL(/\/login/);
  await expect(page.getByLabel("Email", { exact: true })).toBeVisible();
});

test("public news, detail, search, mobile layout and protected routes", async ({
  page,
}) => {
  const errors = [];
  page.on("pageerror", (error) => errors.push(error.message));
  await page.goto("/");
  await expect(
    page.getByRole("heading", { name: "Bảng tin mới nhất" }),
  ).toBeVisible();
  await expect(page.locator(".news-card")).toHaveCount(2);
  await page.screenshot({
    path: "../docs/evidence/public-news.png",
    fullPage: true,
  });
  await page.locator(".news-card h3 a").first().click();
  await expect(page.locator(".article-content")).toBeVisible();
  await page.goto("/");
  await search(page, "zz-no-such-news-zz");
  await expect(page.getByText("Chưa có bài viết phù hợp.")).toBeVisible();
  await page.getByRole("button", { name: "Xóa bộ lọc" }).click();
  await expect(page.locator(".news-card")).toHaveCount(2);
  await page.setViewportSize({ width: 390, height: 844 });
  await expect(page.locator(".news-card").first()).toBeVisible();
  expect(
    await page.evaluate(
      () => document.documentElement.scrollWidth <= window.innerWidth,
    ),
  ).toBeTruthy();
  await page.screenshot({
    path: "../docs/evidence/public-mobile.png",
    fullPage: true,
  });
  await page.goto("/staff/news");
  await expect(page).toHaveURL(/\/login/);
  expect(errors).toEqual([]);
});

test("admin account CRUD, search, popup and delete confirmation", async ({
  page,
  playwright,
}) => {
  const { api, headers } = await adminApi(playwright);
  const email = `admin-crud-${Date.now()}@test.funews.com`;
  let id;
  try {
    await login(page, "admin@funews.com", "Admin@123");
    await page.screenshot({
      path: "../docs/evidence/admin-accounts.png",
      fullPage: true,
    });
    await page.getByRole("button", { name: "+ Thêm tài khoản" }).click();
    const dialog = page.getByRole("dialog");
    await expect(dialog).toBeVisible();
    await dialog.getByLabel("Họ và tên *").fill("Browser Account");
    await dialog.getByLabel("Email *", { exact: true }).fill(email);
    await dialog.getByLabel("Mật khẩu *", { exact: true }).fill(password);
    const created = page.waitForResponse(
      (r) =>
        r.url().endsWith("/api/accounts") && r.request().method() === "POST",
    );
    await dialog.getByRole("button", { name: "Tạo tài khoản" }).click();
    const createdResponse = await created;
    expect(createdResponse.status()).toBe(201);
    id = (await createdResponse.json()).accountId;
    await expect(dialog).not.toBeVisible();
    await search(page, email);
    const row = page.locator("tbody tr").filter({ hasText: email });
    await expect(row).toHaveCount(1);
    await row.getByRole("button", { name: "Sửa", exact: true }).click();
    await dialog.getByLabel("Họ và tên *").fill("Updated Browser Account");
    await dialog.getByRole("button", { name: "Lưu thay đổi" }).click();
    await expect(dialog).not.toBeVisible();
    await expect(row).toContainText("Updated Browser Account");
    await row.getByRole("button", { name: "Xóa", exact: true }).click();
    await expect(
      dialog.getByRole("heading", { name: "Xác nhận xóa" }),
    ).toBeVisible();
    await dialog.getByRole("button", { name: "Hủy", exact: true }).click();
    await expect(row).toBeVisible();
    await row.getByRole("button", { name: "Xóa", exact: true }).click();
    await dialog
      .getByRole("button", { name: "Xác nhận xóa", exact: true })
      .click();
    await expect(dialog).not.toBeVisible();
    await expect(page.getByText("Chưa có dữ liệu phù hợp.")).toBeVisible();
    await page.goto("/staff/categories");
    await expect(page).toHaveURL(/\/admin\/accounts/);
  } finally {
    if (id) await api.delete(`/api/accounts/${id}`, { headers });
    else {
      const result = await api.get("/api/accounts", { params: { q: email } });
      for (const account of await result.json())
        await api.delete(`/api/accounts/${account.accountId}`, { headers });
    }
    await api.dispose();
  }
});

test("staff category and article CRUD, tags, own history and public visibility", async ({
  page,
  playwright,
}) => {
  const { api, headers } = await adminApi(playwright);
  const suffix = Date.now();
  const email = `staff-crud-${suffix}@test.funews.com`;
  const accountResponse = await api.post("/api/accounts", {
    headers,
    data: {
      accountName: "Browser Staff",
      accountEmail: email,
      accountRole: 2,
      password,
    },
  });
  expect(accountResponse.status()).toBe(201);
  const accountId = (await accountResponse.json()).accountId;
  const staffApi = await playwright.request.newContext({
    baseURL: "http://localhost:5173",
  });
  const sessionResponse = await staffApi.post("/api/auth/login", {
    data: { email, password },
  });
  const staffHeaders = {
    "X-CSRF-Token": (await sessionResponse.json()).csrfToken,
  };
  let categoryId;
  let newsId;
  try {
    await login(page, email);
    await page.screenshot({
      path: "../docs/evidence/staff-news.png",
      fullPage: true,
    });
    await page.getByRole("link", { name: "Danh mục", exact: false }).click();
    await page.getByRole("button", { name: "+ Thêm danh mục" }).click();
    const dialog = page.getByRole("dialog");
    await dialog
      .getByLabel("Tên danh mục *")
      .fill(`Browser Category ${suffix}`);
    await dialog.getByLabel("Mô tả *").fill("Danh mục kiểm thử tiếng Việt");
    const createdCategory = page.waitForResponse(
      (r) =>
        r.url().endsWith("/api/categories") && r.request().method() === "POST",
    );
    await dialog.getByRole("button", { name: "Tạo danh mục" }).click();
    const categoryResponse = await createdCategory;
    expect(categoryResponse.status()).toBe(201);
    categoryId = (await categoryResponse.json()).categoryId;
    await expect(dialog).not.toBeVisible();
    const categoryRow = page
      .locator("tbody tr")
      .filter({ hasText: `Browser Category ${suffix}` });
    await categoryRow.getByRole("button", { name: "Sửa", exact: true }).click();
    await dialog.getByLabel("Mô tả *").fill("Mô tả đã sửa");
    await dialog.getByRole("button", { name: "Lưu thay đổi" }).click();
    await expect(dialog).not.toBeVisible();
    await expect(categoryRow).toContainText("Mô tả đã sửa");
    await page.getByRole("link", { name: "Bài viết", exact: false }).click();
    await page.getByRole("button", { name: "+ Thêm bài viết" }).click();
    const title = `Browser News ${suffix}`;
    await dialog.getByLabel("Tiêu đề *").fill(title);
    await dialog
      .getByLabel("Tóm tắt (Headline) *")
      .fill("Bản tin từ kiểm thử trình duyệt");
    await dialog
      .getByLabel("Danh mục *", { exact: true })
      .selectOption(String(categoryId));
    await dialog
      .getByLabel("Nội dung *", { exact: true })
      .fill("Nội dung bài viết tiếng Việt.\nĐoạn văn thứ hai.");
    await dialog.getByLabel("Nguồn tin").fill("Browser test");
    await dialog.getByRole("button", { name: "+ Thêm tag" }).click();
    await dialog.getByLabel("Tên tag 1 *").fill("BrowserTag");
    await dialog.getByLabel("Ghi chú tag 1").fill("Ghi chú kiểm thử");
    await page.screenshot({
      path: "../docs/evidence/news-dialog.png",
      fullPage: true,
    });
    const createdNews = page.waitForResponse(
      (r) => r.url().endsWith("/api/news") && r.request().method() === "POST",
    );
    await dialog.getByRole("button", { name: "Tạo bài viết" }).click();
    const newsResponse = await createdNews;
    expect(newsResponse.status()).toBe(201);
    newsId = (await newsResponse.json()).newsArticleId;
    await expect(dialog).not.toBeVisible();
    expect((await api.get(`/api/public/news/${newsId}`)).status()).toBe(404);
    await page
      .getByRole("link", { name: "Lịch sử của tôi", exact: false })
      .click();
    await expect(page.locator("tbody tr")).toHaveCount(1);
    await expect(page.locator("tbody tr")).toContainText(title);
    await page.getByRole("link", { name: "Bài viết", exact: false }).click();
    await search(page, title);
    const row = page.locator("tbody tr").filter({ hasText: title });
    await row.getByRole("button", { name: "Sửa", exact: true }).click();
    await dialog.getByLabel("Trạng thái *").selectOption("1");
    await dialog.getByLabel("Tên tag 1 *").fill("UpdatedTag");
    await dialog.getByRole("button", { name: "Lưu thay đổi" }).click();
    await expect(dialog).not.toBeVisible();
    const published = await api.get(`/api/public/news/${newsId}`);
    expect(published.status()).toBe(200);
    expect((await published.json()).tags[0].tagName).toBe("UpdatedTag");
    await row.getByRole("button", { name: "Xem", exact: true }).click();
    await expect(dialog).toContainText("Ghi chú kiểm thử");
    await dialog.getByRole("button", { name: "Đóng hộp thoại" }).click();
    await page.getByRole("link", { name: "Danh mục", exact: false }).click();
    await search(page, `Browser Category ${suffix}`);
    await page
      .locator("tbody tr")
      .getByRole("button", { name: "Xóa", exact: true })
      .click();
    await dialog
      .getByRole("button", { name: "Xác nhận xóa", exact: true })
      .click();
    await expect(dialog.getByRole("alert")).toContainText(
      "Không thể xóa danh mục đang có bài viết.",
    );
    await dialog.getByRole("button", { name: "Hủy", exact: true }).click();
    await page.getByRole("link", { name: "Bài viết", exact: false }).click();
    await search(page, title);
    await page
      .locator("tbody tr")
      .getByRole("button", { name: "Xóa", exact: true })
      .click();
    await dialog
      .getByRole("button", { name: "Xác nhận xóa", exact: true })
      .click();
    await expect(dialog).not.toBeVisible();
    await expect(page.getByText("Chưa có dữ liệu phù hợp.")).toBeVisible();
    await page.getByRole("link", { name: "Danh mục", exact: false }).click();
    await search(page, `Browser Category ${suffix}`);
    await page
      .locator("tbody tr")
      .getByRole("button", { name: "Xóa", exact: true })
      .click();
    await dialog
      .getByRole("button", { name: "Xác nhận xóa", exact: true })
      .click();
    await expect(dialog).not.toBeVisible();
    await expect(page.getByText("Chưa có dữ liệu phù hợp.")).toBeVisible();
  } finally {
    if (newsId)
      await staffApi.delete(`/api/news/${newsId}`, { headers: staffHeaders });
    if (categoryId)
      await staffApi.delete(`/api/categories/${categoryId}`, {
        headers: staffHeaders,
      });
    await api.delete(`/api/accounts/${accountId}`, { headers });
    await staffApi.dispose();
    await api.dispose();
  }
});

test("staff profile updates and new password works after logout", async ({
  page,
  playwright,
}) => {
  const { api, headers } = await adminApi(playwright);
  const email = `profile-${Date.now()}@test.funews.com`;
  const created = await api.post("/api/accounts", {
    headers,
    data: {
      accountName: "Profile Tester",
      accountEmail: email,
      accountRole: 2,
      password,
    },
  });
  const id = (await created.json()).accountId;
  try {
    await login(page, email);
    await page
      .getByRole("link", { name: "Hồ sơ cá nhân", exact: false })
      .click();
    await page.getByRole("button", { name: "Cập nhật hồ sơ" }).click();
    const dialog = page.getByRole("dialog");
    await dialog.getByLabel("Họ và tên *").fill("Updated Profile Tester");
    await dialog.getByLabel("Mật khẩu hiện tại *").fill(password);
    await dialog
      .getByLabel("Mật khẩu mới", { exact: false })
      .fill("Updated@123");
    await dialog.getByRole("button", { name: "Lưu thay đổi" }).click();
    await expect(dialog).not.toBeVisible();
    await expect(
      page.getByRole("heading", { name: "Updated Profile Tester" }),
    ).toBeVisible();
    await page.getByRole("button", { name: "Đăng xuất", exact: true }).click();
    await expect(page).toHaveURL("/login");
    await login(page, email, "Updated@123");
    await page.goto("/admin/accounts");
    await expect(page).toHaveURL(/\/staff\/news/);
  } finally {
    await api.delete(`/api/accounts/${id}`, { headers });
    await api.dispose();
  }
});
