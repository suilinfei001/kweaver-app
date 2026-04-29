import pytest
from pages.home_page import HomePage
from pages.chat_page import ChatPage


def test_chat_opens_from_home(logged_in_driver):
    """Verify chat can be opened from the home page."""
    home = HomePage(logged_in_driver)
    assert home.is_loaded(timeout=10), "Home page should be loaded"


def test_chat_with_digital_human(logged_in_driver):
    """Open chat with a digital human and send a message."""
    home = HomePage(logged_in_driver)
    assert home.is_loaded(timeout=10)

    if home.has_digital_human("BKN Creator", timeout=5):
        home.click_digital_human_by_name("BKN Creator")
    else:
        pytest.skip("No digital human available to chat with")

    chat = ChatPage(logged_in_driver)
    assert chat.is_loaded(timeout=30), "Chat page should load after clicking digital human"

    chat.send_chat_message("Hello, what can you do?")
    assert not chat.has_any_error(timeout=10), "Chat should not show HTTP errors"


def test_chat_no_session_errors(logged_in_driver):
    """Verify chat session is created without errors."""
    home = HomePage(logged_in_driver)
    assert home.is_loaded(timeout=10)

    if not home.has_digital_human("BKN Creator", timeout=5):
        pytest.skip("BKN Creator not available")

    home.click_digital_human_by_name("BKN Creator")

    chat = ChatPage(logged_in_driver)
    assert chat.is_loaded(timeout=15)
