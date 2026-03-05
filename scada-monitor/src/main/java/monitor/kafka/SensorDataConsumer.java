package monitor.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import monitor.controller.SensorManagerController;
import monitor.dto.SensorDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SensorDataConsumer {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final SensorManagerController sensorManager;

    public SensorDataConsumer(SimpMessagingTemplate messagingTemplate,
                              ObjectMapper objectMapper,
                              SensorManagerController sensorManager) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
        this.sensorManager = sensorManager;
    }

    @KafkaListener(topics = "scada.telemetry.v1", groupId = "scada-minimal-group")
    public void consume(String message) {
        try {
            SensorDataDTO dto = objectMapper.readValue(message, SensorDataDTO.class);

            if (dto.getSensorId() == null || dto.getSensorId().isBlank()) {
                log.error("❌ sensorId отсутствует");
                return;
            }
            if (dto.getTemperature() != null && (dto.getTemperature() < -100 || dto.getTemperature() > 100)) {
                log.error("❌ Температура вне диапазона: {}", dto.getTemperature());
                return;
            }
            if (dto.getFillLevel() != null && (dto.getFillLevel() < 0 || dto.getFillLevel() > 100)) {
                log.error("❌ Заполненность вне диапазона: {}", dto.getFillLevel());
                return;
            }

            if (dto.getTimestamp() == null || dto.getTimestamp() <= 0) {
                dto.setTimestamp(System.currentTimeMillis() / 1000);
            }

            sensorManager.updateOrCreateSensor(dto);

            messagingTemplate.convertAndSend("/topic/sensor-updates", dto);
            log.info("✅ Данные приняты: {}", dto);

        } catch (Exception e) {
            log.error("❌ Ошибка обработки Kafka: {}", e.getMessage());
        }
    }
}