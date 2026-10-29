package com.driveflow.store;

import com.driveflow.model.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class DataStore {
    public static final List<Vehicle> vehicles = Collections.synchronizedList(new ArrayList<Vehicle>());
    public static final List<Customer> customers = Collections.synchronizedList(new ArrayList<Customer>());
    public static final List<Rental> rentals = Collections.synchronizedList(new ArrayList<Rental>());
    public static final List<Payment> payments = Collections.synchronizedList(new ArrayList<Payment>());
    private static final AtomicInteger vehicleSeq=new AtomicInteger(6), customerSeq=new AtomicInteger(6), rentalSeq=new AtomicInteger(4), paymentSeq=new AtomicInteger(4);
    static { seed(); }
    private DataStore() {}
    private static void seed(){
        vehicles.add(new Vehicle(1,"MH12AB1234","Toyota","Innova Crysta","SUV","Diesel",2023,3200,Vehicle.Status.AVAILABLE));
        vehicles.add(new Vehicle(2,"MH14CD5678","Hyundai","Creta","SUV","Petrol",2024,2400,Vehicle.Status.RENTED));
        vehicles.add(new Vehicle(3,"MH12EF9012","Honda","City","Sedan","Petrol",2022,1900,Vehicle.Status.AVAILABLE));
        vehicles.add(new Vehicle(4,"MH15GH3456","Tata","Nexon EV","Hatchback","Electric",2024,2200,Vehicle.Status.MAINTENANCE));
        vehicles.add(new Vehicle(5,"MH14JK7890","Maruti","Ertiga","MUV","CNG",2023,2100,Vehicle.Status.AVAILABLE));
        customers.add(new Customer(1,"Aarav Sharma","aarav@example.com","9876543210","DL-0420210001","Pune, Maharashtra"));
        customers.add(new Customer(2,"Priya Patil","priya@example.com","9876501234","MH-1220230045","Mumbai, Maharashtra"));
        customers.add(new Customer(3,"Rohan Deshmukh","rohan@example.com","9822001122","MH-1220220099","Nashik, Maharashtra"));
        customers.add(new Customer(4,"Sneha Kulkarni","sneha@example.com","9766003344","MH-1220240017","Pune, Maharashtra"));
        customers.add(new Customer(5,"Aditya Joshi","aditya@example.com","9890005566","MH-1220210077","Satara, Maharashtra"));
        Rental r1=new Rental(1,2,1,LocalDate.now().minusDays(1),LocalDate.now().plusDays(2),7200,Rental.Status.ACTIVE);
        Rental r2=new Rental(2,1,2,LocalDate.now().minusDays(12),LocalDate.now().minusDays(9),9600,Rental.Status.RETURNED);
        Rental r3=new Rental(3,3,3,LocalDate.now().minusDays(7),LocalDate.now().minusDays(5),3800,Rental.Status.RETURNED);
        rentals.add(r1);rentals.add(r2);rentals.add(r3);
        payments.add(new Payment(1,1,7200,Payment.Method.UPI,Payment.Status.PAID,LocalDateTime.now().minusHours(3)));
        payments.add(new Payment(2,2,9600,Payment.Method.CARD,Payment.Status.PAID,LocalDateTime.now().minusDays(9)));
        payments.add(new Payment(3,3,3800,Payment.Method.CASH,Payment.Status.PAID,LocalDateTime.now().minusDays(5)));
    }
    public static Vehicle vehicle(int id){for(Vehicle v:vehicles)if(v.getId()==id)return v;return null;}
    public static Customer customer(int id){for(Customer c:customers)if(c.getId()==id)return c;return null;}
    public static Rental rental(int id){for(Rental r:rentals)if(r.getId()==id)return r;return null;}
    public static int nextVehicleId(){return vehicleSeq.getAndIncrement();}
    public static int nextCustomerId(){return customerSeq.getAndIncrement();}
    public static int nextRentalId(){return rentalSeq.getAndIncrement();}
    public static int nextPaymentId(){return paymentSeq.getAndIncrement();}
    public static double revenue(){double x=0;for(Payment p:payments)if(p.getStatus()==Payment.Status.PAID)x+=p.getAmount();return x;}
}
