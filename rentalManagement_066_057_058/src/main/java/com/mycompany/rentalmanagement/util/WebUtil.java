package com.mycompany.rentalmanagement.util;
import jakarta.servlet.http.HttpServletRequest;
public final class WebUtil {
 private WebUtil(){}
 public static String p(HttpServletRequest r,String key){String v=r.getParameter(key);return v==null?"":v.trim();}
 public static int intParam(HttpServletRequest r,String key){return Integer.parseInt(p(r,key));}
 public static String esc(Object o){if(o==null)return "";return o.toString().replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&#39;");}
}
