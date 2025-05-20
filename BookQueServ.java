package service;

import model.RideBooking;

import java.util.LinkedList;
import java.util.Queue;

public class BookQueServ {
    private final Queue<RideBooking> bookingQueue;

    public BookQueServ() {
        this.bookingQueue = new LinkedList<>();
    }

    /**
     * Adds a booking to the FIFO queue.
     *
     * @param booking The RideBooking to enqueue.
     */
    public void enqueueBooking(RideBooking booking) {
        if (booking != null) {
            bookingQueue.offer(booking);
        }
    }
}