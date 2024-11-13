package valentino.coderhouse.jpa.services;

import valentino.coderhouse.jpa.dto.TimeApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
public class MainService {

    private static final Logger logger = LoggerFactory.getLogger(MainService.class);

    public LocalDateTime getCurrentArgentinaDateTime() {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://timeapi.io/api/Time/current/zone?timeZone=America/Argentina/Buenos_Aires";

        try {
            TimeApiResponse response = restTemplate.getForObject(url, TimeApiResponse.class);
            if (response != null && response.getDateTime() != null) {

                logger.info("Fecha y hora obtenidas desde la API: {}", response.getDateTime());
                return LocalDateTime.parse(response.getDateTime());
            } else {
                logger.warn("ALa API retornó una respuesta nula. Usando la hora local.");
                return LocalDateTime.now();
            }
        } catch (Exception e) {
            logger.error("Error al obtener la hora de la API. Usando la hora local.", e);
            return LocalDateTime.now();
        }

    }

}
