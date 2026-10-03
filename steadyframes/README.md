# Steady Frames (NeoForge 1.21.1)

Mod solo-cliente, sin mixins. Mide el frametime y ajusta la distancia de render
de 1 en 1 chunk entre `minRenderDistance` y `maxRenderDistance` para mantener
el objetivo de FPS. Deja el slider de Minecraft en 32: el mod baja solo cuando
hace falta y vuelve a subir cuando hay margen.

Comando: `/steadyframes status` y `/steadyframes reset`.
Config: `config/steadyframes-client.toml` (se crea al primer arranque).

## Compilar
Opcion A (GitHub, sin instalar nada): sube esta carpeta a un repo -> pestana
Actions -> "Build mod" -> descarga el artefacto `steadyframes-jar`.

Opcion B (local): JDK 21 + Gradle 8.10+ -> `gradle build` -> `build/libs/steadyframes-1.0.0.jar`.

## Estabilidad de GC (argumentos JVM recomendados, Java 21)
-XX:+UseZGC -XX:+ZGenerational -Xms4G -Xmx4G -XX:+AlwaysPreTouch
(ajusta la RAM a tu equipo; si tu launcher usa Java 17, usa G1 con -XX:MaxGCPauseMillis=50)
