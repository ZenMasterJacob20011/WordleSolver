package org.example;

public class Login {
    private String login;
    private String password;
    private boolean undelete;
    private String captcha_key;
    private String login_source;
    private String gift_code_sku_id;

    public Login(String login, String password, boolean undelete, String captcha_key, String login_source, String gift_code_sku_id) {
        this.login = login;
        this.password = password;
        this.undelete = undelete;
        this.captcha_key = captcha_key;
        this.login_source = login_source;
        this.gift_code_sku_id = gift_code_sku_id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isUndelete() {
        return undelete;
    }

    public void setUndelete(boolean undelete) {
        this.undelete = undelete;
    }

    public String getCaptcha_key() {
        return captcha_key;
    }

    public void setCaptcha_key(String captcha_key) {
        this.captcha_key = captcha_key;
    }

    public String getLogin_source() {
        return login_source;
    }

    public void setLogin_source(String login_source) {
        this.login_source = login_source;
    }

    public String getGift_code_sku_id() {
        return gift_code_sku_id;
    }

    public void setGift_code_sku_id(String gift_code_sku_id) {
        this.gift_code_sku_id = gift_code_sku_id;
    }
}
