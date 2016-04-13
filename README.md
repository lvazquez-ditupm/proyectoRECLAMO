# Proyecto RECLAMO

## Instalación:

Toda la información se puede encontrar en el siguiente link:
http://reclamoairsupm.wikidot.com/

## Contenido:

* RECLAMO: Contiene el código preparado para ser ejecutado, con las librerías compiladas y los distintos ficheros de configuración

* config_files: Contiene los distintos ficheros de configuración necesarios para el funcionamiento correcto del software externo a RECLAMO (Nagios, SANCP...)

* libraries: Contiene el código de las librerías que se usan en RECLAMO

* ontologies: Contiene todos los ficheros .owl para el funcionamiento del sistema

* responseModule: Módulo de respuestas del sistema (debe copiarse el fichero agents.conf a /etc)

* running_files: Contiene los scripts de arranque del escenario y otros necesarios para pruebas

* sensor: Contiene logs de SANCP

* vnx_scenarios: Contiene los ficheros necesarios para virtualizar la red del escenario con VNX

## Consideraciones:

* Los distintos ficheros de configuración están situados en RECLAMO/build y RECLAMO/lib

* Deben modificarse estos ficheros para que las URIs de las ontologías apunten a la ruta en la que se ejecuta el código (además de otros parámetros necesarios para poder ejecutarse en cualquier sistema)

