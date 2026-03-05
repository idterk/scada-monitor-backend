package monitor.controller;

import monitor.dto.SensorDataDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/state")
public class StateController {

    private static SensorManagerController sensorManager;

    @Autowired
    public void setSensorManager(SensorManagerController sensorManager) {
        StateController.sensorManager = sensorManager;
    }

    public static void updateState(SensorDataDTO dto) {
        if (sensorManager != null) {
            sensorManager.updateOrCreateSensor(dto);
        }
    }

    @GetMapping("/{sensorId}")
    public SensorDataDTO getSensorState(@PathVariable String sensorId) {
        return sensorManager != null ? sensorManager.getSensor(sensorId) : null;
    }
}