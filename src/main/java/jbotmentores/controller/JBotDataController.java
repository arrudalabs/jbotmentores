package jbotmentores.controller;

import jbotmentores.model.JBotData;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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

/*
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
    }*/

}

