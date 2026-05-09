"""Page Object for AI Config screen."""

from appium.webdriver.common.appiumby import AppiumBy
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC


class ConfigPage:
    def __init__(self, driver):
        self.driver = driver

    # --- Locators (using UiSelector for reliability with Compose) ---
    _title = (AppiumBy.ANDROID_UIAUTOMATOR,
              'new UiSelector().text("AI 模型配置")')
    _base_url = (AppiumBy.ANDROID_UIAUTOMATOR,
                 'new UiSelector().className("android.widget.EditText").instance(0)')
    _model_id = (AppiumBy.ANDROID_UIAUTOMATOR,
                 'new UiSelector().className("android.widget.EditText").instance(1)')
    _api_key = (AppiumBy.ANDROID_UIAUTOMATOR,
                'new UiSelector().className("android.widget.EditText").instance(2)')
    _context_size = (AppiumBy.ANDROID_UIAUTOMATOR,
                     'new UiSelector().className("android.widget.EditText").instance(3)')
    _test_btn = (AppiumBy.ANDROID_UIAUTOMATOR,
                 'new UiSelector().text("测试连接")')
    _save_btn = (AppiumBy.ANDROID_UIAUTOMATOR,
                 'new UiSelector().text("保存")')
    _success_msg = (AppiumBy.ANDROID_UIAUTOMATOR,
                    'new UiSelector().textContains("连接成功")')
    _error_msg = (AppiumBy.ANDROID_UIAUTOMATOR,
                  'new UiSelector().textContains("✗")')

    # --- Actions ---
    def is_displayed(self, timeout=10):
        wait = WebDriverWait(self.driver, timeout)
        el = wait.until(EC.presence_of_element_located(self._title))
        return el.is_displayed()

    def fill_config(self, base_url, model_id, api_key, context_size="4096"):
        self._clear_and_type(self._base_url, base_url)
        self._clear_and_type(self._model_id, model_id)
        self._clear_and_type(self._api_key, api_key)
        self._clear_and_type(self._context_size, context_size)

    def tap_test_connection(self):
        self.driver.find_element(*self._test_btn).click()

    def tap_save(self):
        self.driver.find_element(*self._save_btn).click()

    def get_success_text(self, timeout=15):
        wait = WebDriverWait(self.driver, timeout)
        el = wait.until(EC.presence_of_element_located(self._success_msg))
        return el.text

    def get_error_text(self, timeout=10):
        wait = WebDriverWait(self.driver, timeout)
        el = wait.until(EC.presence_of_element_located(self._error_msg))
        return el.text

    def save_full_config(self, base_url, model_id, api_key, context_size="4096"):
        self.fill_config(base_url, model_id, api_key, context_size)
        self.tap_test_connection()
        self.get_success_text()
        self.tap_save()

    def _clear_and_type(self, locator, text):
        el = self.driver.find_element(*locator)
        el.clear()
        el.send_keys(text)
