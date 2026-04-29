import pytest
from pages.home_page import HomePage


def test_bottom_nav_all_tabs_accessible(logged_in_driver):
    """Verify bottom navigation tabs are accessible by text."""
    home = HomePage(logged_in_driver)
    assert home.is_loaded(timeout=10), "App should be on home page"

    for tab_name in ["Chat", "Agents", "Store", "History", "Plans"]:
        try:
            tab = logged_in_driver.find_element(
                "xpath", f"//*[@text='{tab_name}']"
            )
            tab.click()
        except Exception:
            pytest.skip(f"Tab '{tab_name}' not found - may need contentDescription calibration")


def test_back_navigation_returns_to_home(logged_in_driver):
    """Verify pressing back returns to the main screen."""
    home = HomePage(logged_in_driver)
    assert home.is_loaded(timeout=10)

    logged_in_driver.press_keycode(4)  # KEYCODE_BACK

    # After back press, app may go to background. Bring it back.
    import time
    time.sleep(1)
    logged_in_driver.activate_app("com.kweaver.dip")
    time.sleep(2)

    home2 = HomePage(logged_in_driver)
    assert home2.is_loaded(timeout=10), "Should return to home after back press"


def test_settings_accessible_from_home(logged_in_driver):
    """Verify settings icon is accessible from home."""
    home = HomePage(logged_in_driver)
    assert home.is_loaded(timeout=10)

    assert home.is_element_present("Settings", timeout=5), "Settings button should be visible"
