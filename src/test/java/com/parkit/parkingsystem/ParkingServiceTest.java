package com.parkit.parkingsystem;
import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    @BeforeEach
    private void setUpPerTest() {
        try {
            Mockito.lenient().when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            Mockito.lenient().when(inputReaderUtil.readSelection()).thenReturn(9);
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            Mockito.lenient().when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            Mockito.lenient().when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

            Mockito.lenient().when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void processExitingVehicleTest() {
        parkingService.processExitingVehicle();
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
    }

    @Test
    public void testProcessIncomingVehicle() {
        parkingService.processIncomingVehicle();
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));

    }

    @Test
    public void processExitingVehicleTestUnableUpdate() {
        parkingService.processExitingVehicle();
        Mockito.lenient().when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
    }

    @Test
    public void testGetNextParkingNumberIfAvailable() {
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
        assertEquals(1, parkingSpot.getId());


    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable(); //declare variable
        assertEquals(null, parkingSpot);//If test returns a parking space, test fails

    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {

        int incorrectVehicleType = 2;
        when(inputReaderUtil.readSelection()).thenReturn(incorrectVehicleType);
        assertEquals(null, parkingService.getNextParkingNumberIfAvailable());
        //ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable(incorrectVehicleType);
    }

    @Test
    public void testParkingLotExitRecurringUser() {
        // Given: Recurring user's vehicle
        String recurringUserVehicleRegNumber = "ABCDEF"; // The recurring user's vehicle registration number

        // Mock the behavior for a recurring user's ticket retrieval
        Ticket recurringUserTicket = new Ticket();
        recurringUserTicket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        recurringUserTicket.setVehicleRegNumber(recurringUserVehicleRegNumber);
        when(ticketDAO.getTicket(eq(recurringUserVehicleRegNumber))).thenReturn(recurringUserTicket);

        // When: Recurring user exits the parking lot
        parkingService.processExitingVehicle();

        // Then: Verify the price calculation with 5% discount
        verify(ticketDAO, times(1)).updateTicket(any(Ticket.class)); // Ensure ticket update is called

        // Calculate the expected price for a recurring user with 5% discount
        long expectedPrice = (long) (recurringUserTicket.getDuration() * Fare.CAR_RATE_PER_HOUR * 0.95);
        assertEquals(expectedPrice, recurringUserTicket.getPrice());
    }


}





