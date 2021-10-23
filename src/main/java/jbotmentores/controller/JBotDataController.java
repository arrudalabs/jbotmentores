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

}

