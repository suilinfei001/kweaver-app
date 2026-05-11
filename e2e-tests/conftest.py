import pytest
import subprocess
from appium import webdriver
from appium.options.android import UiAutomator2Options

ADB = "C:/Users/Yabo.sui/AppData/Local/Android/Sdk/platform-tools/adb.exe"
ORIGINAL_IME = "com.google.android.inputmethod.latin/com.android.inputmethod.latin.LatinIME"


def _restore_keyboard():
    subprocess.run([ADB, "shell", "settings", "put", "secure",
                    "default_input_method", ORIGINAL_IME],
                   capture_output=True, timeout=10)


def pytest_addoption(parser):
    parser.addoption(
        "--app-path", action="store", default=None,
        help="Path to APK file. If omitted, assumes app is already installed."
    )
    parser.addoption(
        "--device-name", action="store", default="emulator-5554",
        help="Device name or UDID"
    )
    parser.addoption(
        "--udid", action="store", default=None,
        help="Device UDID (overrides device-name for udid capability)"
    )
    parser.addoption(
        "--appium-url", action="store", default="http://127.0.0.1:4723",
        help="Appium server URL"
    )
    parser.addoption(
        "--no-reset", action="store_true", default=False,
        help="Do not reset app state between tests"
    )


@pytest.fixture(scope="session")
def appium_driver(request):
    app_path = request.config.getoption("--app-path")
    device_name = request.config.getoption("--device-name")
    appium_url = request.config.getoption("--appium-url")
    udid = request.config.getoption("--udid")

    options = UiAutomator2Options()
    options.platform_name = "Android"
    options.device_name = device_name
    options.automation_name = "UiAutomator2"

    if udid:
        options.set_capability("udid", udid)
    elif device_name:
        options.set_capability("udid", device_name)

    if app_path:
        options.set_capability("app", app_path)
    else:
        options.set_capability("appPackage", "com.kweaver.dip")
        options.set_capability("appActivity", "com.kweaver.dip.MainActivity")
        options.set_capability("noReset", False)

    options.set_capability("unicodeKeyboard", False)
    options.set_capability("resetKeyboard", False)
    options.set_capability("newCommandTimeout", 300)
    options.set_capability("uiautomator2ServerLaunchTimeout", 60000)
    options.set_capability("adbExecTimeout", 60000)
    options.set_capability("skipDeviceInitialization", True)
    options.set_capability("skipServerInstallation", True)
    options.set_capability("settingsWatchdogInterval", 0)
    options.set_capability("disableWindowAnimation", False)
    options.set_capability("skipUnlock", True)

    driver = webdriver.Remote(appium_url, options=options)

    yield driver

    driver.quit()
    _restore_keyboard()


@pytest.fixture(autouse=True)
def reset_app(appium_driver, request):
    no_reset = request.config.getoption("--no-reset")
    if not no_reset:
        appium_driver.execute_script("mobile: clearApp", {"appId": "com.kweaver.dip"})
        appium_driver.activate_app("com.kweaver.dip")
    yield
