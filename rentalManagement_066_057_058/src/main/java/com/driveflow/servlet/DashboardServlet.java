package com.driveflow.servlet;
import com.driveflow.store.DataStore; import com.driveflow.model.*; import javax.servlet.*; import javax.servlet.http.*; import java.io.IOException;
public class DashboardServlet extends HttpServlet {
 protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
  int available=0,rented=0,maintenance=0; for(Vehicle v:DataStore.vehicles){if(v.getStatus()==Vehicle.Status.AVAILABLE)available++;else if(v.getStatus()==Vehicle.Status.RENTED)rented++;else maintenance++;}
  int active=0;for(Rental r:DataStore.rentals)if(r.getStatus()==Rental.Status.ACTIVE)active++;
  req.setAttribute("available",available);req.setAttribute("rented",rented);req.setAttribute("maintenance",maintenance);req.setAttribute("active",active);req.setAttribute("revenue",DataStore.revenue());
  req.setAttribute("recentRentals",DataStore.rentals);req.setAttribute("vehicles",DataStore.vehicles);req.getRequestDispatcher("/views/dashboard.jsp").forward(req,resp);
 }
}
