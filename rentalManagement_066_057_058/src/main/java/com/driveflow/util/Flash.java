package com.driveflow.util;
import javax.servlet.http.HttpSession;
public final class Flash { private Flash(){} public static void set(HttpSession s,String type,String message){s.setAttribute("flashType",type);s.setAttribute("flashMessage",message);} }
