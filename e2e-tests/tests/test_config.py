"""E2E tests for AI Config screen."""

import pytest
from pages.config_page import ConfigPage


class TestConfigScreen:
    """Tests for first-open config flow."""

    def test_config_screen_shows_on_first_launch(self, appium_driver):
        """App opens to config screen when no config is saved."""
        page = ConfigPage(appium_driver)
        assert page.is_displayed(), "Config screen should be visible on first launch"

    def test_empty_fields_validation(self, appium_driver):
        """Tapping test connection with empty fields shows validation error."""
        page = ConfigPage(appium_driver)
        assert page.is_displayed()
        page.tap_test_connection()
        error = page.get_error_text(timeout=5)
        assert "请填写" in error or "Base URL" in error, f"Expected validation error, got: {error}"

    def test_invalid_url_shows_error(self, appium_driver):
        """Invalid URL shows connection error."""
        page = ConfigPage(appium_driver)
        assert page.is_displayed()
        page.fill_config(
            base_url="http://192.0.2.1:9999",
            model_id="fake-model",
            api_key="fake-key",
        )
        page.tap_test_connection()
        error = page.get_error_text(timeout=30)
        assert error, "Expected an error message for invalid URL"

    def test_save_button_is_present(self, appium_driver):
        """Save button exists on config screen."""
        page = ConfigPage(appium_driver)
        assert page.is_displayed()
        page.fill_config(
            base_url="http://localhost:8080",
            model_id="test",
            api_key="test-key",
        )
        # Save button should be present (Compose enabled state is not testable via is_enabled)
        from appium.webdriver.common.appiumby import AppiumBy
        save_btn = appium_driver.find_element(
            AppiumBy.ANDROID_UIAUTOMATOR, 'new UiSelector().text("保存")')
        assert save_btn.is_displayed(), "Save button should be visible"
