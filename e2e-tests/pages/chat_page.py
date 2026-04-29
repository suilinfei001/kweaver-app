from appium.webdriver.common.appiumby import AppiumBy
from pages.base_page import BasePage, DEFAULT_TIMEOUT


class ChatPage(BasePage):

    def is_loaded(self, timeout=DEFAULT_TIMEOUT):
        for selector in [
            'new UiSelector().textContains("Type a message")',
            'new UiSelector().textContains("Start a conversation")',
            'new UiSelector().textContains("Creating session")',
            'new UiSelector().description("Back")',
        ]:
            try:
                self.driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR, selector)
                return True
            except Exception:
                continue
        return False

    def _find_chat_input(self, timeout=DEFAULT_TIMEOUT):
        try:
            return self.driver.find_element(
                AppiumBy.ANDROID_UIAUTOMATOR,
                'new UiSelector().textContains("Type a message")'
            )
        except Exception:
            return None

    def type_message(self, text: str):
        chat_input = self._find_chat_input(timeout=DEFAULT_TIMEOUT)
        if chat_input is None:
            raise RuntimeError("Chat input not found")
        chat_input.click()
        self.driver.execute_script("mobile: type", {"text": text})

    def send_message(self):
        send_btn = self.driver.find_element(
            AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().descriptionContains("Send")'
        )
        send_btn.click()

    def send_chat_message(self, text: str):
        self.type_message(text)
        self.send_message()

    def has_error(self, timeout=DEFAULT_TIMEOUT) -> bool:
        try:
            self.driver.find_element(
                AppiumBy.ANDROID_UIAUTOMATOR,
                'new UiSelector().textContains("HTTP 400")'
            )
            return True
        except Exception:
            return False

    def has_any_error(self, timeout=DEFAULT_TIMEOUT) -> bool:
        try:
            self.driver.find_element(
                AppiumBy.ANDROID_UIAUTOMATOR,
                'new UiSelector().textContains("HTTP ")'
            )
            return True
        except Exception:
            return False

    def get_error_text(self, timeout=DEFAULT_TIMEOUT) -> str:
        from selenium.webdriver.support.ui import WebDriverWait
        from selenium.webdriver.support import expected_conditions as EC
        el = WebDriverWait(self.driver, timeout).until(
            EC.presence_of_element_located(
                (AppiumBy.ANDROID_UIAUTOMATOR,
                 'new UiSelector().textContains("HTTP ")')
            )
        )
        return el.text

    def has_assistant_response(self, timeout=30) -> bool:
        try:
            self.driver.find_element(
                AppiumBy.ANDROID_UIAUTOMATOR,
                'new UiSelector().textContains("Creating session")'
            )
            return False
        except Exception:
            pass
        try:
            self.driver.find_element(
                AppiumBy.ANDROID_UIAUTOMATOR,
                'new UiSelector().textContains("Start a conversation")'
            )
            return False
        except Exception:
            pass
        try:
            self.driver.find_element(
                AppiumBy.ANDROID_UIAUTOMATOR,
                'new UiSelector().textContains("Typing")'
            )
            return True
        except Exception:
            pass
        try:
            self.driver.find_element(
                AppiumBy.ANDROID_UIAUTOMATOR,
                'new UiSelector().className("android.widget.ScrollView").childSelector('
                'new UiSelector().textContains("what can you do")).'
                'fromParent(new UiSelector().index(1))'
            )
            return True
        except Exception:
            return False

    def is_creating_session(self, timeout=5) -> bool:
        try:
            self.driver.find_element(
                AppiumBy.ANDROID_UIAUTOMATOR,
                'new UiSelector().textContains("Creating session")'
            )
            return True
        except Exception:
            return False
