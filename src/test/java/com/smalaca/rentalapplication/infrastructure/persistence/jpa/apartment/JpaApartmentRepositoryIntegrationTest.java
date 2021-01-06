package com.smalaca.rentalapplication.infrastructure.persistence.jpa.apartment;

import com.google.common.collect.ImmutableMap;
import com.smalaca.rentalapplication.domain.apartment.Apartment;
import com.smalaca.rentalapplication.domain.apartment.ApartmentAssertion;
import com.smalaca.rentalapplication.domain.apartment.ApartmentFactory;
import com.smalaca.rentalapplication.domain.apartment.ApartmentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Tag("DomainRepositoryIntegrationTest")
class JpaApartmentRepositoryIntegrationTest {
    private static final String OWNER_ID = "1234";
    private static final String STREET = "Florianska";
    private static final String POSTAL_CODE = "12-345";
    private static final String HOUSE_NUMBER = "1";
    private static final String APARTMENT_NUMBER = "13";
    private static final String CITY = "Cracow";
    private static final String COUNTRY = "Poland";
    private static final String DESCRIPTION = "Nice place to stay";
    private static final Map<String, Double> ROOMS_DEFINITION = ImmutableMap.of("Toilet", 10.0, "Bedroom", 30.0);

    @Autowired private ApartmentRepository apartmentRepository;
    @Autowired private SpringJpaApartmentTestRepository springJpaApartmentTestRepository;

    private final ApartmentFactory apartmentFactory = new ApartmentFactory();
    private final List<String> apartmentIds = new ArrayList<>();

    @AfterEach
    void deleteApartments() {
        springJpaApartmentTestRepository.deleteAll(apartmentIds);
    }

    @Test
    void shouldThrowExceptionWhenApartmentDoesNotExist() {
        String nonExistingApartmentId = UUID.randomUUID().toString();

        ApartmentDoesNotExistException actual = assertThrows(ApartmentDoesNotExistException.class, () -> {
            apartmentRepository.findById(nonExistingApartmentId);
        });

        assertThat(actual).hasMessage("Apartment with id " + nonExistingApartmentId + " does not exist");
    }

    @Test
    @Transactional
    void shouldReturnExistingApartment() {
        String existingId = givenExistingApartment(createApartment());

        Apartment actual = apartmentRepository.findById(existingId);

        ApartmentAssertion.assertThat(actual)
                .hasOwnerIdEqualsTo(OWNER_ID)
                .hasDescriptionEqualsTo(DESCRIPTION)
                .hasAddressEqualsTo(STREET, POSTAL_CODE, HOUSE_NUMBER, APARTMENT_NUMBER, CITY, COUNTRY)
                .hasRoomsEqualsTo(ROOMS_DEFINITION);
    }

    @Test
    @Transactional
    void shouldReturnExistingApartmentWeWant() {
        Apartment apartment1 = apartmentFactory.create("1234", "Florianska", "98-765", "12", "34", "Krakow", "Poland", "The greatest apartment", ImmutableMap.of("Room1", 50.0));
        givenExistingApartment(apartment1);
        String existingId = givenExistingApartment(createApartment());
        Apartment apartment2 = apartmentFactory.create("5692", "Florianska", "98-999", "10", "42", "Krakow", "Poland", "Great apartment", ImmutableMap.of("Room42", 100.0));
        givenExistingApartment(apartment2);
        Apartment apartment3 = apartmentFactory.create("2083", "Florianska", "98-123", "11", "13", "Krakow", "Poland", "Not so bad apartment", ImmutableMap.of("Room13", 30.0));
        givenExistingApartment(apartment3);

        Apartment actual = apartmentRepository.findById(existingId);

        ApartmentAssertion.assertThat(actual)
                .hasOwnerIdEqualsTo(OWNER_ID)
                .hasDescriptionEqualsTo(DESCRIPTION)
                .hasAddressEqualsTo(STREET, POSTAL_CODE, HOUSE_NUMBER, APARTMENT_NUMBER, CITY, COUNTRY)
                .hasRoomsEqualsTo(ROOMS_DEFINITION);
    }

    private String givenExistingApartment(Apartment apartment3) {
        String apartmentId = apartmentRepository.save(apartment3);
        apartmentIds.add(apartmentId);

        return apartmentId;
    }

    private Apartment createApartment() {
        return apartmentFactory.create(
                OWNER_ID, STREET, POSTAL_CODE, HOUSE_NUMBER, APARTMENT_NUMBER, CITY, COUNTRY,
                DESCRIPTION, ROOMS_DEFINITION);
    }
}