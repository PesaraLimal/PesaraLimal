package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Driver;
import model.RideBooking;
import service.BookQueServ;
import service.DriverAvailServ;
import service.DriverServ;
import service.FileHandle;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class BookingServlet extends HttpServlet {
    private FileHandle fileHandle;
    private BookQueServ bookQueServ;
    private DriverServ driverServ;
    private DriverAvailServ driverAvailServ;

    @Override
    public void init() throws ServletException {
        fileHandle = new FileHandle();
        bookQueServ = new BookQueServ();
        driverServ = new DriverServ();
        driverAvailServ = new DriverAvailServ();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        // Check if user is logged in and is a rider
        if (session == null || !"user".equalsIgnoreCase((String) session.getAttribute("role"))) {
            response.sendRedirect("pages/login.jsp?role=user");
            return;
        }

        try {
            String contextPath = getServletContext().getRealPath("/");
            // Fetch available drivers
            List<Driver> availableDrivers = fileHandle.getAvailableDrivers(contextPath);
            // Sort drivers by rating
            availableDrivers = driverServ.sortDriversByRating(availableDrivers);

            // Set attributes for JSP
            request.setAttribute("availableDrivers", availableDrivers);
            request.getRequestDispatcher("pages/booking.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Failed to load booking page: " + e.getMessage());
            request.getRequestDispatcher("pages/booking.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !"user".equalsIgnoreCase((String) session.getAttribute("role"))) {
            response.sendRedirect("pages/login.jsp?role=user");
            return;
        }

        String action = request.getParameter("action");
        String riderId = (String) session.getAttribute("riderId");
        String contextPath = getServletContext().getRealPath("/");

        try {
            if ("bookRide".equals(action)) {
                String pickup = request.getParameter("pickup");
                String dropoff = request.getParameter("dropoff");
                String bookingDate = request.getParameter("bookingDate"); // YYYY-MM-DD
                String bookingTime = request.getParameter("bookingTime"); // HH:MM
                String fareStr = request.getParameter("fare");

                // Validate inputs
                if (pickup == null || dropoff == null || bookingDate == null || bookingTime == null || fareStr == null ||
                        pickup.trim().isEmpty() || dropoff.trim().isEmpty() || bookingDate.trim().isEmpty() ||
                        bookingTime.trim().isEmpty() || fareStr.trim().isEmpty()) {
                    request.setAttribute("error", "Please fill in all booking details.");
                    doGet(request, response);
                    return;
                }

                try {
                    double fare = Double.parseDouble(fareStr);

                    // Get available drivers
                    List<Driver> availableDrivers = fileHandle.getAvailableDrivers(contextPath);
                    Driver assignedDriver = null;

                    // Check availability and sort by rating
                    for (Driver driver : driverServ.sortDriversByRating(availableDrivers)) {
                        if (driverAvailServ.isDriverAvailable(driver.getId(), bookingDate, bookingTime, contextPath)) {
                            assignedDriver = driver;
                            break;
                        }
                    }

                    if (assignedDriver == null) {
                        request.setAttribute("error", "No drivers available at the specified time.");
                        doGet(request, response);
                        return;
                    }

                    // Create booking
                    String bookingId = UUID.randomUUID().toString();
                    RideBooking booking = new RideBooking(bookingId, riderId, assignedDriver.getId(), pickup, dropoff,
                            fare, LocalDateTime.now(), "pending"); // Added LocalDateTime.now()

                    // Add to FIFO queue
                    bookQueServ.enqueueBooking(booking);

                    // Save to bookings.txt
                    fileHandle.saveBooking(booking, contextPath);

                    request.setAttribute("success", "Ride booked successfully with Driver: " + assignedDriver.getName());
                } catch (NumberFormatException e) {
                    request.setAttribute("error", "Invalid fare format.");
                }
            } else {
                request.setAttribute("error", "Invalid action.");
            }
            doGet(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Booking failed: " + e.getMessage());
            doGet(request, response);
        }
    }
}