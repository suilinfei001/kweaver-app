from pages.base_page import BasePage


class HomePage(BasePage):
    SETTINGS_BUTTON = "Settings"

    def is_loaded(self, timeout=15):
        return self.is_element_present(self.SETTINGS_BUTTON, timeout=timeout)
