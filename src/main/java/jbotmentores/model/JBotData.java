package jbotmentores.model;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

@Component
public class JBotData {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    private Map<String, Mentor> mentorsByEmail = new HashMap<>();
    private Map<String, Skill> skills = new HashMap<>();
    private Map<LocalDate, Set<Slot>> slots = new TreeMap<>();
    private Map<Skill, Set<String>> mentorsBySkill = new HashMap<>();
    private Map<String, Set<Skill>> skillsByMentor = new HashMap<>();
    private Map<Slot, Set<String>> mentorsBySlot = new HashMap<>();
    private Map<String, Set<Slot>> slotsByMentor = new HashMap<>();

    public Stream<Mentor> mentores() {
        try {
            readLock.lock();
            return new LinkedHashSet<>(this.mentorsByEmail.values()).stream();
        } finally {
            readLock.unlock();
        }
    }

    public Stream<Skill> skills() {
        try {
            readLock.lock();
            return new LinkedHashSet<>(this.skills.values()).stream();
        } finally {
            readLock.unlock();
        }
    }


    public Map<LocalDate, Set<Slot>> slots() {
        try {
            readLock.lock();
            TreeMap map = new TreeMap<>();
            map.putAll(this.slots);
            return map;
        } finally {
            readLock.unlock();
        }
    }

    public Set<Skill> skillsByMentor(Mentor mentor) {
        try {
            readLock.lock();
            return this.skillsByMentor.getOrDefault(mentor.email(), Set.of());
        } finally {
            readLock.unlock();
        }
    }

    public Map<LocalDate, Set<Slot>> slotsByMentor(Mentor mentor) {
        try {
            readLock.lock();
            TreeMap<LocalDate, Set<Slot>> map = new TreeMap<>();
            this.slotsByMentor.getOrDefault(mentor.email(), Set.of())
                    .forEach(slot -> map.computeIfAbsent(slot.from().toLocalDate(), k -> new LinkedHashSet<>()).add(slot));
            return map;
        } finally {
            readLock.unlock();
        }
    }

    private Mentor mentorByEmail(String email) {
        try {
            readLock.lock();
            Mentor mentor = this.mentorsByEmail.get(email);
            return mentor;
        } finally {
            readLock.unlock();
        }
    }

    private Mentor mentorOf(String name, String email) {
        try {
            writeLock.lock();
            Mentor mentor = this.mentorsByEmail.computeIfAbsent(email, k -> new Mentor(email, name));
            return mentor;
        } finally {
            writeLock.unlock();
        }
    }

    private Skill skillOf(String name) {
        try {
            writeLock.lock();
            return this.skills.computeIfAbsent(name, k -> new Skill(name));
        } finally {
            writeLock.unlock();
        }
    }

    private Slot slotOf(LocalDateTime from, LocalDateTime to) {
        try {
            writeLock.lock();
            Slot slot = new Slot(from, to);
            this.slots.computeIfAbsent(from.toLocalDate(), k -> new LinkedHashSet<>()).add(slot);
            return slot;
        } finally {
            writeLock.unlock();
        }
    }


    private void register(Mentor mentor, Skill skill) {
        try {
            writeLock.lock();
            this.skillsByMentor.computeIfAbsent(mentor.email(), k -> new LinkedHashSet<>()).add(skill);
            this.mentorsBySkill.computeIfAbsent(skill, k -> new LinkedHashSet<>()).add(mentor.email());
        } finally {
            writeLock.unlock();
        }
    }

    private void clearAll(){
        try {
            writeLock.lock();
          this.mentorsByEmail.clear();
            this. skills.clear();
            this.slots .clear();
            this.mentorsBySkill.clear();
            this.skillsByMentor .clear();
            this.mentorsBySlot.clear();
            this.slotsByMentor.clear();
        }finally {
            writeLock.unlock();
        }
    }

    private void register(Mentor mentor, Slot slot) {
        try {
            writeLock.lock();
            this.slotsByMentor.computeIfAbsent(mentor.email(), k -> new LinkedHashSet<>()).add(slot);
            this.mentorsBySlot.computeIfAbsent(slot, k -> new LinkedHashSet<>()).add(mentor.email());
        } finally {
            writeLock.unlock();
        }
    }

    //

    private void processData(Map<Integer, String> data) {
        try {
            writeLock.lock();
            var mentor = mentorOf(data.get(0), data.get(1));
            String[] skillsData =
                    data.getOrDefault(2,"").replaceAll("\\n", " ").replaceAll("/",",").split(",");

            var skills = Arrays
                    .stream(skillsData).map(String::trim).map(sd -> skillOf(sd))
                    .map(skill -> {
                        register(mentor, skill);
                        return skill;
                    }).collect(Collectors.toList());

            String dia22 = data.getOrDefault(3, "");
            loadDay(mentor, LocalDate.of(2021, 10, 22), dia22);
            String dia23 = data.getOrDefault(4, "");
            loadDay(mentor, LocalDate.of(2021, 10, 23), dia23);
            String dia24 = data.getOrDefault(5, "");
            loadDay(mentor, LocalDate.of(2021, 10, 24), dia24);
        } finally {
            writeLock.unlock();
        }
    }


    private void loadDay(Mentor mentor,
                         LocalDate day,
                         String dia22) {
        String[] slotData =
                dia22.replaceAll("\\*nãoestoudisponívelnessedia\\*", "")
                        .replaceAll(" ", "")
                        .replaceAll("24h", "23h59")
                        .split(",");
        var mentorSlot =
                Arrays.stream(slotData)
                        .map(String::trim)
                        .filter(d -> !"*nãoestoudisponívelnessedia*".equals(d)).map(sd -> {
                            var horas = sd.split("-");
                            var from = Slot.parseToLocalDateTime(day, horas[0]);
                            var to = Slot.parseToLocalDateTime(day, horas.length > 1 ? horas[1] : horas[0]);
                            Slot slot = slotOf(from, to);
                            return slot;
                        })
                        .map(slot -> {
                            register(mentor, slot);
                            return slot;
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

                for (Row row : sheet) {
                    if(row.getRowNum()>1) {
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
        }finally {
            writeLock.unlock();
        }
    }

    public Stream<Mentor> mentoresBySkill(Skill skill) {
        try {
            readLock.lock();
            return this.mentorsBySkill.getOrDefault(skill, Set.of()).stream().map(this::mentorByEmail).collect(Collectors.toCollection(LinkedHashSet::new)).stream();
        } finally {
            readLock.unlock();
        }
    }

    public Stream<Mentor> mentoresBySlot(Slot slot) {
        try {
            readLock.lock();
            return this.mentorsBySlot.getOrDefault(slot, Set.of()).stream().map(this::mentorByEmail).collect(Collectors.toCollection(LinkedHashSet::new)).stream();
        } finally {
            readLock.unlock();
        }
    }
}
