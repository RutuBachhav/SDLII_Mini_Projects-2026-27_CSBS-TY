package com.driveflow.model;

public class Customer {
    private int id; private String name,email,phone,licenseNo,address;
    public Customer() {}
    public Customer(int id,String name,String email,String phone,String licenseNo,String address){this.id=id;this.name=name;this.email=email;this.phone=phone;this.licenseNo=licenseNo;this.address=address;}
    public int getId(){return id;} public void setId(int v){id=v;}
    public String getName(){return name;} public void setName(String v){name=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;}
    public String getPhone(){return phone;} public void setPhone(String v){phone=v;}
    public String getLicenseNo(){return licenseNo;} public void setLicenseNo(String v){licenseNo=v;}
    public String getAddress(){return address;} public void setAddress(String v){address=v;}
}
