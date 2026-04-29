from pages.base_page import BasePage, DEFAULT_TIMEOUT


class SettingsPage(BasePage):

    def is_loaded(self, timeout=DEFAULT_TIMEOUT):
        return self.is_element_present("Settings", timeout=timeout)

    def click_logout(self, timeout=DEFAULT_TIMEOUT):
        self.click("Logout", timeout=timeout)

    def get_server_url(self, timeout=DEFAULT_TIMEOUT) -> str:
        return self.get_element_text("Server URL", timeout=timeout)
