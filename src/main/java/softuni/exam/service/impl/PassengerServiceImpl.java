package softuni.exam.service.impl;

import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.models.Dto.PassengerSeedDto;
import softuni.exam.models.entity.Passenger;
import softuni.exam.models.entity.Town;
import softuni.exam.repository.PassengerRepository;
import softuni.exam.service.PassengerService;
import softuni.exam.service.TownService;
import softuni.exam.util.ValidationUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Service
public class PassengerServiceImpl implements PassengerService {
    private static final String PASSENGER_FILE_PATH = "src/main/resources/files/json/passengers.json";

    private final PassengerRepository passengerRepository;
    private final ModelMapper modelMapper;
    private final Gson gson;
    private final TownService townService;

    public PassengerServiceImpl(PassengerRepository passengerRepository, ModelMapper modelMapper, Gson gson, TownService townService, ValidationUtil validationUtil) {
        this.passengerRepository = passengerRepository;
        this.modelMapper = modelMapper;
        this.gson = gson;
        this.townService = townService;
        this.validationUtil = validationUtil;
    }

    private final ValidationUtil validationUtil;




    @Override
    public boolean areImported() {
        return passengerRepository.count() > 0;
    }

    @Override
    public String readPassengersFileContent() throws IOException {
        return Files.readString(Path.of(PASSENGER_FILE_PATH));
    }

    @Override
    public String importPassengers() throws IOException {
        StringBuilder sb = new StringBuilder();

        Arrays.stream(gson.fromJson(readPassengersFileContent(), PassengerSeedDto[].class))
                .filter(passengerSeedDto -> {
                    boolean isValid = validationUtil.isValid(passengerSeedDto);
                    sb.append(isValid ? String.format("Successfully imported Passenger %s - %s",
                            passengerSeedDto.getLastName(), passengerSeedDto.getEmail()): "Invalid Passenger")
                            .append(System.lineSeparator());
                    return isValid;
                })
                .map(passengerSeedDto -> {
                    Passenger passenger = modelMapper.map(passengerSeedDto, Passenger.class);
                    Town town = this.townService.getTownByName(passengerSeedDto.getTown());
                    passenger.setTown(town);
                    return passenger;
                })

                .forEach(passengerRepository::save);



        return sb.toString();
    }

    @Override
    public String getPassengersOrderByTicketsCountDescendingThenByEmail() {
        StringBuilder sb = new StringBuilder();
        passengerRepository.findPassengersOrderByTicketsCountThenByEmail()
                .forEach(passenger -> {
                    sb
                            .append(String.format("Passenger %s  %s\n" +
                                    "\tEmail - %s\n" +
                                    "Phone - %s\n" +
                                    "\tNumber of tickets - %d",
                                    passenger.getFirstName(), passenger.getLastName(), passenger.getEmail(),
                                    passenger.getPhoneNumber(), passenger.getTickets().size()));
                    sb.append(System.lineSeparator());
                });



        return sb.toString() ;



    }

    @Override
    public Passenger findByEmail(String email) {
        return passengerRepository.findByEmail(email);
    }
}
