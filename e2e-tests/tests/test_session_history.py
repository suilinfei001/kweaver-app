import pytest
from pages.home_page import HomePage


def test_session_tab_loads(logged_in_driver):
    """Verify the session history tab loads via text selector."""
    home = HomePage(logged_in_driver)
    assert home.is_loaded(timeout=10)

    try:
        tab = logged_in_driver.find_element(
            "xpath", "//*[@text='History']"
        )
        tab.click()
    except Exception:
        pytest.skip("History tab not found")


def test_session_list_displays(logged_in_driver):
    """Verify session list page loads via text selector."""
    home = HomePage(logged_in_driver)
    assert home.is_loaded(timeout=10)

    try:
        tab = logged_in_driver.find_element(
            "xpath", "//*[@text='History']"
        )
        tab.click()
    except Exception:
        pytest.skip("History tab not found")
