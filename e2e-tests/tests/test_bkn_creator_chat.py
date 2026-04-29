import time
import pytest
from pages.home_page import HomePage
from pages.chat_page import ChatPage

BKN_CREATOR_NAME = "BKN Creator"


def test_bkn_creator_exists_on_home(logged_in_driver):
    home = HomePage(logged_in_driver)
    assert home.has_digital_human(BKN_CREATOR_NAME, timeout=10), \
        f"'{BKN_CREATOR_NAME}' should appear on home screen"


def test_bkn_creator_chat_no_http_400(logged_in_driver):
    home = HomePage(logged_in_driver)
    assert home.has_digital_human(BKN_CREATOR_NAME, timeout=10), \
        f"'{BKN_CREATOR_NAME}' should appear on home screen"

    home.click_digital_human_by_name(BKN_CREATOR_NAME)

    chat_page = ChatPage(logged_in_driver)
    assert chat_page.is_loaded(timeout=30), "Chat screen should load"

    time.sleep(2)  # Wait for session to be ready

    chat_page.send_chat_message("what can you do?")

    assert not chat_page.has_error(timeout=15), \
        f"Chat should not return HTTP 400 error. Got: {chat_page.get_error_text() if chat_page.has_any_error() else 'N/A'}"
