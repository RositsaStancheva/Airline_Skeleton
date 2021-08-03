package softuni.exam.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.models.Dto.TicketSeedDto;
import softuni.exam.models.Dto.TicketSeedRootDto;
import softuni.exam.models.entity.Ticket;
import softuni.exam.repository.TicketRepository;
import softuni.exam.service.PassengerService;
import softuni.exam.service.PlaneService;
import softuni.exam.service.TicketService;
import softuni.exam.service.TownService;
import softuni.exam.util.ValidationUtil;
import softuni.exam.util.XmlParser;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class TicketServiceImpl implements TicketService {
    private static final String TICKET_FILE_PATH = "src/main/resources/files/xml/tickets.xml";

    private final TicketRepository ticketRepository;
    private final ModelMapper modelMapper;
    private final ValidationUtil validationUtil;
    private final XmlParser xmlParser;
    private final TownService townService;
    private final PassengerService passengerService;


    public TicketServiceImpl(TicketRepository ticketRepository, ModelMapper modelMapper, ValidationUtil validationUtil, XmlParser xmlParser, TownService townService, PassengerService passengerService) {
        this.ticketRepository = ticketRepository;
        this.modelMapper = modelMapper;
        this.validationUtil = validationUtil;
        this.xmlParser = xmlParser;
        this.townService = townService;

        this.passengerService = passengerService;
    }


    @Override
    public boolean areImported() {
        return ticketRepository.count() > 0;
    }

    @Override
    public String readTicketsFileContent() throws IOException {
        return Files.readString(Path.of(TICKET_FILE_PATH));
    }

    @Override
    public String importTickets() throws JAXBException, FileNotFoundException {
        StringBuilder sb = new StringBuilder();

        xmlParser.fromFile(TICKET_FILE_PATH , TicketSeedRootDto.class)
                .getTickets().stream()
                .filter(ticketSeedDto -> {
                    boolean isValid = validationUtil.isValid(ticketSeedDto);
                    sb.append(isValid ? String.format("Successfully imported Ticket %s - %s"
                    ,ticketSeedDto.getFromTown(), ticketSeedDto.getToTown()) : "Invalid Ticket" )
                            .append(System.lineSeparator());
                    return isValid;
                })
                .map(ticketSeedDto -> {
                    Ticket ticket =modelMapper.map(ticketSeedDto, Ticket.class);
                    ticket.setFromTown(townService.getTownByName(ticketSeedDto.getFromTown().getName()));
                    ticket.setToTown(townService.getTownByName(ticketSeedDto.getToTown().getName()));
                    ticket.setPassenger(passengerService.findByEmail(ticketSeedDto.getPassenger().getEmail()));
                    return ticket;
                })
                .forEach(ticketRepository::save);


        return sb.toString();


    }
}
