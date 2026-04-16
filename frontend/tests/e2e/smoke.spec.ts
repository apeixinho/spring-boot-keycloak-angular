import { expect, test } from "@playwright/test";

const keycloakBase = process.env.E2E_KEYCLOAK_URL ?? "http://localhost:8080";
const userUsername = process.env.E2E_USER_USERNAME ?? "grilldad";
const userPassword = process.env.E2E_USER_PASSWORD ?? "password";
const adminUsername = process.env.E2E_ADMIN_USERNAME ?? "metalgear";
const adminPassword = process.env.E2E_ADMIN_PASSWORD ?? "password";

async function loginViaKeycloak(page: import("@playwright/test").Page, username: string, password: string) {
  await page.waitForURL(new RegExp(`${keycloakBase.replace(/[.*+?^${}()|[\]\\]/g, "\\$&")}`), {
    timeout: 30_000
  });
  await page.fill("#username", username);
  await page.fill("#password", password);
  await page.click("#kc-login");
}

test("user can login and see cart link", async ({ page, baseURL }) => {
  await page.goto(baseURL || "/");
  await loginViaKeycloak(page, userUsername, userPassword);

  await expect(page).toHaveURL(/\/$/);
  await expect(page.getByRole("link", { name: "Cart" })).toBeVisible();
});

test("admin can see admin navigation links", async ({ page, baseURL }) => {
  await page.goto(baseURL || "/");
  await loginViaKeycloak(page, adminUsername, adminPassword);

  await expect(page.getByRole("link", { name: "Orders" })).toBeVisible();
  await expect(page.getByRole("link", { name: "Customers" })).toBeVisible();
});
