from appium.webdriver.common.appiumby import AppiumBy
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

DEFAULT_TIMEOUT = 10


class BasePage:
    def __init__(self, driver):
        self.driver = driver

    def find_by_accessibility_id(self, accessibility_id, timeout=DEFAULT_TIMEOUT):
        return WebDriverWait(self.driver, timeout).until(
            EC.presence_of_element_located(
                (AppiumBy.ACCESSIBILITY_ID, accessibility_id)
            )
        )

    def find_by_text(self, text, timeout=DEFAULT_TIMEOUT):
        return WebDriverWait(self.driver, timeout).until(
            EC.presence_of_element_located(
                (AppiumBy.ANDROID_UIAUTOMATOR, f'new UiSelector().textContains("{text}")')
            )
        )

    def click(self, accessibility_id, timeout=DEFAULT_TIMEOUT):
        element = WebDriverWait(self.driver, timeout).until(
            EC.element_to_be_clickable((AppiumBy.ACCESSIBILITY_ID, accessibility_id))
        )
        element.click()

    def type_text(self, accessibility_id, text, timeout=DEFAULT_TIMEOUT):
        element = self.find_by_accessibility_id(accessibility_id, timeout)
        element.click()
        # Select all existing text (Ctrl+A) then delete
        self.driver.press_keycode(29, metastate=0x1000)
        self.driver.press_keycode(112)
        # Type new text
        self.driver.execute_script("mobile: type", {"text": text})

    def is_element_present(self, accessibility_id, timeout=DEFAULT_TIMEOUT):
        try:
            self.find_by_accessibility_id(accessibility_id, timeout)
            return True
        except Exception:
            return False

    def get_element_text(self, accessibility_id, timeout=DEFAULT_TIMEOUT):
        element = self.find_by_accessibility_id(accessibility_id, timeout)
        return element.text
