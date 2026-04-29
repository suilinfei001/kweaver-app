from appium.webdriver.common.appiumby import AppiumBy
from pages.base_page import BasePage, DEFAULT_TIMEOUT


class DigitalHumanPage(BasePage):

    def is_loaded(self, timeout=DEFAULT_TIMEOUT):
        return self.is_text_present("Digital Humans", timeout=timeout)

    def is_text_present(self, text, timeout=DEFAULT_TIMEOUT):
        try:
            self.find_by_text(text, timeout=timeout)
            return True
        except Exception:
            return False

    def has_digital_human(self, name: str, timeout=DEFAULT_TIMEOUT) -> bool:
        try:
            self.find_by_text(name, timeout=timeout)
            return True
        except Exception:
            return False

    def click_digital_human(self, name: str, timeout=DEFAULT_TIMEOUT):
        element = self.find_by_text(name, timeout=timeout)
        element.click()

    def click_add_button(self, timeout=DEFAULT_TIMEOUT):
        self.click("Add Digital Human", timeout=timeout)

    def get_digital_human_count(self, timeout=DEFAULT_TIMEOUT) -> int:
        try:
            elements = self.driver.find_elements(
                AppiumBy.ANDROID_UIAUTOMATOR,
                'new UiSelector().className("android.view.View")'
            )
            return len(elements)
        except Exception:
            return 0
