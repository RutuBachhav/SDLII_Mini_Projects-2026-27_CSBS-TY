package com.sdl.expense;

import javax.persistence.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@WebServlet("/expenses")
public class ExpenseServlet extends HttpServlet {
    private static final Map<String,String> CURRENCIES;
    static {
        Map<String,String> options=new LinkedHashMap<>();
        options.put("USD","$"); options.put("INR","₹"); options.put("EUR","€"); options.put("GBP","£");
        options.put("JPY","¥"); options.put("CAD","CA$"); options.put("AUD","A$"); options.put("CHF","CHF");
        options.put("CNY","¥"); options.put("SGD","S$"); options.put("NZD","NZ$"); options.put("AED","د.إ");
        CURRENCIES=Collections.unmodifiableMap(options);
    }
    private EntityManagerFactory emf;
    @Override public void init() throws ServletException {
        java.util.Map<String,Object> db=new java.util.HashMap<>();
        db.put("javax.persistence.jdbc.driver","com.mysql.cj.jdbc.Driver");
        db.put("javax.persistence.jdbc.url",System.getenv().getOrDefault("DB_URL","jdbc:mysql://localhost:3306/expense_management?useSSL=false&serverTimezone=UTC"));
        db.put("javax.persistence.jdbc.user",System.getenv().getOrDefault("DB_USER","root"));
        String password=System.getenv("DB_PASSWORD");
        if(password==null) throw new ServletException("Set DB_PASSWORD in the Tomcat environment before starting the application.");
        db.put("javax.persistence.jdbc.password",password);
        try { emf=Persistence.createEntityManagerFactory("expensePU",db); }
        catch(RuntimeException ex){ throw new ServletException("Unable to connect to MySQL. Check DB_URL, DB_USER, DB_PASSWORD and database availability.",ex); }
    }
    @Override public void destroy(){ if(emf!=null) emf.close(); }

    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp) throws ServletException,IOException {
        EntityManager em=emf.createEntityManager();
        try {
            String search=req.getParameter("q");
            List<Expense> expenses;
            if(search==null || search.trim().isEmpty()) expenses=em.createQuery("SELECT e FROM Expense e ORDER BY e.expenseDate DESC, e.id DESC",Expense.class).getResultList();
            else expenses=em.createQuery("SELECT e FROM Expense e WHERE LOWER(e.title) LIKE :q OR LOWER(e.category) LIKE :q ORDER BY e.expenseDate DESC",Expense.class).setParameter("q","%"+search.trim().toLowerCase()+"%").getResultList();
            req.setAttribute("expenses",expenses);
            req.setAttribute("total",em.createQuery("SELECT COALESCE(SUM(e.amount),0) FROM Expense e",BigDecimal.class).getSingleResult());
            LocalDate monthStart=LocalDate.now().withDayOfMonth(1);
            req.setAttribute("monthTotal",em.createQuery("SELECT COALESCE(SUM(e.amount),0) FROM Expense e WHERE e.expenseDate>=:start AND e.expenseDate<:end",BigDecimal.class).setParameter("start",monthStart).setParameter("end",monthStart.plusMonths(1)).getSingleResult());
            LocalDate previousStart=monthStart.minusMonths(1);
            BigDecimal previousTotal=em.createQuery("SELECT COALESCE(SUM(e.amount),0) FROM Expense e WHERE e.expenseDate>=:start AND e.expenseDate<:end",BigDecimal.class).setParameter("start",previousStart).setParameter("end",monthStart).getSingleResult();
            List<Object[]> categoryRows=em.createQuery("SELECT e.category,SUM(e.amount) FROM Expense e WHERE e.expenseDate>=:start AND e.expenseDate<:end GROUP BY e.category ORDER BY SUM(e.amount) DESC",Object[].class).setParameter("start",previousStart).setParameter("end",monthStart).getResultList();
            List<CategorySpend> categorySpends=new ArrayList<>();
            for(Object[] row:categoryRows){
                BigDecimal amount=(BigDecimal)row[1];
                BigDecimal share=previousTotal.signum()==0?BigDecimal.ZERO:amount.multiply(BigDecimal.valueOf(100)).divide(previousTotal,1,RoundingMode.HALF_UP);
                categorySpends.add(new CategorySpend((String)row[0],amount,share));
            }
            req.setAttribute("previousMonthLabel",previousStart.getMonth().getDisplayName(java.time.format.TextStyle.FULL,Locale.ENGLISH)+" "+previousStart.getYear());
            req.setAttribute("previousMonthTotal",previousTotal); req.setAttribute("categorySpends",categorySpends);
            req.setAttribute("count",em.createQuery("SELECT COUNT(e) FROM Expense e",Long.class).getSingleResult());
            AppSetting currency=em.find(AppSetting.class,"currency");
            String code=currency!=null && CURRENCIES.containsKey(currency.getValue()) ? currency.getValue() : "USD";
            req.setAttribute("currencies",CURRENCIES); req.setAttribute("currentCurrency",code); req.setAttribute("currencySymbol",CURRENCIES.get(code));
            String edit=req.getParameter("edit"); if(edit!=null) req.setAttribute("editing",em.find(Expense.class,Long.valueOf(edit)));
            req.getRequestDispatcher("/WEB-INF/index.jsp").forward(req,resp);
        } catch(RuntimeException ex){ throw new ServletException("Unable to load expenses. Check database configuration.",ex); }
        finally {em.close();}
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp) throws ServletException,IOException {
        req.setCharacterEncoding("UTF-8");
        EntityManager em=emf.createEntityManager();
        try {
            String action=req.getParameter("action"); EntityTransaction tx=em.getTransaction(); tx.begin();
            if("currency".equals(action)) {
                String code=req.getParameter("currency");
                if(!CURRENCIES.containsKey(code)) throw new IllegalArgumentException("Unsupported currency.");
                AppSetting setting=em.find(AppSetting.class,"currency");
                if(setting==null) em.persist(new AppSetting("currency",code)); else setting.setValue(code);
            } else if("delete".equals(action)) { Expense e=em.find(Expense.class,Long.valueOf(req.getParameter("id"))); if(e!=null) em.remove(e); }
            else if("create".equals(action) || "update".equals(action)) {
                String title=req.getParameter("title").trim(), category=req.getParameter("category"), method=req.getParameter("paymentMethod");
                BigDecimal amount=new BigDecimal(req.getParameter("amount")); LocalDate date=LocalDate.parse(req.getParameter("expenseDate"));
                String description=req.getParameter("description");
                if(title.isEmpty() || amount.signum()<=0 || !List.of("Travel","Meals","Food","Office","Software","Utilities","Other").contains(category) || !List.of("Cash","Card","Bank transfer","Other").contains(method)) throw new IllegalArgumentException("Invalid expense details.");
                if("update".equals(action)){ Expense e=em.find(Expense.class,Long.valueOf(req.getParameter("id"))); if(e==null) throw new IllegalArgumentException("Expense not found."); e.update(title,amount,category,date,method,description); }
                else em.persist(new Expense(title,amount,category,date,method,description));
            } else throw new IllegalArgumentException("Unsupported action.");
            tx.commit(); resp.sendRedirect(req.getContextPath()+"/expenses");
        } catch(Exception ex){
            if(em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new ServletException("Could not save the expense. Verify the values and database connection.",ex);
        } finally { em.close(); }
    }
}
