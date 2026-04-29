from appium.webdriver.common.appiumby import AppiumBy
from pages.base_page import BasePage, DEFAULT_TIMEOUT


class SessionPage(BasePage):

    def is_loaded(self, timeout=DEFAULT_TIMEOUT):
        # Session list page has a list/composable content
        try:
            self.driver.find_element(
                AppiumBy.ANDROID_UIAUTOMATOR,
                'new UiSelector().className("android.widget.ScrollView")'
            )
            return True
        except Exception:
            return False

    def has_sessions(self, timeout=DEFAULT_TIMEOUT) -> bool:
        try:
            elements = self.driver.find_elements(
                AppiumBy.ANDROID_UIAUTOMATOR,
                'new UiSelector().className("android.view.View")'
            )
            return len(elements) > 0
        except Exception:
            return False

    def click_session(self, label: str, timeout=DEFAULT_TIMEOUT):
        element = self.find_by_text(label, timeout=timeout)
        element.click()
