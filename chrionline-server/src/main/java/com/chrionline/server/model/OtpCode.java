package com.chrionline.server.model;

import java.io.Serializable;
import java.util.Date;

public class OtpCode implements Serializable {
    private static final long serialVersionUID = 1L;

    private int     id;
    private String  email;
    private String  code;
    private String  type;       // INSCRIPTION ou MOT_DE_PASSE
    private Date    expireAt;
    private boolean utilise;
    private Date    createdAt;

    public OtpCode() {}

    public OtpCode(String email, String code, String type, Date expireAt) {
        this.email    = email;
        this.code     = code;
        this.type     = type;
        this.expireAt = expireAt;
        this.utilise  = false;
    }

    public int     getId()       { return id; }
    public String  getEmail()    { return email; }
    public String  getCode()     { return code; }
    public String  getType()     { return type; }
    public Date    getExpireAt() { return expireAt; }
    public boolean isUtilise()   { return utilise; }
    public Date    getCreatedAt(){ return createdAt; }

    public void setId(int id)           { this.id = id; }
    public void setEmail(String e)      { this.email = e; }
    public void setCode(String c)       { this.code = c; }
    public void setType(String t)       { this.type = t; }
    public void setExpireAt(Date d)     { this.expireAt = d; }
    public void setUtilise(boolean u)   { this.utilise = u; }
    public void setCreatedAt(Date d)    { this.createdAt = d; }

    public boolean isExpire() {
        return new Date().after(expireAt);
    }
}