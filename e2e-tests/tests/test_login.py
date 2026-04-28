import pytest
from pages.login_page import LoginPage
from pages.home_page import HomePage

TEST_SERVER_URL = "https://192.168.40.110"
TEST_USERNAME = "admin"
TEST_PASSWORD = "eisoo.com"


class TestLogin:

    def test_login_page_loads(self, appium_driver):
        login_page = LoginPage(appium_driver)
        assert login_page.is_loaded(), "Login page should be displayed on app launch"

    def test_successful_login(self, appium_driver):
        login_page = LoginPage(appium_driver)
        assert login_page.is_loaded()

        login_page.login(TEST_SERVER_URL, TEST_USERNAME, TEST_PASSWORD)

        home_page = HomePage(appium_driver)
        assert home_page.is_loaded(timeout=30), "Should navigate to home screen after successful login"

    def test_login_with_empty_username(self, appium_driver):
        login_page = LoginPage(appium_driver)
        assert login_page.is_loaded()

        login_page.enter_server_url(TEST_SERVER_URL)
        login_page.enter_password(TEST_PASSWORD)
        login_page.click_sign_in()

        assert login_page.has_error(), "Error message should be displayed"
        assert "username" in login_page.get_error_message().lower()

    def test_login_with_empty_password(self, appium_driver):
        login_page = LoginPage(appium_driver)
        assert login_page.is_loaded()

        login_page.enter_server_url(TEST_SERVER_URL)
        login_page.enter_username(TEST_USERNAME)
        login_page.click_sign_in()

        assert login_page.has_error(), "Error message should be displayed"
        assert "password" in login_page.get_error_message().lower()

    def test_login_with_wrong_credentials(self, appium_driver):
        login_page = LoginPage(appium_driver)
        assert login_page.is_loaded()

        login_page.login(TEST_SERVER_URL, "wrong_user", "wrong_pass")

        assert login_page.has_error(timeout=15), "Error message should be displayed for bad credentials"
