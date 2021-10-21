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


    static record MentorDTO(String email,
                            String name,
                            Set<Skill> skills,
                            Map<LocalDate, Set<String>> slots) {
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
                    data.computeIfAbsent(skill, k -> new SkillDTO(skill.name(), new LinkedHashSet<>()))
                            .mentors().addAll(jBotData.mentoresBySkill(skill).map(this::getMentorDTO).collect(Collectors.toSet()));
                });
        return data.values();
    }


    @GetMapping(value = "/slots")
    public Map<LocalDate, Set<String>> listAllSlots() {
        Map<LocalDate, Set<String>> data = new TreeMap<>();
        jBotData.slots().forEach((localDate, slots) -> {
            data.computeIfAbsent(localDate,k->new TreeSet<>()).addAll(slots.stream().map(Slot::printTimeRange).collect(Collectors.toList()));
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
                        slotDTO.slots().computeIfAbsent(slot.printTimeRange(), k -> new LinkedHashSet<>())
                                .addAll(jBotData.mentoresBySlot(slot).map(this::getMentorDTO).collect(Collectors.toSet()));
                    });
                });
        return data;
    }

    private MentorDTO getMentorDTO(Mentor mentor) {
        return new MentorDTO(
                mentor.email(),
                mentor.name(),
                jBotData.skillsByMentor(mentor),
                Optional.of(jBotData.slotsByMentor(mentor)).map(localDateSetMap -> {
                    Map<LocalDate,Set<String>> data=new TreeMap<>();
                    localDateSetMap.forEach((localDate, slots) -> {
                        data.computeIfAbsent(localDate,k->new TreeSet<>())
                                .addAll(slots.stream().map(Slot::printTimeRange).collect(Collectors.toSet()));
                    });
                    return data;
                }).orElse(Map.of())
        );
    }

    static record SkillDTO(String name, Set<MentorDTO> mentors) {

    }

    static record SlotDTO(Map<String, Set<MentorDTO>> slots) {

    }
}

