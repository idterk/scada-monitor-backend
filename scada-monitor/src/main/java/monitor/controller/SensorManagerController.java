package monitor.controller;

import monitor.dto.SensorDataDTO;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/sensors")
public class SensorManagerController {

    private final Map<String, SensorDataDTO> allSensors = new ConcurrentHashMap<>();
    private final AtomicInteger sensorCounter = new AtomicInteger(1);

    public SensorManagerController() {
        SensorDataDTO defaultSensor = new SensorDataDTO(
                "sensor1",
                0.0,
                0,
                System.currentTimeMillis() / 1000
        );
        allSensors.put("sensor1", defaultSensor);
    }

    @GetMapping
    public List<String> getAllSensors() {
        return new ArrayList<>(allSensors.keySet());
    }

    @GetMapping("/{sensorId}")
    public SensorDataDTO getSensor(@PathVariable String sensorId) {
        return allSensors.get(sensorId);
    }

    @PostMapping("/add")
    public SensorDataDTO addSensor() {
        int nextId = sensorCounter.incrementAndGet();
        String newSensorId = "sensor" + nextId;

        SensorDataDTO newSensor = new SensorDataDTO(
                newSensorId,
                0.0,
                0,
                System.currentTimeMillis() / 1000
        );
        allSensors.put(newSensorId, newSensor);
        return newSensor;
    }

    public void updateOrCreateSensor(SensorDataDTO dto) {
        String sensorId = dto.getSensorId();

        if (!allSensors.containsKey(sensorId)) {
            try {
                int num = Integer.parseInt(sensorId.replace("sensor", ""));
                if (num > sensorCounter.get()) {
                    sensorCounter.set(num);
                }
            } catch (Exception e) {

            }
        }

        SensorDataDTO existing = allSensors.get(sensorId);
        if (existing == null) {
            allSensors.put(sensorId, dto);
        } else {
            if (dto.getTemperature() != null) {
                existing.setTemperature(dto.getTemperature());
            }
            if (dto.getFillLevel() != null) {
                existing.setFillLevel(dto.getFillLevel());
            }
            if (dto.getTimestamp() != null) {
                existing.setTimestamp(dto.getTimestamp());
            }
            allSensors.put(sensorId, existing);
        }
    }
}