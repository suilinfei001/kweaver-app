from appium.webdriver.common.appiumby import AppiumBy
from pages.base_page import BasePage, DEFAULT_TIMEOUT


class HomePage(BasePage):
    SETTINGS_BUTTON = "Settings"

    def is_loaded(self, timeout=15):
        return self.is_element_present(self.SETTINGS_BUTTON, timeout=timeout)

    def click_digital_human_by_name(self, name: str, timeout=DEFAULT_TIMEOUT):
        element = self.find_by_text(name, timeout=timeout)
        element.click()

    def has_digital_human(self, name: str, timeout=DEFAULT_TIMEOUT) -> bool:
        try:
            self.find_by_text(name, timeout=timeout)
            return True
        except Exception:
            return False
