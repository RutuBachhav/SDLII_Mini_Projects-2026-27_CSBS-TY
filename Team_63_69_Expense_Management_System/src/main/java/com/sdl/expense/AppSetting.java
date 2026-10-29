package com.sdl.expense;

import javax.persistence.*;

@Entity @Table(name="app_settings")
public class AppSetting {
    @Id @Column(name="setting_key", length=64) private String key;
    @Column(name="setting_value", nullable=false, length=64) private String value;
    protected AppSetting() {}
    public AppSetting(String key,String value){this.key=key;this.value=value;}
    public String getValue(){return value;}
    public void setValue(String value){this.value=value;}
}
