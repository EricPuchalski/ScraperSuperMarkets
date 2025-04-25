package com_supermarket.utils;

import org.openqa.selenium.chrome.ChromeOptions;


public class ChromeUtils {

        private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36";

        public static ChromeOptions getChromeOptions() {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--user-agent=" + USER_AGENT);

            // optimización
            options.addArguments("--disable-dev-shm-usage"); // Evita problemas de memoria en contenedores
            options.addArguments("--no-sandbox"); // Más seguro en entornos CI/CD
            options.addArguments("--blink-settings=imagesEnabled=false"); // Desactiva la carga de imágenes

            return options;
        }
}
