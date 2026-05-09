"""E2E tests for Chat screen."""

import pytest
import os
from pages.config_page import ConfigPage
from pages.chat_page import ChatPage


# Read config from env vars or use defaults for testing
TEST_BASE_URL = os.environ.get("E2E_BASE_URL", "http://10.0.2.2:8080")
TEST_MODEL_ID = os.environ.get("E2E_MODEL_ID", "gpt-4o")
TEST_API_KEY = os.environ.get("E2E_API_KEY", "test-key")


def _setup_config(driver):
    """Navigate to config screen and save a valid config."""
    config = ConfigPage(driver)
    try:
        if config.is_displayed(timeout=3):
            # Already on config page (fresh install)
            config.fill_config(TEST_BASE_URL, TEST_MODEL_ID, TEST_API_KEY)
            config.tap_test_connection()
            try:
                config.get_success_text(timeout=15)
            except Exception:
                pytest.skip("Cannot connect to AI endpoint — skipping chat E2E tests")
            config.tap_save()
            return
    except Exception:
        pass

    # Config already saved — we should be on chat page or need to navigate
    chat = ChatPage(driver)
    try:
        if chat.is_displayed(timeout=3):
            return  # Already on chat page, config was saved previously
    except Exception:
        pass

    pytest.skip("Cannot determine app state — skipping chat E2E tests")


class TestChatScreen:
    """Tests for chat conversation flow."""

    def test_chat_screen_appears_after_config_save(self, appium_driver):
        """After saving config, chat screen appears."""
        _setup_config(appium_driver)
        chat = ChatPage(appium_driver)
        assert chat.is_displayed(timeout=10), "Chat screen should appear after config save"

    def test_send_message_and_see_response(self, appium_driver):
        """Send a text message and verify AI responds."""
        _setup_config(appium_driver)
        chat = ChatPage(appium_driver)
        assert chat.is_displayed()

        chat.send_text("Say exactly: hello_test")
        responded = chat.wait_for_response(timeout=60)
        assert responded, "Should receive a response from AI"

        bubbles = chat.get_message_bubbles()
        assert len(bubbles) >= 2, f"Expected at least 2 bubbles (user + assistant), got {len(bubbles)}"

    def test_new_conversation(self, appium_driver):
        """Create new conversation clears messages."""
        _setup_config(appium_driver)
        chat = ChatPage(appium_driver)
        assert chat.is_displayed()

        chat.send_text("First message")
        chat.wait_for_response(timeout=60)

        chat.tap_new_chat()

        # After new conversation, message list should be empty
        bubbles = chat.get_message_bubbles()
        assert len(bubbles) <= 1, f"New conversation should clear messages, got {len(bubbles)} bubbles"

    def test_settings_navigation(self, appium_driver):
        """Settings button navigates back to config screen."""
        _setup_config(appium_driver)
        chat = ChatPage(appium_driver)
        assert chat.is_displayed()

        chat.tap_settings()
        config = ConfigPage(appium_driver)
        assert config.is_displayed(), "Config screen should appear after tapping settings"
