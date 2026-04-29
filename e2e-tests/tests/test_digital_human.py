import pytest
from pages.home_page import HomePage
from pages.digital_human_page import DigitalHumanPage


def test_digital_human_page_loads(logged_in_driver):
    """Verify the Digital Human tab loads successfully."""
    home = HomePage(logged_in_driver)
    assert home.is_loaded(timeout=30), "Home page should be loaded"


def test_digital_human_tab_shows_agents(logged_in_driver):
    """Switch to Digital Human tab and verify it loads."""
    logged_in_driver.find_element(
        "xpath", "//*[@text='Agents']"
    ).click()

    dh_page = DigitalHumanPage(logged_in_driver)
    assert dh_page.is_loaded(timeout=10), "Digital Human page should load"
