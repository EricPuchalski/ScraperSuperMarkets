package com_supermarket.utils;

import java.util.ArrayList;
import java.util.List;

public class HiperLibertadUtils {
    private static final String BASE_URL = "https://www.hiperlibertad.com.ar";

    public static List<String> getAllCategoryUrls() {
        List<String> urls = new ArrayList<>();

        // Tecnología
        urls.add(BASE_URL + "/tecnologia/tv-y-video");
        urls.add(BASE_URL + "/tecnologia/audio");
        urls.add(BASE_URL + "/tecnologia/informatica");
        urls.add(BASE_URL + "/tecnologia/celulares-y-tablets");
        urls.add(BASE_URL + "/tecnologia/videojuegos");
        urls.add(BASE_URL + "/tecnologia/smartwatch");

        // Electrodomésticos
        urls.add(BASE_URL + "/electrodomesticos/climatizacion");
        urls.add(BASE_URL + "/electrodomesticos/pequenos-electrodomesticos");
        urls.add(BASE_URL + "/electrodomesticos/lavado");
        urls.add(BASE_URL + "/electrodomesticos/cocinas-y-hornos");
        urls.add(BASE_URL + "/electrodomesticos/heladeras-y-freezers");
        urls.add(BASE_URL + "/electrodomesticos/hogar-y-limpieza");
        urls.add(BASE_URL + "/electrodomesticos/cuidado-personal-y-salud");
        urls.add(BASE_URL + "/electrodomesticos/termotanques-y-calefones");

        // Hogar
        urls.add(BASE_URL + "/hogar/muebles-de-interior");
        urls.add(BASE_URL + "/hogar/cocina-y-comedor");
        urls.add(BASE_URL + "/hogar/bano");
        urls.add(BASE_URL + "/hogar/organizacion");
        urls.add(BASE_URL + "/hogar/iluminacion");
        urls.add(BASE_URL + "/hogar/dormitorio");
        urls.add(BASE_URL + "/hogar/herramientas-y-mantenimiento");
        urls.add(BASE_URL + "/hogar/deco");

        // Bebidas
        urls.add(BASE_URL + "/bebidas/aperitivos");
        urls.add(BASE_URL + "/bebidas/cervezas");
        urls.add(BASE_URL + "/bebidas/gaseosas");
        urls.add(BASE_URL + "/bebidas/jugos");
        urls.add(BASE_URL + "/bebidas/aguas");
        urls.add(BASE_URL + "/bebidas/vinos-y-espumantes");
        urls.add(BASE_URL + "/bebidas/isotonicas-y-energizantes");
        urls.add(BASE_URL + "/bebidas/bebidas-blancas-y-licores");

        // Almacén
        urls.add(BASE_URL + "/almacen/aceites-y-vinagres");
        urls.add(BASE_URL + "/almacen/aceitunas-y-encurtidos");
        urls.add(BASE_URL + "/almacen/aderezos");
        urls.add(BASE_URL + "/almacen/arroz-y-legumbres");
        urls.add(BASE_URL + "/almacen/caldos-sopas-y-pure");
        urls.add(BASE_URL + "/almacen/conservas");
        urls.add(BASE_URL + "/almacen/desayuno-y-merienda");
        urls.add(BASE_URL + "/almacen/golosinas-y-chocolates");
        urls.add(BASE_URL + "/almacen/harinas");
        urls.add(BASE_URL + "/almacen/sin-tacc");
        urls.add(BASE_URL + "/almacen/panificados");
        urls.add(BASE_URL + "/almacen/para-preparar");
        urls.add(BASE_URL + "/almacen/pastas-secas-y-salsas");
        urls.add(BASE_URL + "/almacen/sal-pimienta-y-especias");
        urls.add(BASE_URL + "/almacen/snacks");

        // Lácteos
        urls.add(BASE_URL + "/lacteos/dulce-de-leche");
        urls.add(BASE_URL + "/lacteos/leches");
        urls.add(BASE_URL + "/lacteos/cremas");
        urls.add(BASE_URL + "/lacteos/yogures");
        urls.add(BASE_URL + "/lacteos/mantecas-y-margarinas");
        urls.add(BASE_URL + "/lacteos/postres-y-flanes");

        // Quesos y Fiambres
        urls.add(BASE_URL + "/quesos-y-fiambres/quesos");
        urls.add(BASE_URL + "/quesos-y-fiambres/fiambres");
        urls.add(BASE_URL + "/quesos-y-fiambres/salchichas");

        // Carnes
        urls.add(BASE_URL + "/carnes/mariscos");
        urls.add(BASE_URL + "/carnes/pescado");
        urls.add("http://hiperlibertad.com.ar/carnes/embutidos");
        urls.add(BASE_URL + "/carnes/carne-de-pollo");
        urls.add("http://hiperlibertad.com.ar/carnes/carne-de-cerdo");
        urls.add(BASE_URL + "/carnes/carne-vacuna");

        // Frutas y Verduras
        urls.add(BASE_URL + "/frutas-y-verduras/frutas");
        urls.add(BASE_URL + "/frutas-y-verduras/verduras");
        urls.add(BASE_URL + "/frutas-y-verduras/huevos");
        urls.add(BASE_URL + "/frutas-y-verduras/legumbres-y-semillas");
        urls.add(BASE_URL + "/frutas-y-verduras/hierbas-aromaticas");
        urls.add(BASE_URL + "/frutas-y-verduras/lena-y-carbon");

        // Congelados
        urls.add(BASE_URL + "/congelados/frutas-congeladas");
        urls.add(BASE_URL + "/congelados/verduras-congeladas");
        urls.add(BASE_URL + "/congelados/papas-congeladas");
        urls.add(BASE_URL + "/congelados/comidas-preparadas");
        urls.add(BASE_URL + "/congelados/prefritos-congelados");
        urls.add(BASE_URL + "/congelados/helados-y-postres");
        urls.add(BASE_URL + "/congelados/carnes-y-pollo");
        urls.add(BASE_URL + "/congelados/hamburguesas-y-milanesas");

        // Pastas frescas y tapas
        urls.add(BASE_URL + "/pastas-frescas-y-tapas/levaduras-y-grasas");
        urls.add(BASE_URL + "/pastas-frescas-y-tapas/fideos-y-noquis");
        urls.add(BASE_URL + "/pastas-frescas-y-tapas/pastas-rellenas");
        urls.add(BASE_URL + "/pastas-frescas-y-tapas/tapas");

        // Taeq
        urls.add(BASE_URL + "/taeq/almacen-taeq");
        urls.add(BASE_URL + "/taeq/frutas-y-verduras-taeq");
        urls.add(BASE_URL + "/taeq/congelados-taeq");

        // Limpieza
        urls.add(BASE_URL + "/limpieza/accesorios-de-limpieza");
        urls.add(BASE_URL + "/limpieza/calzado");
        urls.add(BASE_URL + "/limpieza/cuidado-de-la-ropa");
        urls.add(BASE_URL + "/limpieza/desodorantes-de-ambiente");
        urls.add(BASE_URL + "/limpieza/insecticidas");
        urls.add(BASE_URL + "/limpieza/lavandina");
        urls.add(BASE_URL + "/limpieza/limpieza-de-bano");
        urls.add(BASE_URL + "/limpieza/limpieza-de-cocina");
        urls.add(BASE_URL + "/limpieza/limpieza-de-pisos-y-muebles");
        urls.add(BASE_URL + "/limpieza/papeles");

        // Perfumería
        urls.add(BASE_URL + "/perfumeria/cuidado-capilar");
        urls.add(BASE_URL + "/perfumeria/cuidado-oral");
        urls.add(BASE_URL + "/perfumeria/cuidado-personal");
        urls.add(BASE_URL + "/perfumeria/cuidado-de-la-piel");
        urls.add(BASE_URL + "/perfumeria/proteccion-femenina");
        urls.add(BASE_URL + "/perfumeria/proteccion-para-adultos");
        urls.add(BASE_URL + "/perfumeria/farmacia");

        // Bebés y niños
        urls.add(BASE_URL + "/bebes-y-ninos/higiene-y-salud");
        urls.add(BASE_URL + "/bebes-y-ninos/lactancia-y-alimentacion");
        urls.add(BASE_URL + "/bebes-y-ninos/seguridad-del-bebe");
        urls.add(BASE_URL + "/bebes-y-ninos/paseo-del-bebe");
        urls.add(BASE_URL + "/bebes-y-ninos/vehiculos-infantiles");
        urls.add(BASE_URL + "/bebes-y-ninos/muebles-infantiles");
        urls.add(BASE_URL + "/bebes-y-ninos/jugueteria");
        urls.add(BASE_URL + "/bebes-y-ninos/accesorios");
        urls.add(BASE_URL + "/bebes-y-ninos/panales-y-toallitas-humedas");

        // Vehículos
        urls.add(BASE_URL + "/vehiculos/accesorios-para-automoviles");
        urls.add(BASE_URL + "/vehiculos/accesorios-para-motos");
        urls.add(BASE_URL + "/vehiculos/neumaticos");

        // Mascotas
        urls.add(BASE_URL + "/mascotas/alimentos");
        urls.add(BASE_URL + "/mascotas/accesorios-para-mascotas");

        // Aire libre y jardín
        urls.add(BASE_URL + "/aire-libre-y-jardin/camping");
        urls.add(BASE_URL + "/aire-libre-y-jardin/piletas");
        urls.add(BASE_URL + "/aire-libre-y-jardin/cuidado-del-jardin");
        urls.add(BASE_URL + "/aire-libre-y-jardin/muebles-de-exterior");
        urls.add(BASE_URL + "/aire-libre-y-jardin/asador");
        urls.add(BASE_URL + "/aire-libre-y-jardin/iluminacion-exterior");

        // Librería
        urls.add(BASE_URL + "/libreria/libreria-y-papeleria");

        // Deportes
        urls.add(BASE_URL + "/deportes/fitness");

        return urls;
    }
}
