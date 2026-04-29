from pages.base_page import BasePage


class LoginPage(BasePage):
    SERVER_URL_INPUT = "login_server_url"
    USERNAME_INPUT = "login_username"
    PASSWORD_INPUT = "login_password"
    SIGN_IN_BUTTON = "login_sign_in_button"
    ERROR_MESSAGE = "login_error_message"

    def is_loaded(self, timeout=10):
        return self.is_element_present(self.SERVER_URL_INPUT, timeout=timeout)

    def enter_server_url(self, url: str):
        self.type_text(self.SERVER_URL_INPUT, url)

    def enter_username(self, username: str):
        self.type_text(self.USERNAME_INPUT, username)

    def enter_password(self, password: str):
        self.type_text(self.PASSWORD_INPUT, password)

    def click_sign_in(self):
        self.click(self.SIGN_IN_BUTTON)

    def get_error_message(self) -> str:
        return self.get_element_text(self.ERROR_MESSAGE)

    def has_error(self, timeout=5) -> bool:
        return self.is_element_present(self.ERROR_MESSAGE, timeout=timeout)

    def login(self, server_url: str, username: str, password: str):
        self.enter_server_url(server_url)
        self.enter_username(username)
        self.enter_password(password)
        self.click_sign_in()
