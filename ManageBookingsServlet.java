package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.RideBooking;
import model.Rider;
import model.Driver;
import service.FileHandle;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class ManageBookingsServlet extends HttpServlet {
    private FileHandle fileHandle;

    @Override
    public void init() throws ServletException {
        fileHandle = new FileHandle();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Check if user is admin
        String role = (String) request.getSession().getAttribute("role");
        if (!"admin".equals(role)) {
            response.sendRedirect(request.getContextPath() + "/pages/login.jsp?role=admin");
            return;
        }

        try {
            String contextPath = getServletContext().getRealPath("");
            List<RideBooking> bookings = fileHandle.getAllBookings(contextPath);
            List<Rider> riders = fileHandle.getAllUsers(contextPath);
            List<Driver> drivers = fileHandle.getAllDrivers(contextPath);
            request.setAttribute("bookings", bookings);
            request.setAttribute("riders", riders);
            request.setAttribute("drivers", drivers);
            request.getRequestDispatcher("/pages/manageBookings.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Error loading bookings: " + e.getMessage());
            request.getRequestDispatcher("/pages/manageBookings.jsp").forward(request, response);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        String contextPath = getServletContext().getRealPath("");

        try {
            if ("addBooking".equals(action)) {
                String bookingId = request.getParameter("bookingId");
                String riderId = request.getParameter("riderId");
                String driverId = request.getParameter("driverId");
                String pickup = request.getParameter("pickup");
                String drop = request.getParameter("drop");
                double amount = Double.parseDouble(request.getParameter("amount"));
                String status = request.getParameter("status");
                RideBooking booking = new RideBooking(bookingId, riderId, driverId, pickup, drop, amount, LocalDateTime.now(), status);
                fileHandle.saveBooking(booking, contextPath);
                request.setAttribute("success", "Booking added successfully.");
            } else if ("editBooking".equals(action)) {
                String bookingId = request.getParameter("bookingId");
                String riderId = request.getParameter("riderId");
                String driverId = request.getParameter("driverId");
                String pickup = request.getParameter("pickup");
                String drop = request.getParameter("drop");
                double amount = Double.parseDouble(request.getParameter("amount"));
                String status = request.getParameter("status");
                RideBooking booking = new RideBooking(bookingId, riderId, driverId, pickup, drop, amount, LocalDateTime.now(), status);
                fileHandle.saveBooking(booking, contextPath); // Overwrites existing booking
                request.setAttribute("success", "Booking updated successfully.");
            } else if ("deleteBooking".equals(action)) {
                String bookingId = request.getParameter("bookingId");
                fileHandle.updateBookingStatus(bookingId, "cancelled", contextPath); // Treat delete as cancelling
                request.setAttribute("success", "Booking cancelled successfully.");
            }

            doGet(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Error processing action: " + e.getMessage());
            doGet(request, response);
        }
    }
}