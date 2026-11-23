package cl.lacasajojo.menu;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Menú de ejemplo (el mismo que traía la app de forma local)
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner sembrarMenu(PlatoRepository platos, ExtraRepository extras) {
        return args -> {
            if (platos.count() == 0) {
                platos.save(new Plato("Guatitas a la Jardinera", 4600));
                platos.save(new Plato("Cerdo mongoliano", 4600));
                platos.save(new Plato("Filete de Pollo", 4600));
                platos.save(new Plato("Tortilla de Porotos verdes", 4600));
                platos.save(new Plato("Pollo Asado", 4800));
                platos.save(new Plato("Carne al jugo", 4800));
            }
            if (extras.count() == 0) {
                extras.save(new Extra("Puré (natural)", TipoExtra.AGREGADO));
                extras.save(new Extra("Arroz normal", TipoExtra.AGREGADO));
                extras.save(new Extra("Arroz integral", TipoExtra.AGREGADO));
                extras.save(new Extra("Verduras salteadas", TipoExtra.AGREGADO));
                extras.save(new Extra("Fideos blancos", TipoExtra.AGREGADO));
                extras.save(new Extra("Fideos al pesto", TipoExtra.AGREGADO));
                extras.save(new Extra("Mix de ensaladas", TipoExtra.ENSALADA));
            }
        };
    }
}
