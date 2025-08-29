package sample.service.espresso;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
class EspressoServer {
//    @Scheduled(fixedDelay = 2 * 60000)
//    public void drinkEspresso() {
//        RestTemplate restTemplate = new RestTemplate();
//        String apiUrl = "https://initializr-nea0.onrender.com";
//        String websiteUrl = "https://start-spring-plus.onrender.com";
//        try {
//            restTemplate.getForEntity(apiUrl, String.class);
//            restTemplate.getForEntity(websiteUrl, String.class);
//            System.out.println("Poked Initializr");
//        } catch (RestClientException e) {
//            System.out.println("Something went wrong while drinking espresso");
//        }
//    }
}
