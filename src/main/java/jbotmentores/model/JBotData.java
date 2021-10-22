package jbotmentores.model;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Component
@Validated
public class JBotData {

    private static final Logger logger = LoggerFactory.getLogger(JBotData.class);

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    private Validator validator;
    private MentorRepository mentorRepository;
    private DiscordInfoRepository discordInfoRepository;

    public JBotData(Validator validator, MentorRepository mentorRepository, DiscordInfoRepository discordInfoRepository) {
        this.validator = validator;
        this.mentorRepository = mentorRepository;
        this.discordInfoRepository = discordInfoRepository;
    }


    @Transactional(Transactional.TxType.REQUIRES_NEW)
    protected void clearAll() {
        try {
            writeLock.lock();
            this.mentorRepository.deleteAll();
        } finally {
            writeLock.unlock();
        }
    }


    protected void processData(Map<Integer, String> data) {
        try {
            writeLock.lock();

            var name = data.get(0);
            var email = data.get(1);


            String[] skillsData =
                    data.getOrDefault(2, "").replaceAll("\\n", " ").replaceAll("/", ",").split(",");
            var skills = Arrays
                    .stream(skillsData)
                    .map(String::trim)
                    .collect(Collectors.toSet());

            Set<Slot> slots = new TreeSet<>();
            String dia22 = data.getOrDefault(3, "");
            slots.addAll(loadDay(LocalDate.of(2021, 10, 22), dia22));
            String dia23 = data.getOrDefault(4, "");
            slots.addAll(loadDay(LocalDate.of(2021, 10, 23), dia23));
            String dia24 = data.getOrDefault(5, "");
            slots.addAll(loadDay(LocalDate.of(2021, 10, 24), dia24));


            Mentor mentor = new Mentor(email, name, skills, slots);

            saveMentor(mentor);
        } finally {
            writeLock.unlock();
        }
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void saveMentor(@Valid Mentor mentor) {
        mentorRepository.save(mentor);
        discordInfoRepository
                .findByMentor(mentor)
                .ifPresentOrElse(
                        info -> {
                            logger.info("{} user is activated already", info.getMentorEmail());
                        },
                        () -> {
                            DiscordInfo discordInfo = new DiscordInfo(mentor);
                            discordInfoRepository.save(discordInfo);
                            logger.info("{} user needs to be activated", discordInfo.getMentorEmail());
                        });
    }


    private List<Slot> loadDay(
            LocalDate day,
            String dia22) {
        String[] slotData =
                dia22.replaceAll("\\*nãoestoudisponívelnessedia\\*", "")
                        .replaceAll(" ", "")
                        .replaceAll("24h", "23h59")
                        .split(",");
        return
                Arrays.stream(slotData)
                        .map(String::trim)
                        .filter(d -> !"*nãoestoudisponívelnessedia*".equals(d)).map(sd -> {
                            var horas = sd.split("-");
                            var from = Slot.parseToLocalDateTime(day, horas[0]);
                            var to = Slot.parseToLocalDateTime(day, horas.length > 1 ? horas[1] : horas[0]);
                            return new Slot(from, to);
                        })
                        .collect(Collectors.toList());
    }


    public void updateDataFrom(InputStream inputStream) throws IOException {
        try (
                Workbook wb = new XSSFWorkbook(inputStream)
        ) {
            writeLock.lock();
            clearAll();
            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                Sheet sheet = wb.getSheetAt(i);
                System.out.println(wb.getSheetName(i));
                if(!"Mentoria | colabfest 2021".equalsIgnoreCase(wb.getSheetName(i))){
                    continue;
                }
                for (Row row : sheet) {
                    if (row.getRowNum() > 1) {
                        System.out.println("rownum: " + row.getRowNum());
                        Map<Integer, String> data = new LinkedHashMap<>();
                        for (Cell cell : row) {
                            if (CellType.STRING.equals(cell.getCellType())) {
                                data.put(cell.getAddress().getColumn(), cell.getStringCellValue());
                            } else if (CellType.NUMERIC.equals(cell.getCellType())) {
                                data.put(cell.getAddress().getColumn(), "" + cell.getNumericCellValue());
                            }
                        }
                        try {
                            processData(data);
                        } catch (RuntimeException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        } finally {
            writeLock.unlock();
        }
    }
}
