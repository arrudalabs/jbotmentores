package jbotmentores.controller;

import jbotmentores.model.JBotData;
import jbotmentores.model.Mentor;
import jbotmentores.model.Skill;
import jbotmentores.model.Slot;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@CrossOrigin("*")
public class JBotDataController {

    final JBotData jBotData;

    public JBotDataController(JBotData jBotData) {
        this.jBotData = jBotData;
    }

    @PostMapping(value = "/data/upload",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @CacheEvict({
            "listAllMentors",
            "listAllSkills",
            "listAllSlots"
    })
    public ResponseEntity<?> updateData(@RequestParam("file") MultipartFile xlsxFile)
            throws IOException {
        jBotData.updateDataFrom(xlsxFile.getInputStream());
        return ResponseEntity.status(201).body(xlsxFile.getSize());
    }

    @GetMapping(value = "/mentors")
    @Cacheable("listAllMentors")
    public Collection<MentorDTO> listAllMentors() {
        List<MentorDTO> data = new LinkedList<>();
        jBotData.mentores()
                .forEach(mentor -> {
                    data.add(
                            getMentorDTO(mentor)
                    );
                });
        return data;
    }


    static class MentorDTO {

        private final String email;
        private final String name;
        private final Set<Skill> skills;
        private final Map<LocalDate, Set<String>> slots;

        MentorDTO(String email,
                  String name,
                  Set<Skill> skills,
                  Map<LocalDate, Set<String>> slots) {
            this.email = email;
            this.name = name;
            this.skills = skills;
            this.slots = slots;
        }

        public String getEmail() {
            return email;
        }

        public String getName() {
            return name;
        }

        public Set<Skill> getSkills() {
            return skills;
        }

        public Map<LocalDate, Set<String>> getSlots() {
            return slots;
        }
    }

    @GetMapping(value = "/skills")
    public Collection<Skill> listAllSkills() {
        return jBotData.skills().collect(Collectors.toSet());
    }

    @GetMapping(value = "/skills/mentores")
    @Cacheable("listAllSkills")
    public Collection<SkillDTO> listAllMentorsBySkills() {
        Map<Skill, SkillDTO> data = new LinkedHashMap<>();
        jBotData.skills()
                .forEach(skill -> {
                    data.computeIfAbsent(skill, k -> new SkillDTO(skill.getName(), new LinkedHashSet<>()))
                            .getMentors().addAll(jBotData.mentoresBySkill(skill).map(this::getMentorDTO).collect(Collectors.toSet()));
                });
        return data.values();
    }


    @GetMapping(value = "/slots")
    public Map<LocalDate, Set<String>> listAllSlots() {
        Map<LocalDate, Set<String>> data = new TreeMap<>();
        jBotData.slots().forEach((localDate, slots) -> {
            data.computeIfAbsent(localDate, k -> new TreeSet<>()).addAll(slots.stream().map(Slot::printTimeRange).collect(Collectors.toList()));
        });
        return data;
    }

    @GetMapping(value = "/slots/mentores")
    @Cacheable("listAllSlots")
    public Map<LocalDate, SlotDTO> listAllMentoresBySlots() {
        Map<LocalDate, SlotDTO> data = new TreeMap<>();
        jBotData.slots()
                .entrySet()
                .forEach(entry -> {
                    SlotDTO slotDTO = data.computeIfAbsent(entry.getKey(), k -> new SlotDTO(new TreeMap<>()));
                    entry.getValue().forEach(slot -> {
                        slotDTO.getSlots().computeIfAbsent(slot.printTimeRange(), k -> new LinkedHashSet<>())
                                .addAll(jBotData.mentoresBySlot(slot).map(this::getMentorDTO).collect(Collectors.toSet()));
                    });
                });
        return data;
    }

    private MentorDTO getMentorDTO(Mentor mentor) {
        return new MentorDTO(
                mentor.getEmail(),
                mentor.getName(),
                jBotData.skillsByMentor(mentor),
                Optional.of(jBotData.slotsByMentor(mentor)).map(localDateSetMap -> {
                    Map<LocalDate, Set<String>> data = new TreeMap<>();
                    localDateSetMap.forEach((localDate, slots) -> {
                        data.computeIfAbsent(localDate, k -> new TreeSet<>())
                                .addAll(slots.stream().map(Slot::printTimeRange).collect(Collectors.toSet()));
                    });
                    return data;
                }).orElse(Map.of())
        );
    }

    static class SkillDTO {
        private final String name;
        private final Set<MentorDTO> mentors;

        SkillDTO(String name, Set<MentorDTO> mentors) {

            this.name = name;
            this.mentors = mentors;
        }

        public String getName() {
            return name;
        }

        public Set<MentorDTO> getMentors() {
            return mentors;
        }
    }

    static class SlotDTO {
        private final Map<String, Set<MentorDTO>> slots;

        SlotDTO(Map<String, Set<MentorDTO>> slots) {

            this.slots = slots;
        }

        public Map<String, Set<MentorDTO>> getSlots() {
            return slots;
        }
    }
}

