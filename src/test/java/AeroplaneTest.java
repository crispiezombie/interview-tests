import org.example.model.Passenger;
import org.example.model.Aeroplane;
import org.example.model.PassengerSevice;
import org.example.model.PassengerType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class AeroplaneTest {
    private Aeroplane aeroplane;
    private UUID economyPassengerId;
    private Passenger economyPassenger;
    private Passenger businessClassPassenger;
    private Passenger firstClassPassenger;

    private final PassengerService passengerService = new PassengerService();

    @BeforeEach
    void setUp() {
        aeroplane = new Aeroplane();
        economyPassengerId = UUID.randomUUID();
        economyPassenger = new Passenger(
                economyPassengerId,
                "Steve",
                PassengerType.ECONOMY,
                3,
                50.0,
                "Economy Passenger",
                "13A"
        );
        businessClassPassenger = new Passenger(
                UUID.randomUUID(),
                "Phil",
                PassengerType.BUSINESS_CLASS,
                10,
                66.90,
                "Business Class Passenger",
                "9C"
        );
        firstClassPassenger = new Passenger(
                UUID.randomUUID(),
                "Sophie",
                PassengerType.FIRST_CLASS,
                3,
                100.73,
                null,
                "9A"
        );
    }

    @Test
    void shouldAddPassengerOnEnter() {
        assertEquals("Welcome Economy Passenger Steve", aeroplane.enter(economyPassenger));
        assertEquals("Welcome Business Class Passenger Phil", aeroplane.enter(businessClassPassenger));
        assertEquals(2, aeroplane.countPassengers());
        assertEquals(1, aeroplane.countPassengersByType(PassengerType.ECONOMY));
        assertEquals(1, aeroplane.countPassengersByType(PassengerType.BUSINESS_CLASS));
    }

    @Test
    void shouldRemovePassengerOnExit() {
        aeroplane.bulkEnter(economyPassenger, businessClassPassenger);
        assertEquals(2, aeroplane.countPassengers());

        aeroplane.exit(economyPassengerId);
        assertEquals(0, aeroplane.countPassengersByType(PassengerType.ECONOMY));
        assertEquals(1, aeroplane.countPassengers());
        assertEquals(1, aeroplane.countPassengersByType(PassengerType.BUSINESS_CLASS));
    }

    @Test
    void shouldMapPassengersByType() {
        aeroplane.bulkEnter(economyPassenger, businessClassPassenger);

        var result = aeroplane.mapPassengersByType();
        assertEquals(2, result.size());
        assertSame(economyPassenger, result.get(PassengerType.ECONOMY));
        assertSame(businessClassPassenger, result.get(PassengerType.BUSINESS_CLASS));
    }

    @Test
    void shouldOrderPassengersByFare() {
        aeroplane.bulkEnter(economyPassenger, businessClassPassenger, firstClassPassenger);

        var result = aeroplane.orderPassengersByFare();
        assertEquals(PassengerType.ECONOMY, result.get(0).getType());
        assertEquals(PassengerType.BUSINESS_CLASS, result.get(1).getType());
        assertEquals(PassengerType.FIRST_CLASS, result.get(2).getType());
    }

    @Test
    void shouldCountTotalFareOnTheAeroplane() {
        aeroplane.bulkEnter(economyPassenger, businessClassPassenger, firstClassPassenger);

        assertEquals(217.63, aeroplane.totalFare());
    }

    @Test
    void shouldFilterPassengersByType() {
        var result = passengerService.filterPassengersByType(
                List.of(economyPassenger, businessClassPassenger, firstClassPassenger),
                PassengerType.ECONOMY
        );
        assertEquals(1, result.size());
        assertEquals(PassengerType.ECONOMY, result.get(0).getType());
    }

    @Test
    void shouldFilterPassengersByFare() {
        var result = passengerService.filterPassengersByFareAsInt(
                List.of(
                        economyPassenger,
                        businessClassPassenger,
                        firstClassPassenger
                ), 60
        );
        assertEquals(1, result.size());
        assertEquals(3, result.get(0).getLuggageCount());
    }

    @Test
    void shouldConvertToAnyPassengerToFirstClass() {
        var passenger = new Passenger(
                UUID.randomUUID(),
                "Joe",
                PassengerType.ECONOMY,
                null,
                null,
                null,
                null
        );
        var result = passengerService.upgradeToFirstClass(passenger);
        assertEquals(PassengerType.FIRST_CLASS, result.getType());
    }

    @Test
    void shouldComputeCorrectTotalCostForACustomerWithLuggage() {
        var passenger = new Passenger(
                UUID.randomUUID(),
                "Joe",
                PassengerType.ECONOMY,
                2,
                45.0,
                null,
                null
        );
        var result = passengerService.computeTotalCost(passenger);
        assertEquals(55.0, result);
    }

    @Test
    void shouldComputeCorrectTotalCostForACustomerWithoutLuggage() {
        var passenger = new Passenger(
                UUID.randomUUID(),
                "Joe",
                PassengerType.ECONOMY,
                0,
                45.0,
                null,
                null
        );
        var result = passengerService.computeTotalCost(passenger);
        assertEquals(45.0, result);
    }

    @Test
    void shouldReturnBoardingOrder() {
        assertEquals(3, passengerService.boardOrder(economyPassenger));
        assertEquals(2, passengerService.boardOrder(businessClassPassenger));
        assertEquals(1, passengerService.boardOrder(firstClassPassenger));
    }

    @Test
    void shouldSortPassengersBySeatNumber() {
        var passengerList = List.of(economyPassenger, businessClassPassenger, firstClassPassenger);
        var sortedPassengers = passengerService.sortBySeatNumber(passengerList);
        assertEquals(sortedPassengers, passengerList);
    }

    @Test
    void shouldFindPassengerIdBySeatNumber() {
        var passengerList = List.of(economyPassenger, businessClassPassenger, firstClassPassenger);
        assertEquals(
                businessClassPassenger.getId(),
                passengerService.findPassengerIdBySeatNumber(passengerList, businessClassPassenger.getSeatNumber())
        );
    }

    @Test
    void shouldFindPassengerIdWithLowestSeatNumber() {
        var passengerList = List.of(economyPassenger, businessClassPassenger, firstClassPassenger);
        assertEquals(
                firstClassPassenger.getId(),
                passengerService.findPassengerIdWithLowestSeatNumber(passengerList)
        );
    }

    @Test
    void shouldFindMostCommonPassengerType() {
        var businessPassenger2 = new Passenger(
                UUID.randomUUID(),
                "Joe",
                PassengerType.BUSINESS_CLASS,
                2,
                62.0,
                null,
                null
        );

        assertEquals(
                PassengerType.BUSINESS_CLASS,
                passengerService.findMostCommonPassengerType(
                        List.of(
                                economyPassenger,
                                businessClassPassenger,
                                firstClassPassenger, businessPassenger2
                        )
                )
        );
    }

    @Test
    void shouldReturnNullWhenNoPassengers() {
        assertEquals(null, passengerService.findMostCommonPassengerType(List.of()));
    }
}