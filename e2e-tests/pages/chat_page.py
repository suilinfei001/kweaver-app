"""Page Object for Chat screen."""

from appium.webdriver.common.appiumby import AppiumBy
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC


class ChatPage:
    def __init__(self, driver):
        self.driver = driver

    # --- Locators ---
    _title = (AppiumBy.ANDROID_UIAUTOMATOR,
              'new UiSelector().text("AI 对话")')
    _input_field = (AppiumBy.ANDROID_UIAUTOMATOR,
                    'new UiSelector().textContains("输入消息")')
    _send_btn = (AppiumBy.ANDROID_UIAUTOMATOR,
                 'new UiSelector().text("发送")')
    _new_chat_btn = (AppiumBy.ANDROID_UIAUTOMATOR,
                     'new UiSelector().descriptionContains("新对话")')
    _settings_btn = (AppiumBy.ANDROID_UIAUTOMATOR,
                     'new UiSelector().descriptionContains("设置")')

    # --- Actions ---
    def is_displayed(self, timeout=10):
        wait = WebDriverWait(self.driver, timeout)
        el = wait.until(EC.presence_of_element_located(self._title))
        return el.is_displayed()

    def type_message(self, text):
        el = self.driver.find_element(*self._input_field)
        el.send_keys(text)

    def send_message(self):
        self.driver.find_element(*self._send_btn).click()

    def send_text(self, text):
        self.type_message(text)
        self.send_message()

    def wait_for_response(self, timeout=60):
        """Wait for streaming to complete — input becomes enabled again."""
        import time
        # First wait a moment for streaming to start
        time.sleep(2)
        start = time.time()
        while time.time() - start < timeout:
            try:
                el = self.driver.find_element(*self._input_field)
                if el.is_enabled():
                    # Check if enough time has passed for a response
                    elapsed = time.time() - start
                    if elapsed > 3:
                        return True
            except Exception:
                pass
            time.sleep(1)
        return False

    def get_message_bubbles(self):
        """Get all visible text in the chat."""
        loc = (AppiumBy.ANDROID_UIAUTOMATOR,
               'new UiSelector().className("android.widget.TextView")')
        elements = self.driver.find_elements(*loc)
        skip = {"AI 对话", "发送", "输入消息..."}
        return [el.text for el in elements if el.text and el.text not in skip]

    def tap_new_chat(self):
        self.driver.find_element(*self._new_chat_btn).click()

    def tap_settings(self):
        self.driver.find_element(*self._settings_btn).click()
