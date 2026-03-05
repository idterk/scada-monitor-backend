package monitor.controller;

import monitor.dto.SensorDataDTO;
import monitor.kafka.CommandProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/sensor")
public class SensorController {

    private final CommandProducer commandProducer;
    private final SimpMessagingTemplate messagingTemplate;
    private final SensorManagerController sensorManager;

    public SensorController(CommandProducer commandProducer,
                            SimpMessagingTemplate messagingTemplate,
                            SensorManagerController sensorManager) {
        this.commandProducer = commandProducer;
        this.messagingTemplate = messagingTemplate;
        this.sensorManager = sensorManager;
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateSensor(@RequestBody @Valid SensorDataDTO dto) {

        if (dto.getTimestamp() == null || dto.getTimestamp() <= 0) {
            dto.setTimestamp(System.currentTimeMillis() / 1000);
        }

        log.info("🖱️ Пользователь изменил: {} t={} fill={}",
                dto.getSensorId(), dto.getTemperature(), dto.getFillLevel());

        sensorManager.updateOrCreateSensor(dto);

        if (dto.getTemperature() != null) {
            commandProducer.sendCommand(dto.getSensorId(), "SET_TEMPERATURE", dto.getTemperature());
        }
        if (dto.getFillLevel() != null) {
            commandProducer.sendCommand(dto.getSensorId(), "SET_FILL_LEVEL", dto.getFillLevel());
        }

        messagingTemplate.convertAndSend("/topic/sensor-updates", dto);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Данные приняты",
                "timestamp", dto.getTimestamp()
        ));
    }
}